/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.InvalidOperationException;
import mano.Resettable;
import mano.io.Buffer;
import mano.net.ByteArrayBuffer;
import mano.net.Channel;
import mano.net.ChannelHandler;
import mano.util.NameValueCollection;

/**
 *
 * @author jun
 */
class HttpRequestImpl extends HttpRequest implements HttpRequestAppender {

    String method;
    String rawUrl;
    String version;
    HttpHeaderCollection headers;
    Map<String, String> _form;
    Map<String, HttpPostFile> _files;
    Map<String, String> _query;
    long _contentLength;
    HttpChannel connection;
    AtomicLong remaining = new AtomicLong(0);
    boolean isChunked;
    boolean _hasPostData;
    boolean isFormUrlEncoded;
    boolean isFormMultipart;
    String _boundary;
    private URL url;

    public HttpRequestImpl(HttpChannel conn) {
        this.headers = new HttpHeaderCollection();
        connection = conn;
    }

    @Override
    public String method() {
        return this.method;
    }

    @Override
    public String rawUrl() {
        return this.rawUrl;
    }

    @Override
    public URL url() {
        if (url == null) {
            String path;
            if (this.rawUrl.startsWith("/")) {
                path = this.isSecure() ? "https://" : "http://";
                path += this.headers.get("host").value();
                path += this.rawUrl;
            } else {
                path = this.rawUrl;
            }

            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                throw new mano.InvalidOperationException(e.getMessage(), e);
            }
        }
        return url;
    }

    @Override
    public long getContentLength() {
        return this._contentLength;
    }

    boolean hasPostData() throws HttpException {
        if ("POST".equalsIgnoreCase(this.method)
                || "PUT".equalsIgnoreCase(this.method)) {
            if (this.headers.containsKey("Transfer-Encoding")
                    && "chunked".equalsIgnoreCase(this.headers.get("Transfer-Encoding").value())) {
                isChunked = true;
                _hasPostData = true;
            } else if (!this.headers.containsKey("Content-length")) {
                throw new HttpException(HttpStatus.LengthRequired, "Length Required");
            } else {
                this._contentLength = Long.parseLong(this.headers.get("Content-Length").value());
                if (this._contentLength > 0) {
                    remaining.set(_contentLength);
                    _hasPostData = true;
                }
            }
        }

        if (_hasPostData && this.headers.containsKey("Content-Type")) {

            //String ctype = (this.headers.get("Content-Type").value() + "").trim();
            if ("application/x-www-form-urlencoded".equalsIgnoreCase(this.headers.get("Content-Type").value())) {
                isFormUrlEncoded = true;
            } else if ("multipart/form-data".equalsIgnoreCase(this.headers.get("Content-Type").value())) {
                isFormMultipart = true;
                _boundary = "--" + this.headers.get("Content-Type").attr("boundary");
            }
        }

        return _hasPostData;
    }

    @Override
    public Map<String, String> query() {
        if (_query == null) {
            _query = new NameValueCollection<>();

            String query = this.url().getQuery() == null ? "" : this.url().getQuery().trim();
            if ((query != null && !"".equals(query))) {
                if (query.startsWith("?")) {
                    query = query.substring(1);
                }
                String key;
                String value;
                int index;
                for (String s : query.split("&")) {
                    index = s.indexOf('=');
                    value = null;
                    if (index > 0) {
                        key = (s.substring(0, index) + "").trim();
                        value = (s.substring(index + 1) + "").trim();
                    } else {
                        key = s.trim();
                    }

                    if (key != null && !"".equals(key)) {
                        if (_query.containsKey(key)) {
                            String old = _query.get(key);
                            _query.put(key, (old == null ? "" : old) + "," + (value == null ? "" : value));
                        } else {
                            _query.put(key, value);
                        }
                    }
                }
            }
        }

        return _query;
    }
    final AtomicBoolean postLoadFlag = new AtomicBoolean(false);
    final AtomicBoolean waitFlag = new AtomicBoolean(false);

    @Override
    public synchronized void appendFormItem(String name, String value) {
        if (_form == null) {
            _form = new NameValueCollection<>();
        }

        if (_form.containsKey(name)) {
            String old = _form.get(name);
            _form.put(name, (old == null ? "" : old) + "," + (value == null ? "" : value));
        } else {
            _form.put(name, value);
        }
    }

    @Override
    public void notifyDone() {
        synchronized (this.waitFlag) {
            this.waitFlag.set(true);
            this.waitFlag.notify();
        }
    }

    @Override
    public synchronized void appendPostFile(HttpPostFile file) {
        if (_files == null) {
            _files = new NameValueCollection<>();
        }
        _files.put(file.getName(), file);
        System.err.println("SIZE:" + file.getLength());
    }

    private void loadPostData() {

        if (postLoadFlag.get()) {
            return;
        }
        postLoadFlag.set(true);

        boolean hasPostData;
        try {
            hasPostData = hasPostData();
        } catch (HttpException ex) {
            //TODO:抛出错误？
            return;
        }
        if (!hasPostData) {
            return;
        }

        if (isChunked) {
            throw new UnsupportedOperationException("chunked 编码未实现。");
        } else if (this.isFormMultipart || this.isFormUrlEncoded) {
            try {

                if (this.isFormUrlEncoded) {
                    loadEntityBody(new HttpFormUrlEncodedParser<>());
                } else {
                    loadEntityBody(new HttpMultipartParser<>());
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            synchronized (waitFlag) {
                if (!waitFlag.get()) { //等待处理完成
                    try {
                        waitFlag.wait(1000 * 60 * 5);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } else {
            throw new InvalidOperationException("未设置处理程序。");
        }
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String protocol() {
        return this.version;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canLoadEntityBody() {
        return this.isConnected() && !postLoadFlag.get();
    }

    @Override
    public void loadEntityBody(ChannelHandler<? extends Channel, ? extends Object> handler) throws Exception {
        this.connection.callHandler(handler, this);
    }

    @Override
    public void loadEntityBody() throws InvalidOperationException {
        loadPostData();
    }

    @Override
    public void Abort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpHeaderCollection headers() {
        return this.headers;
    }

    @Override
    public synchronized Map<String, String> form() {
        if (_form == null) {
            _form = new NameValueCollection<>();
            loadPostData();
        }
        return _form;
    }

    @Override
    public Map<String, HttpPostFile> files() {
        if (_files == null) {
            //_files = new NameValueCollection<>();
            loadPostData();
        }
        return this._files;
    }

    @Override
    public String getBoundary() {
        return this._boundary;
    }

    static class LoadExactDataHandlerxxxxxx extends ChannelHandler<HttpChannel, HttpRequestImpl> implements Resettable {

        HttpRequestImpl request;
        HttpEntityBodyHandler handler;
        long remaining = 0;
        Throwable error;

        @Override
        public void reset() {
            handler = null;
            request = null;
            remaining = 0;
            error = null;
        }

        @Override
        protected void onRead(HttpChannel channel, int bytesRead, ByteArrayBuffer buffer, HttpRequestImpl token) {
            token = token == null ? request : token;
            int pos = buffer.position();
            try {
                handler.onRead(buffer, token);
            } catch (Throwable ex) {
                this.error = ex;
                synchronized (token.postLoadFlag) {
                    token.postLoadFlag.set(true);
                    token.postLoadFlag.notify();
                }
                return;
            }

            int eat = buffer.position() - pos;
            remaining -= eat;
            if (remaining > 0) {
                if (buffer.hasRemaining()) {
                    buffer.compact();
                } else {
                    //抛弃未使用的数据？
                    if (true) {
                        remaining -= buffer.length();
                    }
                    buffer.reset();
                }
            }

            if (remaining > 0) {
                channel.read(this, token);
            } else {
                synchronized (token.postLoadFlag) {
                    token.postLoadFlag.set(true);
                    token.postLoadFlag.notify();
                }
            }
        }

        @Override
        protected void onFailed(HttpChannel channel, Throwable exc) {
            if (channel == null) {
                error = exc;
            } else {
                channel.onFailed(this, exc);
            }
        }
    }

}

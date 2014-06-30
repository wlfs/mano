/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.InvalidOperationException;
import mano.io.Buffer;
import mano.util.NameValueCollection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author jun
 */
class HttpRequestImpl extends HttpRequest implements HttpRequestAppender {

    String _method;
    String _rawUrl;
    String _version;
    HttpHeaderCollection _headers;
    Map<String, String> _form;
    Map<String, HttpPostFile> _files;
    Map<String, String> _query;
    long _contentLength;
    HttpContextImpl context;
    AtomicLong remaining = new AtomicLong(0);
    boolean isChunked;
    boolean _hasPostData;
    boolean isFormUrlEncoded;
    boolean isFormMultipart;
    String _boundary;

    public HttpRequestImpl(HttpContextImpl ctx) {
        this._headers = new HttpHeaderCollection();
        context = ctx;
    }

    @Override
    public String method() {
        return this._method;
    }

    @Override
    public String rawUrl() {
        return this._rawUrl;
    }

    @Override
    public URL url() {
        String url = "http://";
        url += this._headers.get("host").value();
        url += this._rawUrl;
        try {
            URL result = new URL(url);
            return result;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long contentLength() {
        return this._contentLength;
    }

    boolean hasPostData() throws HttpException {
        if ("POST".equalsIgnoreCase(this._method)
                || "PUT".equalsIgnoreCase(this._method)) {
            if (this._headers.containsKey("Transfer-Encoding")
                    && "chunked".equalsIgnoreCase(this._headers.get("Transfer-Encoding").value())) {
                isChunked = true;
                _hasPostData = true;
            } else if (!this._headers.containsKey("Content-length")) {
                throw new HttpException(HttpStatus.LengthRequired, "Length Required");
            } else {
                this._contentLength = Long.parseLong(this._headers.get("Content-Length").value());
                if (this._contentLength > 0) {
                    remaining.set(_contentLength);
                    _hasPostData = true;
                }
            }
        }

        if (_hasPostData && this._headers.containsKey("Content-Type")) {

            //String ctype = (this.headers.get("Content-Type").value() + "").trim();
            if ("application/x-www-form-urlencoded".equalsIgnoreCase(this._headers.get("Content-Type").value())) {
                isFormUrlEncoded = true;
            } else if ("multipart/form-data".equalsIgnoreCase(this._headers.get("Content-Type").value())) {
                isFormMultipart = true;
                _boundary = "--" + this._headers.get("Content-Type").attr("boundary");
            }
        }

        return _hasPostData;
    }

    @Override
    public Map<String, String> query() {
        if (_query == null) {
            _query = new NameValueCollection<>();

            String query = this.url().getQuery() == null ? "" : this.url().getQuery().trim();
            if ((query != null && "".equals(query))) {
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
                        _query.put(key, value);
                    }
                }
            }
        }

        return _query;
    }
    final AtomicBoolean postLoadFlag = new AtomicBoolean(false);

    public void setPostDataProcessHandler(String contentType, Object handler) {

    }

    @Override
    public synchronized void appendFormItem(String name, String value) {

        if (_form == null) {
            _form = new NameValueCollection<>();
        }
        _form.put(name, value);
    }

    @Override
    public synchronized void appendPostFile(HttpPostFile file) {
        if (_files == null) {
            _files = new NameValueCollection<>();
        }
        _files.put(file.getName(), file);
    }

    public void loadPostData() {
        boolean hasPostData;
        try {
            hasPostData = hasPostData();
        } catch (HttpException ex) {
            return;
        }
        if(!hasPostData){
            return;
        }
        
        if (isChunked) {
            //TODO: context.setPhase(HttpPhase.ResolveChunkedHeader);
        } else {
            System.out.println("here");
            context.requestHandler = new LoadExactDataHandler(this, new HttpMultipartParser(this._boundary));
            context.run();
        }
    }

    @Override
    public String version() {
        return this._version;
    }

    @Override
    public String protocol() {
        return this._version;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canLoadEntityBody() {
        return this.isConnected() && !postLoadFlag.get();
    }

    @Override
    public void loadEntityBody(HttpEntityBodyHandler handler) throws InvalidOperationException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadEntityBody() throws InvalidOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void Abort() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpHeaderCollection headers() {
        return this._headers;
    }

    class LoadExactDataHandler implements HttpRequestHandler {

        HttpRequestHandler handler;
        HttpRequestImpl request;
        HttpEntityBodyHandler handle;

        LoadExactDataHandler(HttpRequestImpl req, HttpEntityBodyHandler hd) {
            request = req;
            handle = hd;
        }

        @Override
        public synchronized void onRead(Buffer buffer) throws UnsupportedEncodingException, HttpException, IOException {
            handler = this;
            int p = buffer.position();
            handle.onRead(buffer, request);
            int z = buffer.position() - p;

            request.remaining.set(request.remaining.get() - z);
            if (request.remaining.get() <= 0) {
                handler = null;
                synchronized (postLoadFlag) {
                    request.postLoadFlag.set(true);
                    request.postLoadFlag.notify();
                }
            }
        }

        @Override
        public HttpRequestHandler next() {
            return this;
        }
    }

    private void loadMultipart() throws InterruptedException {
        synchronized (postLoadFlag) {
            if (postLoadFlag.get() || !this.isFormMultipart) {
                return;
            }
        }

        loadPostData();
        synchronized (postLoadFlag) {
            if (!postLoadFlag.get()) {
                postLoadFlag.wait(1000 * 60 * 5);
            }
        }
    }

    @Override
    public Map<String, String> form() {
        try {
            loadMultipart();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_form == null) {
            _form = new NameValueCollection<>();
        }
        return _form;
    }

    @Override
    public Map<String, HttpPostFile> files() {
        return this._files;
    }
}

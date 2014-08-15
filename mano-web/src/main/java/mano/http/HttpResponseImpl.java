/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map.Entry;
import mano.DateTime;
import mano.InvalidOperationException;
import mano.http.HttpCookieCollection.CookieEntry;
import mano.web.HttpSession;

/**
 *
 * @author jun
 */
final class HttpResponseImpl extends HttpResponse {

    HttpHeaderCollection headers;
    HttpConnection connection;
    int _bufferSize = 1024;
    boolean _headerSent = false;
    boolean _chunked = false;
    boolean _endFlush = false;
    long _contentLength = 0;
    String CRLF = "\r\n";

    HttpResponseImpl(HttpConnection conn) {
        this.connection = conn;
        this.headers = new HttpHeaderCollection();
        _buffer = ByteBuffer.allocate(_bufferSize);
    }

    @Override
    public HttpHeaderCollection headers() {
        return this.headers;
    }

    @Override
    public synchronized void setHeader(String name, String value) throws InvalidOperationException {
        if (this.headerSent()) {
            throw new InvalidOperationException("HTTP Header has been sent.");
        }
        if (headers.containsKey(name)) {
            headers.get(name).text(value);
        } else {
            headers.put(new HttpHeader(name, value));
        }
    }

    @Override
    public synchronized void setHeader(HttpHeader header) throws InvalidOperationException {
        if (this.headerSent()) {
            throw new InvalidOperationException("HTTP Header has been sent.");
        }
        if (headers.containsKey(header.name())) {
            this.setHeader(header.name(), header.text());
        } else {
            headers.put(header);
        }
    }

    private void writeHeaders() throws InvalidOperationException {
        if (!_headerSent) {

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s %s %s%s", "HTTP/1.1", this.status(), this.statusDescription(), CRLF));

            if (!headers.containsKey("Date")) {
                this.setHeader("Date", DateTime.now().toGMTString());
            }

            if (!headers.containsKey("Connection")) {
                this.setHeader("Connection", "keep-alive");
            }

            if (!headers.containsKey("Content-Type")) {
                this.setHeader("Content-Type", "text/html;charset=utf-8");
            }

            if (!this._chunked || this._endFlush) {
                this.setHeader("Content-Length", this._contentLength + "");
            } else {
                this.setHeader("Transfer-Encoding", "chunked");
            }
            
            if (connection.context != null) {
                if (connection.context.session != null && connection.context.session.isNewSession()) {
                    this.getCookie().set(HttpSession.COOKIE_KEY, connection.context.session.getSessionId(), 0, null, null, true, false);
                }
                this.setHeader("Server", connection.context.getServer().getVersion());
                this.setHeader("X-Powered-By", "mano/1.1,java/1.8");
            }
            

            for (Entry<String, HttpHeader> entry : headers.entrySet()) {
                sb.append(String.format("%s%s", entry.getValue().toString(), CRLF));
            }

            

            for (CookieEntry entry : this.getCookie().iterator()) {
                sb.append(String.format("%s%s%s", "Set-Cookie:", entry.toString(), CRLF));
            }

            sb.append(CRLF);
            ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes(this.charset()));

            //buffer.flip();
            connection.write(buffer);
            _headerSent = true;
        }
    }

    private ByteBuffer _buffer;

    @Override
    public synchronized void write(byte[] buf, int offset, int count) {

        if (_buffer == null) {
            _buffer = connection.getBuffer();
            _buffer.clear();
        }
        int size = Math.min(_buffer.remaining(), count);
        if (size <= 0) {
            this.flush();
            this.write(buf, offset, count);
        } else {

            _buffer.put(buf, offset, size);
            /*if(!buffering()){
               
             }*/
            _contentLength += size;
            if (count > size) {
                this.flush();
                this.write(buf, offset + size, count - size);
            }
        }
    }

    @Override
    public synchronized void transmit(String filename) throws FileNotFoundException, IOException {

        FileChannel chan = new FileInputStream(filename).getChannel();
        this.transmit(chan, 0, chan.size());
    }

    @Override
    public synchronized void transmit(String filename, long position, long length) throws IOException {
        this.transmit(new FileInputStream(filename).getChannel(), position, length);
    }

    private synchronized void transmit(FileChannel chan, long position, long length) throws IOException {
        this.flush();

        if (this._endFlush) {
            throw new IOException("错误的传输方式");
        } else if (this._chunked) {// || this._endFlush
            connection.write(ByteBuffer.wrap(String.format("%s %s", Long.toHexString(length), CRLF).getBytes(this.charset())));
            connection.transferFile(chan, position, length);
            connection.write(ByteBuffer.wrap(String.format("%s", CRLF).getBytes(this.charset())));

        } else {
            connection.transferFile(chan, position, length);
        }

    }
    private boolean auto = true;

    @Override
    public synchronized void flush() {
        if (!this._headerSent) {
            if (!this._endFlush && auto) {
                this._chunked = true;
            }

            try {
                this.writeHeaders();
            } catch (InvalidOperationException ex) {
            }
        }

        int len = 0;
        if (_buffer != null) {
            len = _buffer.position();
            if (len > 0) {
                _buffer.flip();
                len = _buffer.limit();
            } else {
                _buffer.clear();
                len = 0;
            }
        }

        if (_chunked) {
            if (len > 0) {
                connection.write(ByteBuffer.wrap(String.format("%s %s", Long.toHexString(len), CRLF).getBytes(this.charset())));
                connection.write(_buffer);
                _buffer = null;
                connection.write(ByteBuffer.wrap(String.format("%s", CRLF).getBytes(this.charset())));
                len = 0;
            }

            if (_endFlush && len <= 0) {
                connection.write(ByteBuffer.wrap(String.format("0%s%s", CRLF, CRLF).getBytes(this.charset())));//没有尾部
            }
        } else if (len > 0) {
            connection.write(_buffer);
            _buffer = null;
        }
        _contentLength -= len;
    }

    @Override
    public synchronized void end() {
        this._endFlush = true;
        this.flush();
        this.connection.end();
    }

    @Override
    public boolean headerSent() {
        return _headerSent;
    }

    @Override
    public boolean isConnected() {
        return true;// TODO
    }

    @Override
    public void setContentLength(long length) throws InvalidOperationException {
        if (this.headerSent()) {
            throw new InvalidOperationException("HTTP Header has been sent.");
        }
        auto = false;
        this._chunked = false;
        _contentLength = length;
    }

}

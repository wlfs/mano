/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.Disposable;
import mano.InvalidOperationException;
import mano.io.Buffer;
import mano.net.Connection;
import mano.net.Task;
import mano.web.WebApplication;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HttpContext的实现
 * @author jun(jun@diosay.com)
 */
class HttpContextImpl extends HttpContext implements Runnable, Disposable {

    Buffer buffer;
    ByteBuffer readBuffer;
    //final Queue<Task> _writeQueued;
    AtomicBoolean closedFlag = new AtomicBoolean(false);
    HttpService service;
    Connection conn;
    HttpRequestImpl req;
    HttpResponseImpl rsp;
    int errorCode = 0;
    Exception error;
    HttpRequestHandler requestHandler;
    WebApplication _application;
    boolean completed;
    HttpServer _server;
    boolean disposed;

    public HttpContextImpl(HttpService svc, Connection connection) {
        this.service = svc;
        this.conn = connection;
        //_writeQueued = new LinkedBlockingQueue<>();
    }

    @Override
    public HttpRequest request() {
        return req;
    }

    @Override
    public HttpResponse response() {
        return rsp;
    }

    public void init() {
        buffer = this.service.workBufferPool().get();
        req = new HttpRequestImpl(this);
        rsp = new HttpResponseImpl(this);
        requestHandler = new processRequestLine(this);
        receive();
    }

    synchronized void close() {
        requestHandler = null;
        conn.close(service.getMessage().attach(this));
    }

    synchronized void receive() {

        if (readBuffer != null) {
            buffer.write(readBuffer);
            if (!readBuffer.hasRemaining()) {
                service.ioBufferPool().put(readBuffer);
            }
            buffer.flush();
            this.run();
        } else {
            Task msg=service.getMessage().attach(this);
            msg.buffer(service.ioBufferPool().get());

            if (!conn.read(msg)) {
                this.close();// error
            }
        }
    }

    void write(ByteBuffer buffer) {
        write(service.getMessage().buffer(buffer).attach(this));
    }

    void write(Task msg){
        conn.write(msg);
        //this.flush();
    }

    /*synchronized void flush() {
        if (!conn.connected()) {
            close();
        } else if ((closedFlag.get() && _writeQueued.isEmpty())) {
            complete();
        } else {
            Task msg=_writeQueued.peek();
            if (msg != null && conn.write(msg)) {
                _writeQueued.poll();
            }
        }
    }*/

    synchronized void end() {
        this.closedFlag.set(true);
        //this.flush();
    }

    void complete() {
        if (completed) {
            return;
        }
        completed = true;
        String req_conn = req._headers.containsKey("Connection") ? req._headers.get("Connection").value().trim() : "keep-alive";
        String rsp_conn = rsp.headers.containsKey("Connection") ? req._headers.get("Connection").value().trim() : "keep-alive";
        if ("keep-alive".equalsIgnoreCase(req_conn) && conn.connected()) {//Keep-Alive: timeout=5, max=100 Connection: keep-alive
            HttpContextImpl ctx = new HttpContextImpl(service, conn);
            ctx.init();
        } else {
            this.close();
        }
        this.dispose();
    }

    /**
     * 只处理request请求（接收数据）。
     */
    @Override
    public void run() {
        if (requestHandler == null) {
            return;
        }
        try {
            requestHandler.onRead(buffer);
        } catch (HttpException | IOException ex) {
            service.logger().error("", ex);
            return;
        }
        //error
        if (requestHandler.next() == null) {
            //TODO: 未处理 buffer
            return;
        } else if (requestHandler.equals(requestHandler.next())) {
            if (buffer.hasRemaining()) {
                buffer.compact();
                if (!buffer.hasRemaining()) {
                }
            } else {
                buffer.reset();
            }
        } else {
            requestHandler = requestHandler.next();
            if (buffer.hasRemaining()) {
                run();
                return;
            } else {
                buffer.reset();
            }
        }
        this.receive();
    }

    @Override
    public HttpServer server() {
        return _server;
    }

    void onError(Throwable t) {
        try {
            int status;
            if (t instanceof HttpException) {
                status = ((HttpException) t).getHttpCode();
            } else {
                status = HttpStatus.InternalServerError;
            }
            try {
                rsp.status(status, HttpStatus.getKnowDescription(status));
            } catch (InvalidOperationException ignored) {
                this.close();
                return;
            }
            rsp.write("<html><head><title>%d Error</title></head><body>%s<body></html>", status, t.getMessage());
            rsp.end();
            //t.printStackTrace();
        } catch (Exception ex) {
            service.logger().error("", ex);
        }
    }

    @Override
    public WebApplication application() {
        return _application;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public synchronized void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        service.workBufferPool().put(buffer);
        buffer = null;
        if (readBuffer != null) {
            service.ioBufferPool().put(readBuffer);
        }
        readBuffer = null;
        /*Task msg;
         while (!_writeQueued.isEmpty()) {
            msg = _writeQueued.poll();
            if (msg != null) {
                msg.dispose();
            }
        }
        _writeQueued.clear();*/

    }

    /*####################################################*/
    class processRequestLine implements HttpRequestHandler {

        HttpRequestHandler handler;
        HttpContextImpl context;

        public processRequestLine(HttpContextImpl ctx) {
            context = ctx;
        }

        @Override
        public void onRead(Buffer buffer) {
            handler = this;
            String line;
            try {
                if ((line = buffer.readln()) != null) {
                    String[] arr = line.split(" ");
                    if (arr.length != 3) {
                        throw new HttpException(HttpStatus.BadRequest, "Bad Request Line");
                    }
                    context.req._method = arr[0].trim();
                    context.req._rawUrl = arr[1].trim();
                    context.req._version = arr[2].trim();
                    handler = new processRequestHeader(context);
                }
            } catch (UnsupportedEncodingException | HttpException ex) {
                handler = null;
                context.onError(ex);
            }
        }

        @Override
        public HttpRequestHandler next() {
            return handler;
        }

    }

    class processRequestHeader implements HttpRequestHandler {

        HttpRequestHandler handler;
        HttpContextImpl context;

        public processRequestHeader(HttpContextImpl ctx) {
            context = ctx;
        }

        @Override
        public void onRead(Buffer buffer) {
            handler = this;
            String line;
            HttpHeader header;
            try {
                while ((line = buffer.readln()) != null) {
                    if ("".equals(line)) {
                        handler = null;
                        context.req.hasPostData(); //确定POST数据
                        if (!context.service.handle(context)) {
                            throw new HttpException(HttpStatus.BadRequest, "Bad Request (Invalid Hostname)");
                        }
                        return;
                    }
                    header = HttpHeader.prase(line);
                    if (header == null) {
                        throw new HttpException(HttpStatus.BadRequest, "Bad Request");
                    }
                    context.req._headers.put(header);
                }
            } catch (UnsupportedEncodingException | HttpException ex) {
                handler = null;
                context.onError(ex);
            }
        }

        @Override
        public HttpRequestHandler next() {
            return handler;
        }

    }

}

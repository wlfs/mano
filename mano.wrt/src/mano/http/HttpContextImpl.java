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
import mano.web.HttpSession;

/**
 * HttpContext的实现
 *
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
    }

    @Override
    public HttpRequest getRequest() {
        return req;
    }

    @Override
    public HttpResponse getResponse() {
        return rsp;
    }

    @Override
    public WebApplication getApplication() {
        return _application;
    }

    public void init() {
        buffer = this.service.workBufferPool().get();
        req = new HttpRequestImpl(this);
        rsp = new HttpResponseImpl(this);
        requestHandler = new processRequestLine(this);
        receive();
    }

    /**
     * 关闭连接
     */
    synchronized void close() {
        requestHandler = null;
        conn.close(true, service.newTask().attach(this));
    }

    /**
     * 接收数据
     */
    synchronized void receive() {

        if (readBuffer != null) {
            buffer.write(readBuffer);
            if (!readBuffer.hasRemaining()) {
                service.ioBufferPool().put(readBuffer);
            }
            buffer.flush();
            this.run();
        } else {
            Task task = service.newTask().attach(this);
            task.buffer(service.ioBufferPool().get());

            if (!conn.read(task)) {
                this.close();// error
            }
        }
    }

    /**
     * 定入数据
     *
     * @param buffer
     */
    void write(ByteBuffer buffer) {
        write(service.newTask().buffer(buffer).attach(this));
    }

    /**
     * 执行传输一个任务
     *
     * @param task
     */
    void write(Task task) {
        conn.write(task);
        conn.flush();
    }

    /**
     * 结束
     */
    synchronized void end() {
        closedFlag.set(true);
        conn.flush(service.newTask().attach(this));
    }

    /**
     * 完成
     */
    void complete() {
        //System.out.println("finish");
        if (completed) {
            return;
        }
        completed = true;
        String req_conn = req._headers.containsKey("Connection") ? req._headers.get("Connection").value().trim() : "keep-alive";
        String rsp_conn = rsp.headers.containsKey("Connection") ? rsp.headers.get("Connection").value().trim() : "keep-alive";
        if ("keep-alive".equalsIgnoreCase(req_conn) && "keep-alive".equalsIgnoreCase(rsp_conn) && conn.isConnected()) {//Keep-Alive: timeout=5, max=100 Connection: keep-alive
            service.context(conn);
        } else {
            this.close();
        }
        this.dispose();
    }

    /**
     * 只处理request请求（接收数据）。
     */
    @Override
    public synchronized void run() {
        if (requestHandler == null) {
            return;
        }
        try {
            requestHandler.onRead(buffer);
        } catch (HttpException | IOException ex) {
            this.onError(ex);
            return;
        }

        //error
        if (this.disposed || this.completed || requestHandler.getNextHandler() == null) {
            //TODO: 未处理 buffer
            requestHandler = null;
            return;
        } else if (requestHandler.equals(requestHandler.getNextHandler())) {
            if (buffer.hasRemaining()) {
                buffer.compact();
                if (!buffer.hasRemaining()) {
                    //数据未处理?
                    this.onError(new HttpException(HttpStatus.InternalServerError, "Internal Server Error(buffer full)"));
                    return;
                }
            } else {
                buffer.reset();
            }
        } else {
            requestHandler = requestHandler.getNextHandler();
            if (requestHandler == null) {
                //什么情况?
                this.onError(new HttpException(HttpStatus.InternalServerError, "Internal Server Error(lost handler)"));
                return;
            }
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
    public HttpServer getServer() {
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
                rsp.setHeader("Connection", "close");
                rsp.status(status, HttpStatus.getKnowDescription(status));
            } catch (InvalidOperationException ignored) {
                this.close();
                return;
            }
            rsp.write("<html><head><title>%d Error</title></head><body>%s<body></html>", status, t.getMessage());
            rsp.end();
            //t.printStackTrace();
        } catch (Exception ex) {
            service.getLogger().error("", ex);
        }
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
    HttpSession session;
    @Override
    public HttpSession getSession() {
        return session;
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
        public HttpRequestHandler getNextHandler() {
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
                        handler = null;//结束头部解析
                        context.req.hasPostData(); //提前确定POST数据，发现错误
                        if (!context.service.handle(context)) {
                            throw new HttpException(HttpStatus.BadRequest, "Bad Request (Invalid Hostname)");
                        }
                        if (!context.isCompleted()) {
                            context.getResponse().end();
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
        public HttpRequestHandler getNextHandler() {
            return handler;
        }

    }

}

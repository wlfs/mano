/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.WritePendingException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import mano.InvalidOperationException;
import mano.Resettable;
import mano.io.Buffer;
import mano.net.ByteArrayBuffer;
import mano.util.ThreadPool;

/**
 *
 * @author jun <jun@diosay.com>
 */
class HttpConnection {

    AsynchronousSocketChannel channel;
    HttpContextImpl context;
    HttpRequestImpl request;
    ByteBuffer readBuffer;
    ByteArrayBuffer buffer;
    int phase;
    HttpService service;
    final Queue<SentCompletionHandler> paddings = new LinkedBlockingQueue<>();
    TransferProxy proxy;

    public HttpConnection() {
        proxy = new TransferProxy();
        buffer = new ByteArrayBuffer(1024 * 4);
    }

    public ByteBuffer getBuffer() {
        return service.ioBufferPool().get();
    }

    public void putBuffer(ByteBuffer buf) {
        service.ioBufferPool().put(buf);
    }

    public void open(AsynchronousSocketChannel chan) {
        channel = chan;
        proxy.channel = chan;
        ResolveRequestLineHandler handler = service.resolveRequestLineHandlerPool.get();
        try {
            handler.connection = this;
            this.read(handler);
        } catch (Exception ex) {
            this.failed(ex, this);
        }
    }

    public void read(ReceivedHandler handler) throws Exception {
        if (handler == null) {
            throw new InvalidOperationException();
        }
        ReceivedCompletionHandler rc = service.receivedCompletionHandlerPool.get();
        rc.connection = this;
        rc.handler = handler;
        receive(readBuffer, this, rc);
    }

    private <A> void receive(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
        if (buffer == null) {
            readBuffer = buffer = getBuffer();
        }
        channel.read(buffer, 1000 * 5, TimeUnit.MILLISECONDS, attachment, handler);
    }

    private <A> void sent(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
        sent0(buffer, attachment, handler, true, 0);
    }

    private <A> void sent0(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler, boolean canTry, int tries) {
        try {
            channel.write(buffer, 1000 * 5, TimeUnit.MILLISECONDS, attachment, handler);
        } catch (WritePendingException ex) {
            if (canTry && tries < 50) {
                try {
                    Thread.sleep(100);//5s
                    sent0(buffer, attachment, handler, true, tries++);
                } catch (InterruptedException ignored) {
                    sent0(buffer, attachment, handler, false, tries++);
                }
            } else {
                throw ex;
            }
        }
    }

    public void transferFile(FileChannel chan, long pos, long len) {
        SentCompletionHandler handler = service.sentCompletionHandlerPool.get();
        handler.connection = this;
        handler.channel = chan;
        handler.position = pos;
        handler.length = len;
        this.paddings.offer(handler);
        handler = null;//立即脱离引用
        flush();
    }

    public void write(ByteBuffer buffer) {
        SentCompletionHandler handler = service.sentCompletionHandlerPool.get();
        handler.connection = this;
        handler.buffer = buffer;
        this.paddings.offer(handler);
        handler = null;//立即脱离引用
        flush();
    }

    public void end() {
        SentCompletionHandler handler = service.sentCompletionHandlerPool.get();
        handler.connection = this;
        handler.close = true;
        this.paddings.offer(handler);
        handler = null;//立即脱离引用
        flush();
    }

    private void close() {
        close0();
        if (context != null) {
            context.completed = true;
        }
        //service.onClosed(this);
    }

    private void flush() {
        FlushHandler handler = service.flushHandlerPool.get();
        handler.connection = this;
        ThreadPool.execute(handler);
    }

    private void close0() {
        try {
            channel.close();
        } catch (IOException ex) {
            //ignored
        }
    }

    private void failed(Throwable exc, Object attachment) {
        System.out.println("error===================");
        exc.printStackTrace();
        this.close0();
    }

    static class ReceivedCompletionHandler implements CompletionHandler<Integer, Object> {

        HttpConnection connection;
        ReceivedHandler handler;

        public void init(HttpConnection c, ReceivedHandler h) {
            connection = c;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            if (result < 0) {
                failed(new java.io.IOException("This remote Connection was alread closed."), attachment);
            } /*else if (connection.readBuffer.hasRemaining()) {
             connection.receive(connection.readBuffer, attachment, this);
             }*/ else {
                connection.readBuffer.flip();
                connection.buffer.write(connection.readBuffer);
                connection.buffer.flush();
                if (!connection.readBuffer.hasRemaining()) {
                    connection.putBuffer(connection.readBuffer);
                    connection.readBuffer = null;
                }
                process(handler, attachment);
                connection.service.receivedCompletionHandlerPool.put(this);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            connection.failed(exc, attachment);
        }

        private void process(ReceivedHandler handler, Object attachment) {
            try {
                int old = connection.buffer.position();
                ReceivedHandler next = handler.onRead(connection.buffer);

                if (next != null) {
                    if (old == connection.buffer.position()) {
                        connection.buffer.compact();
                        connection.read(next);
                    } else {
                        process(next, attachment);
                    }
                } else {

                }
                handler = null;
            } catch (Exception ex) {
                failed(ex, attachment);
            }
        }
    }

    static class SentCompletionHandler implements CompletionHandler<Integer, Object>, Resettable {

        HttpConnection connection;
        ByteBuffer buffer;
        FileChannel channel;
        long position;
        long length;
        boolean close;

        @Override
        public void reset() {
            connection = null;
            buffer = null;
            channel = null;
            position = 0;
            length = 0;
            close = false;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            if (result < 0) {
                failed(new java.io.IOException("This remote Connection was alread closed."), attachment);
            } else if (buffer.hasRemaining()) {
                connection.sent(buffer, attachment, this);
            } else {
                connection.putBuffer(buffer);
                buffer = null;

                connection.flush();
                connection.service.sentCompletionHandlerPool.put(this);
            }
            //System.out.println("sent:" + result);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            connection.failed(exc, attachment);
        }

    }

    static class ResolveRequestLineHandler implements ReceivedHandler, Resettable {

        HttpConnection connection;

        @Override
        public ReceivedHandler onRead(ByteArrayBuffer buffer) throws Exception {

            String line;
            if ((line = buffer.readln()) != null) {
                String[] arr = line.split(" ");
                if (arr.length != 3) {
                    throw new HttpException(HttpStatus.BadRequest, "Bad Request Line");
                }
                //System.out.println(line);
                HttpRequestImpl request = new HttpRequestImpl(null);
                request.method = arr[0].trim();
                request.rawUrl = arr[1].trim();
                request.version = arr[2].trim();
                connection.request = request;
                ResolveRequestHeadersHandler next = new ResolveRequestHeadersHandler();
                next.connection = connection;
                return next;
            }
            return this;
        }

        @Override
        public void reset() {
            connection = null;
        }

    }

    static class ResolveRequestHeadersHandler implements ReceivedHandler, Resettable {

        HttpConnection connection;

        @Override
        public ReceivedHandler onRead(ByteArrayBuffer buffer) throws Exception {

            String line;
            HttpHeader header;
            while ((line = buffer.readln()) != null) {
                if ("".equals(line)) {
                    connection.request.hasPostData(); //提前确定POST数据，发现错误
                    //connection.close0();

                    if (!connection.service.processRequest(connection.request)) {
                        connection.failed(new HttpException(HttpStatus.BadRequest, "Bad Request(Invalid Hostname)"), this);
                        return null;
                    } else {
                        //handler.init();
                    }
                    //HttpResponseImpl response = new HttpResponseImpl(connection);
                    //response.write("hello world");
                    //response.end();
                    //get_app
                    //
                    //wait done
                    return null;
                }
                System.out.println(line);
                header = HttpHeader.prase(line);
                if (header == null) {
                    throw new HttpException(HttpStatus.BadRequest, "Bad Request");
                }
                connection.request.headers.put(header);
            }
            return this;
        }

        @Override
        public void reset() {
            connection = null;
        }
    }

    static class FlushHandler implements Runnable, Resettable {

        HttpConnection connection;

        @Override
        public void run() {
            synchronized (connection.paddings) {
                if (!connection.paddings.isEmpty()) {
                    SentCompletionHandler next = connection.paddings.poll();
                    if (next != null) {
                        if (next.close) {
                            connection.close();
                            connection.service.sentCompletionHandlerPool.put(next);
                        } else if (next.channel != null) {
                            try {
                                next.channel.transferTo(next.position, next.length, connection.proxy);
                                next.channel.close();
                                connection.service.sentCompletionHandlerPool.put(next);
                            } catch (Exception ex) {
                                connection.failed(ex, this);
                            }
                            connection.flush();
                        } else {
                            try {
                                connection.sent(next.buffer, this, next);
                            } catch (Exception ex) {
                                connection.failed(ex, this);
                            }
                        }
                    }

                    next = null;//立即脱离引用
                }
            }
            connection.service.flushHandlerPool.put(this);
        }

        @Override
        public void reset() {
            connection = null;
        }

    }

    static class TransferProxy implements ReadableByteChannel, WritableByteChannel {

        AsynchronousSocketChannel channel;

        @Override
        public int read(ByteBuffer dst) throws IOException {

            try {
                Future<Integer> result = channel.read(dst);
                return result.get(1000 * 5, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }

        @Override
        public boolean isOpen() {
            return channel.isOpen();
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }

        @Override
        public int write(ByteBuffer src) throws IOException {

            try {
                Future<Integer> result = channel.write(src);
                int count = result.get(1000 * 5, TimeUnit.MILLISECONDS);
                //Logger.getDefault().info("sent:%s", count);
                return count;
            } catch (WritePendingException ex) {
                return this.write(src);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }

    }
}

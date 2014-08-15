/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.WritePendingException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.Resettable;
import mano.util.ThreadPool;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class AioSocketChannel implements Channel {
    
    AsynchronousSocketChannel channel;
    final ReceiveCompletionHandler receiveHandler;
    final SentCompletionHandler sentHandler;
    ByteArrayBuffer buffer;
    TransferProxy proxy;
    
    public AioSocketChannel() {
        receiveHandler = new ReceiveCompletionHandler(this);
        sentHandler = new SentCompletionHandler(this);
    }
    
    public void open() {
        
        proxy.channel = channel;
    }
    
    public void resetBuffer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void clearBuffer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void allocate(boolean directed) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Buffer wrap(Object obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Buffer wrap(String filename, long position, long length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void free(Buffer buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public synchronized void read(ChannelHandler handler, Object attachment) {
        handler.attach(attachment);
        read0(handler, true, 0);
    }

    /**
     * 将任务提交到异步队列
     *
     * @param handler
     * @param canTry
     * @param tries
     */
    private void read0(ChannelHandler handler, boolean canTry, int tries) {
        try {
            channel.read(buffer.inner(), 1000 * 5, TimeUnit.MILLISECONDS, handler, receiveHandler);
        } catch (WritePendingException ex) {
            if (canTry && tries < 50) {
                try {
                    Thread.sleep(100);//5s
                    read0(handler, true, tries++);
                } catch (InterruptedException ignored) {
                    read0(handler, false, tries++);
                }
            } else {
                this.onFailed(this, ex);
            }
        }
    }
    
    protected final Queue<Buffer> paddings = new LinkedBlockingQueue<>();
    
    @Override
    public void write(Buffer buffer) {
        if (buffer == null || buffer.equals(this.getBuffer())) {
            throw new java.lang.IllegalArgumentException("buffer");
        }
        paddings.offer(buffer);
        this.flush();
    }
    
    private void flush() {
        
    }
    
    private void write(ByteBuffer buffer, SentCompletionHandler handler, boolean canTry, int tries) {
        try {
            channel.write(buffer, 1000 * 5, TimeUnit.MILLISECONDS, buffer, handler);
        } catch (WritePendingException ex) {
            if (canTry && tries < 50) {
                try {
                    Thread.sleep(100);//5s
                    write(buffer, handler, true, tries++);
                } catch (InterruptedException ignored) {
                    write(buffer, handler, false, tries++);
                }
            } else {
                throw ex;
            }
        }
    }
    
    @Override
    public final void close(boolean force) {
        if (force) {
            close0();
        } else {
            
        }
    }
    
    private void close0() {
        try {
            this.channel.close();
        } catch (IOException ex) {
            //ignored
        }
    }
    
    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public ByteArrayBuffer getBuffer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public abstract void onFailed(Object sender, Throwable exc);
    
    @Override
    public abstract void onClosed();
    
    private static class ReceiveCompletionHandler<T extends ChannelHandler> implements CompletionHandler<Integer, T> {
        
        AioSocketChannel channel;
        
        ReceiveCompletionHandler(AioSocketChannel chan) {
            channel = chan;
        }
        
        @Override
        public void completed(Integer bytesTransferred, T handler) {
            if (bytesTransferred < 0) {
                failed(new IOException("this remote connection was closed."), handler);
            } else {
                handler.init(channel.receiveHandler, channel, bytesTransferred, channel.buffer, null, null);
                ThreadPool.execute(handler);
            }
        }
        
        @Override
        public void failed(Throwable exc, T handler) {
            handler.init(channel.receiveHandler, channel, -1, null, null, exc);
            ThreadPool.execute(handler);
        }
    }
    
    private static class SentCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
        
        AioSocketChannel channel;
        Buffer buffer;
        
        SentCompletionHandler(AioSocketChannel chan) {
            channel = chan;
        }
        
        @Override
        public void completed(Integer bytesTransferred, ByteBuffer attachment) {
            if (bytesTransferred < 0) {
                failed(new IOException("this remote connection was closed."), null);
            } else {
                //but buffer
            }
        }
        
        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            channel.onFailed(this, exc);
        }
    }
    
    private static class PaddingClose implements Buffer {
        
    }
    
    private static class FlushHandler implements Runnable, Resettable {
        
        AioSocketChannel chan;
        
        @Override
        public void run() {
            synchronized (chan.paddings) {
                if (!chan.paddings.isEmpty()) {
                    Buffer buf = chan.paddings.poll();
                    if (buf != null) {
                        if (buf instanceof PaddingClose) {
                            chan.close(true);
                            //put buffer
                        } else if (buf instanceof FileRegin) {
                            try {
                                //open file
                                //next.channel.transferTo(next.position, next.length, connection.proxy);
                                //next.channel.close();
                                //connection.service.sentCompletionHandlerPool.put(next);
                            } catch (Exception ex) {
                                chan.onFailed(this, ex);
                            }
                            //connection.flush();
                        }
                    }
                    
                    buf = null;//立即脱离引用
                }
            }
        }
        
        @Override
        public void reset() {
            chan = null;
            //put this
        }
        
    }
    
    private static class TransferProxy implements ReadableByteChannel, WritableByteChannel {
        
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
            channel = null; //调用者自己关闭
        }
        
        @Override
        public int write(ByteBuffer src) throws IOException {
            return write(src, true, 0);
        }
        
        private int write(ByteBuffer src, boolean cantry, int tried) throws IOException {
            try {
                Future<Integer> result = channel.write(src);
                int count = result.get(1000 * 5, TimeUnit.MILLISECONDS);
                //Logger.getDefault().info("sent:%s", count);
                return count;
            } catch (WritePendingException ex) {
                if (cantry && tried < 50) {
                    return this.write(src, cantry, tried++);
                }
                throw new IOException(ex.getMessage(), ex);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }
        
    }
}

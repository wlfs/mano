/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.FileInputStream;
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
import mano.Resettable;
import mano.util.ThreadPool;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class AioSocketChannel implements Channel {

    private final ReceiveCompletionHandler receiveHandler;
    private final SentCompletionHandler sentHandler;
    private final FlushHandler flushHandler;
    private final TransferProxy proxy;
    private ByteArrayBuffer buffer;
    private AsynchronousSocketChannel channel;

    public AioSocketChannel() {
        receiveHandler = new ReceiveCompletionHandler(this);
        sentHandler = new SentCompletionHandler(this);
        flushHandler = new FlushHandler(this);
        proxy = new TransferProxy();
    }

    public void open(AsynchronousSocketChannel chan, ByteArrayBuffer buf) {
        this.buffer = buf;
        buffer.reset();
        channel = chan;
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

    @Override
    public synchronized void callHandler(ChannelHandler handler, Object attachment) {
        handler.attach(attachment);
        handler.init(this, this, buffer.length(), buffer, null);
        handler.run();
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
        } catch (Exception ex) {
            this.onFailed(this, ex);
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
        ThreadPool.execute(this.flushHandler);
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
            onClosed();
        } else {
            paddings.offer(new PaddingClose());
            this.flush();
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
        return buffer;
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public abstract void onFailed(Object sender, Throwable exc);

    @Override
    public abstract void onClosed();

    public void onFlush(Buffer buffer, long bytesTransferred) {

    }

    public boolean isOpen() {
        if (channel == null) {
            return true;
        }
        return channel.isOpen();
    }

    private static class ReceiveCompletionHandler<T extends ChannelHandler> implements CompletionHandler<Integer, T> {

        AioSocketChannel channel;

        ReceiveCompletionHandler(AioSocketChannel chan) {
            channel = chan;
        }

        @Override
        public void completed(Integer bytesTransferred, T handler) {
            if (bytesTransferred < 0) {
                channel.close0();
                failed(new IOException("this remote connection was closed."), handler);
            } else {
                channel.buffer.flush();
                handler.init(channel.receiveHandler, channel, bytesTransferred, channel.buffer, null);
                ThreadPool.execute(handler);
            }
        }

        @Override
        public void failed(Throwable exc, T handler) {
            channel.onFailed(this, exc);
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
                channel.close0();
                failed(new IOException("this remote connection was closed."), attachment);
            } else {
                channel.onFlush(buffer, bytesTransferred);
                buffer = null;
                attachment = null;
                channel.flush();
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            channel.onFlush(buffer, 0);
            buffer = null;
            attachment = null;
            channel.onFailed(this, exc);
        }
    }

    private static class PaddingClose implements Buffer {

    }

    private static class FlushHandler implements Runnable, Resettable {

        AioSocketChannel chan;

        FlushHandler(AioSocketChannel channel) {
            chan = channel;
        }

        @Override
        public void run() {
            synchronized (chan.paddings) {

                if (!chan.paddings.isEmpty()) {
                    Buffer buf = chan.paddings.poll();
                    if (buf != null) {
                        if (buf instanceof PaddingClose) {
                            chan.close(true);
                            chan.onFlush(buf, -1);
                        } else if (buf instanceof FileRegin) {
                            long sent = 0;
                            try {
                                FileRegin regin = (FileRegin) buf;
                                try (FileInputStream in = new FileInputStream(regin.getFilename())) {
                                    sent = in.getChannel().transferTo(regin.getPosition(), regin.getLimit(), chan.proxy);
                                }
                            } catch (Exception ex) {
                                chan.onFailed(this, ex);
                            }
                            chan.onFlush(buf, sent);

                        } else if (buf instanceof ByteArrayBuffer) {
                            try {
                                chan.sentHandler.buffer = buf;
                                chan.write(((ByteArrayBuffer) buf).inner(), chan.sentHandler, true, 0);
                            } catch (Exception ex) {
                                chan.onFailed(this, ex);
                            }
                        } else if (buf instanceof DBuffer) {
                            try {
                                chan.sentHandler.buffer = buf;
                                chan.write(((DBuffer) buf).inner(), chan.sentHandler, true, 0);
                            } catch (Exception ex) {
                                chan.onFailed(this, ex);
                            }
                        } else {
                            chan.onFlush(buf, -1);//抛弃
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

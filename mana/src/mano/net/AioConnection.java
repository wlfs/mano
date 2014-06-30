/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import mano.util.CachedObjectFactory;
import mano.util.Pool;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class AioConnection extends Connection {

    private static final int MODE_SERVER = 1, MODE_CONNECTOR = 2;

    AsynchronousServerSocketChannel _server;
    AsynchronousSocketChannel _client;
    Pool<AcceptedHandler> acceptFactory;
    Pool<ReceivedHandler> readFactory;
    Pool<SentHandler> writeFactory;
    InetSocketAddress _address;
    int _mode = 0;
    int _backlog = 100;
    boolean _isBind;

    private AioConnection(CachedObjectFactory<Task> handlerFactory, int mode) {
        _mode = mode;
        //_handlerFactory = handlerFactory;

    }

    public AioConnection(CachedObjectFactory<Task> handlerFactory, InetSocketAddress address, ExecutorService executor) throws IOException {
        this(handlerFactory, MODE_SERVER);
        _address = address;
        _server = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(executor));

        acceptFactory = new Pool<>(new AcceptedHandler());
        readFactory = new Pool<>(new ReceivedHandler());
        writeFactory = new Pool<>(new SentHandler());
        //setOption(StandardSocketOptions.SO_RCVBUF,16*1024);
    }

    public AioConnection(CachedObjectFactory<Task> handlerFactory, AsynchronousSocketChannel channel) {
        this(handlerFactory, MODE_CONNECTOR);
        _client = channel;

        /*setOption(StandardSocketOptions.TCP_NODELAY,true);
         setOption(StandardSocketOptions.SO_REUSEADDR,true);
         setOption(StandardSocketOptions.SO_KEEPALIVE,false);*/
    }

    @Override
    public <T> AioConnection setOption(SocketOption<T> name, T value) throws IOException {
        if (MODE_SERVER == _mode) {
            _server.setOption(name, value);
        } else if (MODE_CONNECTOR == _mode) {
            _client.setOption(name, value);
        }
        return this;
    }

    @Override
    public void bind(int backlog) throws IOException {
        if (_isBind) {
            return;
        }
        _backlog = backlog;
        _server.bind(_address, _backlog);
        _isBind = true;
    }

    @Override
    public void accept(Task msg) {
        if (!this.isAcceptable()) {
            throw new IllegalStateException("当前状态下不支持该操作");
        }
        AcceptedHandler<AsynchronousSocketChannel> callback = acceptFactory.get();
        callback.bind(this, null);
        _server.accept(msg, callback);
    }

    @Override
    public void close(Task task) {
        if (readHandle.acquire(this) && writeHandle.acquire(this)) {
            try {
                this._client.close();
            } catch (IOException ignored) {
            }
            if (task != null) {
                task.fire(this, Task.EVENT_CLOSED, this, null, null, 0);
            }
        }
    }

    @Override
    public Connection createConnection(NetworkChannel channel) {

        AioConnection conn = new AioConnection(null, (AsynchronousSocketChannel) channel);
        conn.acceptFactory = this.acceptFactory;
        conn.readFactory = this.readFactory;
        conn.writeFactory = this.writeFactory;
        return conn;
    }

    @Override
    protected void readImpl(Task msg) {
        //读取并写入文件
        if (msg.operation() == Task.OP_TRANS) {
            if (_proxy == null) {
                _proxy = new TransferProxy(this);
            }

            AioConnection me = this;
            new Thread(new Runnable() { //TODO:线程池

                @Override
                public void run() {
                    long result = 0;
                    try {
                        result = msg.channel().transferFrom(_proxy, msg.position(), msg.length());
                    } catch (IOException ex) {
                        //readPending.set(false);
                        msg.fire(this, Task.EVENT_ERROR, me, null, ex, result);
                        return;
                    }
                    //readPending.set(false);
                    msg.fire(this, Task.EVENT_READ, me, null, null, result);
                }

            }).start();
        } else {
            ReceivedHandler handler = new ReceivedHandler();
            handler.bind(this, null);
            _client.read(msg.buffer(), msg.timeout(), TimeUnit.MILLISECONDS, msg, handler);
        }
    }

    @Override
    protected void writeImpl(Task msg) {
        //传输文件
        if (msg.operation() == Task.OP_TRANS) {
            if (_proxy == null) {
                _proxy = new TransferProxy(this);
            }
            AioConnection me = this;
            new Thread(new Runnable() { //TODO:线程池

                @Override
                public void run() {
                    long result = 0;
                    try {
                        result = msg.channel().transferTo(msg.position(), msg.length(), _proxy);
                    } catch (IOException ex) {
                        //writeLock.unlockWrite(msg.stamp);
                        //writePending.set(false);

                        msg.fire(this, Task.EVENT_ERROR, me, null, ex, result);
                        return;
                    }
                    //writeLock.unlockWrite(msg.stamp);
                    //writePending.set(false);
                    msg.fire(this, Task.EVENT_WRITTEN, me, null, null, result);
                }

            }).start();
        } else {
            SentHandler handler = new SentHandler();
            handler.bind(this, null);
            if (!this.connected()) {
                msg.fire(this, Task.EVENT_ERROR, this, null, new IOException("conn was closed"), 0);
            } else {
                _client.write(msg.buffer(), msg.timeout(), TimeUnit.MILLISECONDS, msg, handler);
            }
        }
    }

    @Override
    public boolean connected() {
        if (_mode == MODE_SERVER) {
            return _server.isOpen();
        }
        return _client.isOpen();
    }

    TransferProxy _proxy;

    @Override
    public boolean isAcceptable() {
        if (_mode != MODE_SERVER) {
            return false;
        }

        return true;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        if (_mode != MODE_SERVER) {
            return this._client.getLocalAddress();
        }
        return this._server.getLocalAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        if (_mode != MODE_SERVER) {
            return this._client.getRemoteAddress();
        }
        return null;
    }

    private class TransferProxy implements ReadableByteChannel, WritableByteChannel {

        AioConnection _conn;

        public TransferProxy(AioConnection conn) {
            _conn = conn;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {

            try {
                Future<Integer> result = _client.read(dst);
                return result.get(1000 * 30, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }

        @Override
        public boolean isOpen() {
            return _conn.connected();
        }

        @Override
        public void close() throws IOException {
            _conn._client.close();
        }

        @Override
        public int write(ByteBuffer src) throws IOException {

            try {
                Future<Integer> result = _client.write(src);
                return result.get(1000 * 30, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }

    }
}

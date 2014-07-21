/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.InvalidOperationException;
import mano.util.ObjectFactory;
import mano.util.SafeHandle;

/**
 * 表示一个网络(socket)连接。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Connection {

    private volatile long time;

    protected synchronized void update() {
        time = Calendar.getInstance().getTime().getTime();
    }

    public boolean vod() {
        if (Calendar.getInstance().getTime().getTime() - time > 300000) {
            return false;
        }
        return true;
    }

    protected abstract void closeImpl(Task task);

    public synchronized void close(boolean forcibly, Task task) throws InvalidOperationException {
        if (forcibly) {
            closeImpl(task != null ? task.close() : null);
        } else {
            if (task == null) {
                throw new IllegalArgumentException("task");
            }
            this._writeQueued.offer(task.close());
            this.flush();
        }
    }

    public void close(Task task) throws InvalidOperationException {
        close(false, task);
    }

    public abstract SocketAddress getLocalAddress() throws IOException;

    public abstract SocketAddress getRemoteAddress() throws IOException;

    public abstract boolean isConnected();

    /**
     * 创建一个新的客户连接。
     *
     * @param channel
     * @return
     */
    public abstract Connection createConnection(NetworkChannel channel);

    /**
     * 绑定地址到基础连接
     *
     * @param backlog
     * @throws IOException
     */
    public abstract void bind(int backlog) throws IOException;

    /**
     * 设置基础连接的选项。
     *
     * @param name
     * @param value
     * @return
     * @throws IOException
     */
    public abstract <T> Connection setOption(SocketOption<T> name, T value) throws IOException;

    /**
     * 异步接受连接。
     *
     * @param msg
     */
    public abstract void accept(Task msg);

    protected abstract void readImpl(Task msg) throws ReadPendingException;

    final SafeHandle writeHandle = new SafeHandle();
    final SafeHandle readHandle = new SafeHandle();

    /**
     * 从连接中异步读取数据。如果成功将任务添加到队列则返回 true，否则 false.
     *
     * @param task
     * @return
     */
    public synchronized boolean read(Task task) {
        if (task == null) {
            throw new NullPointerException();
        } else if (!this.isConnected()) {
            return false;
        } else if (task.tryLock(readHandle)) {
            try {
                this.readImpl(task);
                return true;
            } catch (ReadPendingException e) {
                readHandle.release(task);
                return false;
            }
        }
        return false;
    }

    protected abstract void writeImpl(Task msg) throws WritePendingException;

    /**
     * 将数据异步写入到连接中。如果成功将任务添加到队列则返回 true，否则 false.
     *
     * @param task
     * @return
     */
    public synchronized void write(Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }
        _writeQueued.offer(task);
        flush();
    }

    final Queue<Task> _writeQueued = new LinkedBlockingQueue<>();

    public synchronized boolean hasWriteTaskQueued() {
        return !_writeQueued.isEmpty();
    }

    /**
     * 检查并执行写入队列中的任务。
     */
    public synchronized void flush(Task task) {
        _writeQueued.offer(task.flush());
        this.flush();
    }

    public synchronized void flush() {
        if (_writeQueued.isEmpty()) {
            return;
        }

        Task task = _writeQueued.peek();
        if (task != null && task.tryLock(writeHandle)) {
            if (task.operation() == Task.OP_FLUSH) {
                _writeQueued.poll();
                task.fire(this, Task.EVENT_FLUSH, this, null, null, 0);
                task = null;
            } else if (task.operation() == Task.OP_CLOSE) {
                _writeQueued.poll();
                this.closeImpl(task);
                task = null;
            } else {
                try {
                    writeImpl(task);
                    _writeQueued.poll();
                    task = null;
                } catch (WritePendingException ex) {
                    //ignored errors
                    writeHandle.release(task);
                }
            }
        }
        if (task != null) { //重复处理上步处理失败的任务
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                //igored
                return;
            }
            this.flush();
        }
    }

    public abstract boolean isAcceptable();

    protected class AcceptedHandler<T extends NetworkChannel> implements CompletionHandler<T, Task>, ObjectFactory<AcceptedHandler<T>> {

        Connection _conn;
        Object _attachment;

        public AcceptedHandler bind(Connection conn, Object attachment) {
            _conn = conn;
            _attachment = attachment;
            return this;
        }

        @Override
        public void completed(T result, Task msg) {
            msg.fire(this, Task.EVENT_ACCEPTED, _conn, _conn.createConnection((NetworkChannel) result), null, 0);
            msg.dispose();
        }

        @Override
        public void failed(Throwable exc, Task msg) {
            msg.fire(this, Task.EVENT_ERROR, _conn, null, exc, 0);
            msg.dispose();
        }

        @Override
        public AcceptedHandler<T> create() {
            return new AcceptedHandler<>();
        }

    }

    protected class ReceivedHandler implements CompletionHandler<Integer, Task>, ObjectFactory<ReceivedHandler> {

        Connection _conn;
        Object _attachment;

        public ReceivedHandler bind(Connection conn, Object attachment) {
            _conn = conn;
            _attachment = attachment;
            return this;
        }

        @Override
        public void completed(Integer count, Task msg) {
            if (count < 0) {
                _conn.close(msg);
                return;
            } else if (count == 0) {
                _conn.readImpl(msg);
                return;
            }
            msg.fire(this, Task.EVENT_READ, _conn, null, null, count);
            msg.dispose();
        }

        @Override
        public void failed(Throwable exc, Task msg) {
            msg.fire(this, Task.EVENT_ERROR, _conn, null, exc, 0);
            msg.dispose();
        }

        @Override
        public ReceivedHandler create() {
            return new ReceivedHandler();
        }
    }

    protected class SentHandler implements CompletionHandler<Integer, Task>, ObjectFactory<SentHandler> {

        Connection _conn;
        Object _attachment;

        public SentHandler bind(Connection conn, Object attachment) {
            _conn = conn;
            _attachment = attachment;
            return this;
        }

        @Override
        public void completed(Integer count, Task task) {
            if (count < 0) {
                _conn.close(task);
                return;
            } else if (task.buffer().hasRemaining()) {
                _conn.writeImpl(task);
                return;
            }
            task.fire(this, Task.EVENT_WRITTEN, _conn, null, null, count);
            task.dispose();
        }

        @Override
        public void failed(Throwable exc, Task msg) {
            msg.fire(this, Task.EVENT_ERROR, _conn, null, exc, 0);
            msg.dispose();
        }

        @Override
        public SentHandler create() {
            return new SentHandler();
        }

    }
}

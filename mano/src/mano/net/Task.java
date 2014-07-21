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
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.Disposable;
import mano.Resettable;
import mano.util.SafeHandle;
import mano.util.ThreadPool;

/**
 * 封装了一个包含处理IO操作的参数集及结果处理程序的抽象类。 该类是非线程安全的。
 *
 * @author jun <jun@diosay.com>
 *
 */
public abstract class Task implements Resettable, Disposable, Callable<Integer> {

    public final static int EVENT_ACCEPTED = 1;
    public final static int EVENT_READ = 2;
    public final static int EVENT_WRITTEN = 4;
    public final static int EVENT_ERROR = 8;
    public final static int EVENT_CLOSED = 16;
    public final static int EVENT_FLUSH = 32;
    public final static int OP_BUFFER = 64;
    public final static int OP_TRANS = 128;
    public final static int OP_FLUSH = 256;
    public final static int OP_CLOSE = 512;
    static int TIMEOUT = 1000 * 5;
    private volatile Connection _conn;
    private volatile Connection _accept;
    private volatile Throwable _lastException;
    private volatile ByteBuffer _buffer;
    private volatile long _bytesTransferred;
    private volatile Object _attachment;
    private volatile long _timeout = TIMEOUT;//5s
    private volatile FileChannel _channel;
    private volatile int _op;
    private volatile long _pos;
    private volatile long _len;
    private SafeHandle _handle;

    /**
     * 获取或设置一个值，以指示是在事件处理完成后调用 dispose 方法。
     */
    protected volatile boolean cancelDisposing = false;

    /**
     * 设置要用于异步传输的数据缓冲区。
     *
     * @param buffer
     * @return
     */
    public final Task buffer(ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer is required");
        }
        _op = OP_BUFFER;
        _buffer = buffer;
        return this;
    }

    public final ByteBuffer buffer() {
        return _buffer;
    }

    /**
     * 设置要用于异步传输的文件通道。
     *
     * @param chan
     * @param position
     * @param length
     * @return
     */
    public final Task channel(FileChannel chan, long position, long length) {
        if (chan == null) {
            throw new NullPointerException("chan is required");
        }
        _op = OP_TRANS;
        _channel = chan;
        _pos = position;
        _len = length;
        return this;
    }

    /**
     * 获取文件传输通道。
     *
     * @return
     */
    public final FileChannel channel() {
        return _channel;
    }

    /**
     * 获取限定文件传输的位置。
     *
     * @return
     */
    public final long position() {
        return _pos;
    }

    /**
     * 获取限定文件传输的长度。
     *
     * @return
     */
    public final long length() {
        return _len;
    }

    /**
     * 获取在连接操作中传输的字节数。
     *
     * @return
     */
    public final long bytesTransferred() {
        return _bytesTransferred;
    }

    public final Object attachment() {
        return _attachment;
    }

    public final Task attach(Object attachment) {
        _attachment = attachment;
        return this;
    }

    /**
     * 获取一个值，用于指示操作的超时时长。
     *
     * @return
     */
    public final long timeout() {
        return _timeout;
    }

    /**
     * 设置超时时间
     *
     * @param mills
     * @return
     */
    public final Task timeout(long mills) {
        _timeout = mills;
        return this;
    }

    /**
     * 获取用于当前操作的连接 。
     *
     * @return NetConnection
     */
    public final Connection connect() {
        return _conn;
    }

    /**
     * 获取异步方法已接受的连接。
     *
     * @return NetConnection
     */
    public final Connection accept() {
        return _accept;
    }

    /**
     * 获取发生的错误信息。
     *
     * @return
     */
    public final Throwable error() {
        return _lastException;
    }

    public final int operation() {
        return _op;
    }

    final Task flush() {
        _op = Task.OP_FLUSH;
        return this;
    }

    final Task close() {
        _op = Task.OP_CLOSE;
        return this;
    }

    /**
     * 在当前对象上附加一个自动锁，当回调时自动解锁。
     *
     * @param handle
     * @return
     */
    final boolean tryLock(SafeHandle handle) {
        if (handle == null) {
            throw new NullPointerException();
        }
        /*if(_handle!=null){
         throw new IllegalArgumentException("当前实例中已经包含一个安全锁。");
         }*/
        if (handle.tryAcquire(this)) {
            _handle = handle;
            return true;
        }
        return false;
    }

    /**
     * 在当前对象上附加一个自动锁，当回调时自动解锁。
     *
     * @param handle
     * @return
     */
    final boolean lock(SafeHandle handle) {
        if (handle == null) {
            throw new NullPointerException();
        }
        /*if(_handle!=null){
         throw new IllegalArgumentException("当前实例中已经包含一个安全锁。");
         }*/
        if (handle.acquire(this)) {
            _handle = handle;
            return true;
        }
        return false;
    }

    @Override
    public final void reset() {

        this._attachment = null;
        this._buffer = null;
        this._bytesTransferred = 0;
        this._lastException = null;
        this._conn = null;
        this._accept = null;
        this._op = 0;
        try {
            this._channel.close();
        } catch (Exception ex) {
            //
        }
        this._channel = null;
        try {
            this._handle.release(this);
            
        } catch (Exception ex) {
            //
        }
        this._handle = null;
        this._pos = 0;
        this._len = 0;
        this._timeout = TIMEOUT;//5s
    }

    /**
     * 清理使用过的资源。
     */
    @Override
    public void dispose() {
        this.reset();
    }
    int opt;

    final synchronized void fire(Object sender, int eventOps, Connection conn, Connection accept, Throwable e, long bytesTransferred) {

        this._bytesTransferred = bytesTransferred;
        this._lastException = e;
        this._conn = conn;
        this._accept = accept;
        opt = eventOps;
        if (_handle != null) {
            _handle.release(this);
        }
        this.callr();
        //this.dispose();
        //ThreadPool.submit(this);
        /*Future<Task> f = ThreadPool.submit(this);
         try {
         f.get().dispose();
         } catch (Exception ex) {
         this.dispose();
         ex.printStackTrace();
         }*/

    }

    public Task callr() {
        switch (this.opt) {
            case EVENT_ACCEPTED:
                this.onAccepted();
                break;
            case EVENT_READ:
                this.onRead();
                break;
            case EVENT_WRITTEN:
                this.onWriten();
                break;
            case EVENT_ERROR:
                this.onFailed();
                break;
            case EVENT_CLOSED:
                this.onClosed();
                break;
            case EVENT_FLUSH:
                this.onFlush();
                break;
        }
        return this;
    }

    @Override
    public Integer call() {

        //GenericObjectPool pool;
        this.dispose();
        return 0;
    }

    /**
     * 当已接受客户端连接时调用。
     */
    protected abstract void onAccepted();

    /**
     * 当读取数据完成时调用。
     */
    protected abstract void onRead();

    /**
     * 当写入数据完成时调用。
     */
    protected abstract void onWriten();

    /**
     * 当失败时调用。
     */
    protected abstract void onFailed();

    /**
     * 当连接关闭时调用。
     */
    protected abstract void onClosed();

    protected abstract void onFlush();
}

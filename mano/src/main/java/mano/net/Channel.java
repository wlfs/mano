/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import mano.Disposable;
import mano.Resettable;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface Channel extends Resettable,Disposable {

    /**
     * 获取用于读操作(<code>read()</code>)的缓冲区。
     */
    ByteArrayBuffer getBuffer();

    /**
     * 从通道中异步读取数据。
     * @param handler
     * @param attachment 
     */
    void read(ChannelHandler handler, Object attachment);
    
    void callHandler(ChannelHandler handler, Object attachment);

    /**
     * 将指定缓冲区放入写入队列。
     *
     * @param buffer 要写入的缓冲区对象。注意，调用该方法后该对象将会被回收。
     */
    void write(Buffer buffer);

    /**
     * 关闭通道。
     *
     * @param force true 表示强制关闭不管是否有未完成的任务。
     */
    void close(boolean force);
    
    void onFailed(Object sender,Throwable exc);
    
    void onClosed();
}

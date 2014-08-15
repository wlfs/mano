/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.InvalidOperationException;
import mano.Resettable;

/**
 * 表示一个 HTTP 处理程序。
 * <p>
 * 该类是非线程安全的。</p>
 *
 * @author jun <jun@diosay.com>
 */
public abstract class HttpHandler<T extends HttpContext> implements Runnable,Resettable {

    private HttpHandler next;
    private T context;

    @Override
    public synchronized final void run() {
        if (context == null) {
            throw new IllegalArgumentException("This Handler is not initialized.");
        }
        try {
            this.next = this.process(context);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 处理 HTTP 操作。
     *
     * @param context 当前要处理的 HTTP 上下文。
     * @return 返回下个 HTTP 处理程序，如果整个 HTTP 已处理完成，则必须返回 null。
     */
    protected abstract HttpHandler process(T context) throws Exception;

    /**
     * 设置将要处理的 HTTP 上下文。
     *
     * @param context
     */
    public void init(T context) {
        if (context == null) {
            throw new IllegalArgumentException("context");
        }else if (this.context != null) {
            throw new IllegalArgumentException("This Handler was alread initialized.");
        }
        this.context = context;
    }

    /**
     * 获取已设置的处理程序。 如果为 null 则表示流程处理结束。
     *
     * @return
     */
    public final HttpHandler getHandler() {
        return this.next;
    }

    @Override
    public final void reset() {
        this.next = null;
        this.context = null;
    }
}

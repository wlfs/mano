/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import mano.Action;
import mano.InvalidOperationException;
import mano.util.ThreadPool;

/**
 * 解决系统服务之间的通讯。
 * @author jun <jun@diosay.com>
 */
public final class Intent implements Callable<Intent> {

    private final HashMap<String, Object> extras;
    private volatile transient String serviceName;
    private volatile transient String action;
    //private int timeout;
    private volatile transient Future<Intent> result;
    private volatile transient Action callback;
    private volatile transient boolean submited;
    private volatile transient Throwable error;

    public Intent(String serviceName, String action) {
        this.serviceName = serviceName;
        this.action = action;
        this.extras = new HashMap<>();
    }
    
    public static Intent create(String serviceName, String action) {
        Intent intent = new Intent(serviceName, action);
        return intent;
    }

    /**
     * 设置用于通信双方的参数。
     * @param key
     * @param value 
     */
    public final void set(String key, Object value) {
        extras.put(key, value);
    }

    public final Object get(String key) {
        return extras.get(key);
    }

    /**
     * 获取扩展参数的键集合。
     * @return 
     */
    public final Iterable<String> getKeys() {
        return extras.keySet();
    }

    /**
     * 获取目标服务的名称。
     * @return 
     */
    public final String getServiceName() {
        return serviceName;
    }

    /**
     * 获取目标服务调用的action。
     * @return 
     */
    public final String getAction() {
        return action;
    }
    
    /**
     * 获取在执行过程中的异常。
     * @return 
     */
    public Throwable getException(){
        return this.error;
    }

    /**
     * 获取此 Intent 实例是否由于被取消的原因而已完成执行。
     *
     * @return
     */
    public boolean isCancelled() {
        return result.isCancelled();
    }

    /**
     * 获取 Intent 是否由于未经处理异常的原因而完成。
     *
     * @return
     */
    public boolean isFaulted() {
        return error != null;
    }

    /**
     * 获取此 Intent 是否已完成。
     *
     * @return
     */
    public synchronized boolean isDone() {
        return result.isDone();
    }

    /**
     * 等待远程服务执行完成。
     * @return
     * @throws InterruptedException
     * @throws ExecutionException 
     */
    public synchronized Intent waitForDone() throws InterruptedException, ExecutionException {
        if (!submited) {
            this.submit();
        }
        return result.get();
    }

    /**
     * 提交到服务总线。
     * @return 
     */
    public synchronized final Intent submit() {
        return this.submit(null);
    }

    /**
     * 设置一个回调，并提交到服务总线。
     * @param action
     * @return 
     */
    public synchronized final Intent submit(Action<Intent> action) {
        if (submited || result != null) {
            throw new InvalidOperationException("This Intent has been already submit.");
        }
        callback = action;
        result = ThreadPool.submit(this);
        submited = true;
        return this;
    }

    /**
     * 内部方法。执行服务 Action.
     * @return
     * @throws Exception 
     */
    @Override
    public final Intent call() throws Exception {
        
        
        //URI uri=new URI("uri","service","action","null");
        //uri://service/action
        
        try {
            Service service = ServiceManager.getInstance().getService(this.getServiceName());
            if (service == null) {
                throw new UnsupportedOperationException("This service is undefined:" + this.getServiceName());
            } else {
                service.process(this);
            }
        } catch (Throwable ex) {
            this.error = ex;
            if (callback != null) {
                callback.run(this);
            }
            throw ex;
        }
        if (callback != null) {
            callback.run(this);
        }
        return this;
    }
}

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import mano.util.ThreadPool;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Intent implements Callable<Intent> {

    private final HashMap<String, Object> extras;
    private String serviceName;
    private String action;
    public int timeout;
    private final Object locker;
    private boolean done;
    private Future<Intent> result;

    public Intent(String serviceName, String action) {
        this.serviceName = serviceName;
        this.action = action;
        this.extras = new HashMap<>();
        this.locker = new Object();
    }

    public final void set(String key, Object value) {
        extras.put(key, value);
    }

    public final Object get(String key) {
        return extras.get(key);
    }

    public final Iterable<String> getKeys() {
        return extras.keySet();
    }

    public final String getServiceName() {
        return serviceName;
    }

    public final String getAction() {
        return action;
    }

    protected void onCompleted() {

    }

    protected void onFailed(Throwable ex) {

    }

    public synchronized boolean isDone() {
        return result.isDone();
    }

    public Intent waitForDone() throws InterruptedException, ExecutionException {
        return result.get();
    }

    public final Intent submit() {
        result = ThreadPool.submit(this);
        return this;
    }

    @Override
    public final Intent call() throws Exception {

        Service service = ServiceManager.getInstance().getService(this.getServiceName());

        if (service == null) {
            this.onFailed(new UnsupportedOperationException("This Action is undefined:" + this.getAction()));
        } else {
            try {
                service.process(this);
            } catch (Throwable ex) {
                this.onFailed(ex);
                return this;
            }
            
            this.onCompleted();
        }

        return this;
    }
}

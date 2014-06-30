/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author jun <jun@diosay.com>
 * @param <T>
 */
public class Pool<T> {

    private final Queue<T> items = new LinkedBlockingQueue<>();
    private ObjectFactory<T> _factory;
    
    public Pool(){}
    
    public Pool(ObjectFactory<T> factory){
        _factory=factory;
    }
    
    protected T create(){
        if(_factory==null){
            throw new IllegalArgumentException();
        }
        return _factory.create();
    }

    public synchronized T get() {
        T result = items.poll();
        if (result == null) {
            result = create();
        }
        return result;
    }

    public synchronized void put(T item) {
        if (item == null) {
            return;
        }
        items.offer(item);
    }

    public synchronized void clear() {
        items.clear();
    }
}

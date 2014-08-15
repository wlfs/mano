/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class CachedObjectPool<T> extends Pool<T> {

    private int min;
    private int max;
    
    public CachedObjectPool(ObjectFactory<T> factory,int min,int max) {
        super(factory);
        this.max=max;
        this.min=min;
    }
    
    @Override
    public synchronized void put(T item) {
        if(this.count()>max){
            return;
        }
        super.put(item);
    }

    @Override
    public synchronized T get() {
        if(this.count()<min){
            return this.create();
        }
        return super.get();
    }
}

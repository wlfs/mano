/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.Resettable;

/**
 *
 * @author jun <jun@diosay.com>
 * @param <T>
 */
public class Pool<T> {

    private final Queue<T> items = new LinkedBlockingQueue<>();
    private ObjectFactory<T> _factory;
    private int count;
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
        }else{
            count--;
        }
        return result;
    }

    public synchronized void put(T item) {
        if (item == null) {
            return;
        }
        count++;
        if(item instanceof Resettable){
            ((Resettable)item).reset();
        }
        items.offer(item);
    }
    
    public int count(){
        return count;
    }

    public synchronized void clear() {
        count=0;
        items.clear();
    }
    
    
    public static void main(String...args){
        ReferenceQueue rq=new ReferenceQueue();
        PhantomReference wr=new PhantomReference("abc",rq);
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            
        }
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            
        }System.gc();
        System.out.println(wr.get());
        System.out.println(rq.poll());
    } 
    
}

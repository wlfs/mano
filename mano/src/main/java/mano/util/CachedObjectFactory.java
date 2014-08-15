/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import mano.Activator;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class CachedObjectFactory<T> extends Pool<T> implements ObjectFactory<T> {

    ReferenceQueue<T> queued = new ReferenceQueue<>();

    @Override
    public synchronized void clear() {

    }

    @Override
    public synchronized void put(T item) {

    }

    @Override
    public synchronized T get() {
        T result;
        Reference<? extends T> ref = queued.poll();
        if (ref == null) {
            create();
            ref = queued.poll();
        }
        if (ref == null) {
            return null;
        }
        result = ref.get();
        return result; //To change body of generated methods, choose Tools | Templates.
    }

    private Activator loader;
    private Class<?> insClazz;
    private Object[] ctorArgs;
    ObjectFactory<T> _factory;

    public CachedObjectFactory(ObjectFactory<T> factory) {
        this._factory = factory;
    }

    public CachedObjectFactory(Class<? extends T> clazz, Object... args) {
        this(new Activator(), clazz, args);
    }

    public CachedObjectFactory(Activator activator, Class<? extends T> clazz, Object... args) {
        this.loader = activator;
        this.insClazz = clazz;
        this.ctorArgs = args;
    }

    public CachedObjectFactory(Activator activator, String classFullname, Object... args) throws ClassNotFoundException {
        this(activator, (Class<? extends T>) activator.getClass(classFullname), args);
    }

    @Override
    public T create() {

        T result;

        if (this._factory != null) {
            result = _factory.create();
        } else {
            try {
                result = (T) this.loader.newInstance(this.insClazz, this.ctorArgs);
            } catch (InstantiationException ex) {
                return null;
            }
        }
        WeakReference<T> ref = new WeakReference<>(result, queued);
        ref.enqueue();
        //result = ref.get();
        //ref.clear();
        return null;
        //return result;
    }

}

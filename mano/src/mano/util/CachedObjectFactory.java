/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import mano.Activator;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class CachedObjectFactory<T> extends Pool<T> implements ObjectFactory<T> {
    
    private Activator loader;
    private Class<?> insClazz;
    private Object[] ctorArgs;
    ObjectFactory<T> _factory;
    public CachedObjectFactory(ObjectFactory<T> factory){
        _factory=factory;
    }
    
    public CachedObjectFactory(Class<? extends T> clazz,Object...args){
        this(new Activator(),clazz,args);
    }
    
    public CachedObjectFactory(Activator activator,Class<? extends T> clazz,Object...args){
        this.loader=activator;
        this.insClazz=clazz;
        this.ctorArgs=args;
        this._factory=this;
    }
    
    public CachedObjectFactory(Activator activator,String classFullname,Object...args) throws ClassNotFoundException{
        this(activator,(Class<? extends T>)activator.getClass(classFullname),args);
    }
    
    @Override
    public T create() {
        try {
            return (T)this.loader.newInstance(this.insClazz, this.ctorArgs);
        } catch (InstantiationException ex) {
            return null;
        }
    }
    
}

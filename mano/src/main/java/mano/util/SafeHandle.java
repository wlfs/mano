/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.concurrent.locks.StampedLock;

/**
 * 包装了一个排他锁的句柄。 
 * @author jun <jun@diosay.com>
 */
public class SafeHandle {
    protected final StampedLock locker = new StampedLock();
    private Object _state;
    private long _stamp;
    
    /**
     * 获取对象锁。
     * @param state
     * @return 
     */
    public synchronized boolean acquire(Object state){
        if(null==state){
            throw new NullPointerException();
        }
        
        if(null !=this._state && state.equals(this._state)){
            return true;
        }
        else if(null !=this._state){
            return false;
        }
        long stamp=locker.writeLock();
        if(stamp==0){
            return false;
        }
        this._state=state;
        this._stamp=stamp;
        return true;
    }
    
    /**
     * 尝试获取对象锁。
     * @param state
     * @return 
     */
    public synchronized boolean tryAcquire(Object state){
        if(null==state){
            throw new NullPointerException();
        }
        
        if(null !=this._state && state.equals(this._state)){
            return true;
        }
        else if(null !=this._state){
            return false;
        }
        long stamp=locker.tryWriteLock();
        if(stamp==0){
            return false;
        }
        this._state=state;
        this._stamp=stamp;
        return true;
    }
    
    /**
     * 释放对象锁。
     * @param state
     * @return 
     */
    public synchronized boolean release(Object state){
        if(null==state){
            throw new NullPointerException();
        }
        if(state.equals(this._state)){
            this.locker.unlockWrite(_stamp);
            this._state=null;
            return true;
        }
        return false;
    }
    
    
}

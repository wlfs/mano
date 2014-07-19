/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

/**
 * 表示一个缓存项。
 * @author jun <jun@diosay.com>
 */
public interface CacheEntry {

    String getKey();

    Object getValue();
    
    long getLastVisited();
    
    long getTimeout();
    
    boolean canUpdate();
    
    boolean isExpired();
}

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javafx.util.Callback;
import mano.InvalidOperationException;

/**
 * 通过维护一个 HashMap 来实现的简单缓存提供程序。
 * @author jun <jun@diosay.com>
 */
public class HashCacheProvider implements CacheProvider {

    Map<String, ItemEntry> entries = new HashMap<>();

    @Override
    public void set(String key, Object value, long timeout, boolean update, Callback<CacheEntry, Object> callback) throws InvalidOperationException {
        ItemEntry entry;
        if (entries.containsKey(key)) {
            entry = entries.get(key);
        } else {
            entry = new ItemEntry();
            entry.key = key;
            entries.put(key, entry);
        }

        entry.callback = callback;
        entry.timeout = timeout;
        entry.value = value;
        entry.canUpdate = update;

    }

    @Override
    public CacheEntry get(String key) {
        ItemEntry entry = null;
        if (entries.containsKey(key)) {
            entry = entries.get(key);
        }

        if (entry != null) {
            if (entry.isExpired()) {
                this.remove(key);
                
                //call
                entry = null;
            }
        }

        return entry;
    }

    @Override
    public void remove(String key) {
        entries.remove(key);
    }

    @Override
    public void flush() {
        
    }

    class ItemEntry implements CacheEntry, Entry<String, Object> {

        String key;
        Object value;
        long timeout;
        long visited;
        boolean canUpdate;
        Callback<CacheEntry, Object> callback;

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public long getLastVisited() {
            return visited;
        }

        @Override
        public long getTimeout() {
            return timeout;
        }

        @Override
        public boolean canUpdate() {
            return canUpdate;
        }

        @Override
        public boolean isExpired() {

            return 0 - visited > timeout;
        }

        @Override
        public Object setValue(Object value) {
            this.value = value;
            return value;
        }

    }
}

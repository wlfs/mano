/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javafx.util.Callback;
import mano.InvalidOperationException;

/**
 * 通过维护一个 HashMap 来实现的简单缓存提供程序。
 *
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
        entry.visited = Instant.now().toEpochMilli();
        entry.timeout = timeout;
        entry.value = value;
        entry.canUpdate = update;

    }

    @Override
    public CacheEntry get(String key) {
        ItemEntry entry = null;
        if (entries.containsKey(key)) {
            entry = entries.get(key);
            if (entry != null && entry.isExpired()) {
                this.remove(key);
                entry = null;
            } else if (entry != null) {
                entry.visited = Instant.now().toEpochMilli();
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

    @Override
    public Object get(String key, String index) {
        CacheEntry entry = this.get(key);
        if (entry == null || entry.getValue() == null || !(entry.getValue() instanceof Map)) {
            return null;
        }
        Map map = (Map) entry.getValue();
        if (map.containsKey(index)) {
            return map.get(index);
        }
        return null;
    }

    @Override
    public boolean contains(String key) {
        this.get(key);//验证是否过期。
        return entries.containsKey(key);
    }

    @Override
    public boolean set(String key, String index, Object value) {
        CacheEntry entry = this.get(key);
        if (entry == null || entry.getValue() == null || !(entry.getValue() instanceof Map)) {
            return false;
        }
        Map map = (Map) entry.getValue();
        map.put(index, value);
        return true;
    }

    @Override
    public void remove(String key, String index) {
        CacheEntry entry = this.get(key);
        if (entry == null || entry.getValue() == null || !(entry.getValue() instanceof Map)) {
            return;
        }
        Map map = (Map) entry.getValue();
        map.remove(index);
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
            //return false;
            return Instant.now().toEpochMilli() - visited > timeout;
        }

        @Override
        public Object setValue(Object value) {
            this.value = value;
            return value;
        }

    }
}

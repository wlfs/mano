/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jun <jun@diosay.com>
 * @param <T>
 */
public class NameValueCollection<T> implements Map<String, T> {

    private class ItemEntry implements Entry<String, T> {

        private final String _key;
        private T _value;

        public ItemEntry(String key, T value) {
            _key = key;
            _value = value;
        }

        @Override
        public String getKey() {
            return _key;
        }

        @Override
        public T getValue() {
            return _value;
        }

        @Override
        public T setValue(T value) {
            _value = value;
            return value;
        }
    }

    
    public Entry<String, T> newEntry(String key, T value){
        return new ItemEntry(key,value);
    }
    
    protected HashMap<String, T> map;
    protected final Map<String, Entry<String, T>> lookup;

    public NameValueCollection() {
        lookup = new LinkedHashMap<>();
    }

    @Override
    public void clear() {
        lookup.clear();
    }

    private Map<String, T> getMap() {
        if (map == null) {
            map = new HashMap<>();
            lookup.values().stream().forEach((entry) -> {
                map.put(entry.getKey(), entry.getValue());
            });
        }
        return map;
    }

    @Override
    public boolean containsKey(Object key) {
        return lookup.containsKey(key.toString().toLowerCase());
    }

    @Override
    public boolean containsValue(Object value) {
        return lookup.containsValue(value);
    }

    @Override
    public Set<Entry<String, T>> entrySet() {
        return getMap().entrySet();
    }

    @Override
    public T get(Object key) {
        key = key.toString().toLowerCase();
        if (!lookup.containsKey(key)) {
            return null;
        }
        return lookup.get(key).getValue();
    }

    @Override
    public boolean isEmpty() {
        return lookup.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return getMap().keySet();
    }

    @Override
    public T put(String key, T value) {
        map = null;
        lookup.put(key.toLowerCase(), newEntry(key, value));
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> items) {
        map = null;
        items.entrySet().stream().forEach((entry) -> {
            lookup.put(entry.getKey().toLowerCase(), newEntry(entry.getKey(), entry.getValue()));
        });
    }

    @Override
    public T remove(Object key) {
        map = null;
        return lookup.remove(key.toString().toLowerCase()).getValue();
    }

    @Override
    public int size() {
        return lookup.size();
    }

    @Override
    public Collection<T> values() {
        return getMap().values();
    }

}

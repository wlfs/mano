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
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class LinkedMap<K, V> implements Map<K, LinkedMap.LinkedNode<K, V>> {

    LinkedNode<K, V> firstNode;
    LinkedNode<K, V> lastNode;
    LinkedNode<K, V> currentNode;
    Map<K, LinkedNode<K, V>> map;

    public LinkedMap() {
        map = new HashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public void clear() {
        map.clear();
        firstNode = null;
        lastNode = null;
    }

    public void addFrist(K key, V value) {
        LinkedNode<K, V> node = new LinkedNode<>(key, value);
        if (firstNode != null) {
            node.next = firstNode;
            firstNode.prev = node;
            if (lastNode == null) {
                lastNode = firstNode;
                lastNode.next=null;
            }
        }
        firstNode = node;
        firstNode.prev=null;
        if (lastNode == null) {
            lastNode = firstNode;
            lastNode.next=null;
        }
        map.put(key, node);
    }

    public void addLast(K key, V value) {
        LinkedNode<K, V> node = new LinkedNode<>(key, value);
        if (lastNode != null) {
            node.prev = lastNode;
            lastNode.next=node;
        }
        lastNode = node;
        node.next = null;
        if (firstNode == null) {
            firstNode = lastNode;
            firstNode.prev=null;
        }
        
        map.put(key, node);
    }
    
    public LinkedNode<K, V> getFirstNode(){
        return firstNode;
    }
    public LinkedNode<K, V> getLastNode(){
        return lastNode;
    }

    @Override
    public LinkedNode<K, V> get(Object key) {
        return map.get(key);
    }

    @Override
    public LinkedNode<K, V> put(K key, LinkedNode<K, V> value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LinkedNode<K, V> remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putAll(Map<? extends K, ? extends LinkedNode<K, V>> m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<LinkedNode<K, V>> values() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Entry<K, LinkedNode<K, V>>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     *
     * @author jun <jun@diosay.com>
     */
    public static class LinkedNode<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;
        private LinkedNode<K, V> prev;
        private LinkedNode<K, V> next;

        private LinkedNode(K k, V v) {
            key = k;
            value = v;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return this.value;
        }

        public LinkedNode<K, V> getPrev() {
            return prev;
        }

        public LinkedNode<K, V> getNext() {
            return next;
        }

    }
}

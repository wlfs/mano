/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mano.otpl.emit;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Tuple extends OpCode implements List<OpCode> {

    LinkedList<OpCode> list=new LinkedList<>();
    
    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<OpCode> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(OpCode e) {
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends OpCode> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends OpCode> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public OpCode get(int index) {
        return list.get(index);
    }

    @Override
    public OpCode set(int index, OpCode element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, OpCode element) {
        list.add(index, element);
    }

    @Override
    public OpCode remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<OpCode> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<OpCode> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<OpCode> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
    
    
    
}

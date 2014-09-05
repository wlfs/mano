/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl;

import java.util.LinkedList;

/**
 * 表示一个块结点。
 * @author jun <jun@diosay.com>
 */
public class Block implements Node {
    
    /**
     * 获取当前节点的所有子节点集合。
     */
    public final LinkedList<Node> children= new LinkedList<>();
    
    @Override
    public final boolean isBlock() {
        return true;
    }

    @Override
    public final boolean isText() {
        return false;
    }
    
    @Override
    public boolean isDocument() {
        return false;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public int getNodeType() {
        return Node.BLOCK;
    }

    @Override
    public Block getParent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node getNext() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Node getPrev() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * 使用给定的节点确定是否可以关闭当前节点。
     * @param node
     * @return 
     */
    protected boolean canClose(Node node){
        
        String names="/if,else,elif";
        return false;
    }

    @Override
    public Block append(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    protected void parse(){
        
    }
    
}

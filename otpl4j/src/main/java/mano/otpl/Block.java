/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.otpl;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * 表示一个块元素
 * @author jun <jun@diosay.com>
 */
public class Block extends Node {

    private LinkedList<Node> nodes;
    private LinkedList<Node> inlines;

    protected Block(Parser p, int type, String s) {
        super(p, type, s);
        nodes = new LinkedList<>();
        inlines = new LinkedList<>();
    }

    @Override
    public final boolean isBlock() {
        return true;
    }

    public LinkedList<Node> getNodes() {
        return this.nodes;
    }

    public LinkedList<Node> getInlines() {
        return this.inlines;
    }

    private boolean closed;

    protected void close() {
        this.closed = true;
    }
    int[] endTypes;

    public boolean canEnd(Node node) {
        if (endTypes == null) {
            return false;
        }
        for (int t : endTypes) {
            if (node.getNodeType() == t) {
                return true;
            }
        }
        return false;
    }

    public static Block create(Parser parser, int type, String source, int... endTypes) {
        Block result = new Block(parser, type, source);
        result.endTypes = endTypes;
        return result;
    }

    public Block append(Node node) {

        if (this.canEnd(node)) {
            this.close();
            if (node instanceof End) {
                return this.getParent();
            }
        }
        if (node.getNodeType() == Node.LEXS_PLAIN) { //合并字符串
            Node last = null;
            try {
                last = this.getNodes().getLast();
            } catch (NoSuchElementException e) {
            }
            if (last != null && last.getNodeType() == Node.LEXS_PLAIN) {
                last = this.getNodes().removeLast();
                last.source += node.source;
                node = last;
            }
        }
        if (this.closed) {
            node.setParent(this.getParent()); //设置父级
            node.setPrevNode(this);
            this.getParent().getNodes().add(node);
            return (Block) node;
        } else {
            node.setParent(this); //设置父级
            if (!this.getNodes().isEmpty()) { //设置上个节点
                node.setPrevNode(this.getNodes().getLast());
            }
        }
        this.getNodes().add(node);
        if (node.isBlock()) {
            return (Block) node;
        } else {
            return this;
        }
    }

    @Override
    public void parse() {
        this.parser.parse(this);
    }

}

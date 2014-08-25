/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package com.diosay.otpl;
//http://msdn.microsoft.com/zh-cn/library/system.xml.xmlnode(v=vs.110).aspx
/**
 * 表示 OTPL 文档中的单个节点。
 * @author jun <jun@diosay.com>
 */
public interface Node {
    
    final int UNDEFINED=0;
    /**
     * 作为文档树的根的文档对象提供对整个 OTPL 文档的访问。
     */
    final int DOCUMENT=1;
    /**
     * 表示一个独立的节点，它不能含有子节点。
     */
    final int SPAN=2;
    /**
     * 表示一个块节点，它必须使用一对<code>{node}{/node}</code> 来指示完整的块。
     */
    final int BLOCK=3;
    /**
     * 表示文本节点。
     * 它是一个特殊的 SPAN 节点，唯一不同是因为它不能被语法解析。
     */
    final int TEXT=4;
    
    /**
     * 获取一个值，以指示当前节点是否是块结点。
     * @return 
     */
    boolean isBlock();
    
    /**
     * 获取一个值，以指示当前节点是否是文本结点。
     * @return 
     */
    boolean isText();
    
    /**
     * 获取一个值，以指示当前文档的根类型。
     * @return 
     */
    boolean isDocument();
    
    /**
     * 获取当前节点的名称。
     * @return 
     */
    String getName();
    
    /**
     * 获取当前节点的类型。
     * @return 
     */
    int getNodeType();
    
    /**
     * 获取该节点（对于可以具有父级的节点）的父级。
     * @return 
     */
    Block getParent();
    
    /**
     * 获取紧接在该节点之后的节点。
     * @return 
     */
    Node getNext();
    
    /**
     * 获取紧接在该节点之前的节点。
     * @return 
     */
    Node getPrev();
    
    /**
     * 向当前节点的dom树附加一个节点。
     * @param node
     * @return 
     */
    Block append(Node node); 
    
}

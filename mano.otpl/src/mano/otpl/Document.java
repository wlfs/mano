/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl;

import java.util.NoSuchElementException;
import mano.otpl.Node;
import mano.otpl.Block;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Document extends Block {

    public Document(Parser p) {
        super(p, Node.LEXB_DOM, null);
        this.current = this;
    }
    Block current;

    @Override
    public Block append(Node node) {
        if (current == this) {
            if (node.getNodeType() == Node.LEXS_PLAIN) { //合并字符串
                Node last = null;
                try {
                    last = this.getNodes().getLast();
                } catch (NoSuchElementException e) {
                }
                if (last != null && last instanceof Plain) {
                    this.getNodes().removeLast();
                    node = new Plain(node.parser, node.getName(), last.source + node.source);
                }
            }
            node.setParent(this); //设置父级
            if (!this.getNodes().isEmpty()) { //设置上个节点
                node.setPrevNode(this.getNodes().getLast());
            }
            this.getNodes().add(node);
            if (node.isBlock()) {
                current = (Block) node;
            }
        } else {
            current = current.append(node);
        }
        return current;
    }

    @Override
    public boolean canEnd(Node node) {
        
        return false;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl;

import java.util.Map;
import java.util.NoSuchElementException;
import mano.otpl.Block;
import mano.otpl.Node;
import mano.util.NameValueCollection;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Document extends Block {

    Block current;
    public Node Layout;
    public Node Body;
    public Map<String, Block> blocks;

    public Document(Parser p) {
        super(p, Node.LEXB_DOM, null);
        this.current = this;
        blocks = new NameValueCollection<>();
    }

    @Override
    public Block append(Node node) {

        if (current == this) {
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
            node.setParent(this); //设置父级
            if (!this.getNodes().isEmpty()) { //设置上个节点
                node.setPrevNode(this.getNodes().getLast());
            }
            this.getNodes().add(node);
            if (node.isBlock()) {
                current = (Block) node;
                if (node.getNodeType() == Node.LEXB_BLOCK) {
                    blocks.put(node.getSource(), current);
                }
            }
        } else {
            if (current.getNodeType() == Node.LEXB_BLOCK && node.getNodeType() == Node.LEXB_BLOCK) {
                this.parser.reportError("block 不允许嵌套");
            }
            current = current.append(node);
            if (node.getNodeType() == Node.LEXB_BLOCK) {
                blocks.put(node.getSource(), current);
            }
        }
        return current;
    }

    @Override
    public boolean canEnd(Node node) {

        return false;
    }

}

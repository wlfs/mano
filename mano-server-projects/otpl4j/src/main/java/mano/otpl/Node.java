/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.otpl;

/**
 * 表示一个元素节点。
 * @author jun <jun@diosay.com>
 */
public abstract class Node {

    public static final int NODE_UNDEFINED = 0,
            LEXB_DOM = 1,
            LEXS_PLAIN = 2,
            LEXS_PRINT = 3,
            LEXB_IF = 4,
            LEXB_ELIF = 5,
            LEXB_ELSE = 6,
            LEXS_ENDIF = 7,
            LEXS_RAW = 8,
            LEXS_CCALL = 9,
            LEXS_CALL = 10,
            LEXB_FOR = 11,
            LEXS_ENDFOR = 12,
            LEXB_EACH = 13,
            LEXS_ENDEACH = 14,
            LEXB_WHILE = 15,
            LEXS_ENDWHILE = 16,
            LEXS_BREAK = 17,
            LEXS_CONTINUE = 18,
            LEXS_EXIT = 19,
            LEXS_INCLUDE = 20,
            LEXS_LAYOUT = 21,
            LEXB_BLOCK = 22,
            LEXS_ENDBLOCK = 23,
            LEXS_BODY = 24,
            LEXS_SET = 25,
            LEXS_PLACE = 26;

    private String nodeName;
    private String fileName;
    private long lineNo;
    private Block parent;
    private int indent = 0;
    private int nodeType;
    public static char INDENT_CHAR = ' ';
    protected String source;
    protected final Parser parser;
    protected Node prevNode;
    protected Node nextNode;
    protected Node(Parser p, int type, String s) {
        parser = p;
        this.nodeType = type;
        this.fileName = parser.getSourceName();
        this.lineNo = parser.getCurrentLine();
        source = s;
    }

    public Block getParent() {
        return parent;
    }

    public void setParent(Block node) {
        this.indent = node.getIndent() + (node instanceof Document ? 0 : 4);
        parent = node;
    }

    public int getIndent() {
        return indent;
    }

    public String getName() {
        return nodeName;
    }

    public String getSourceFilename() {
        return fileName;
    }

    public String getSource() {
        return source;
    }
    
    public long getLine() {
        return lineNo;
    }

    public abstract boolean isBlock();

    public int getNodeType(){
        return nodeType;
    }
    
    public Node getPrevNode(){
        return prevNode;
    }
    
    public void setPrevNode(Node node){
        node.nextNode=this;
        this.prevNode=node;
    }
    
    public Node getNextNode(){
        return nextNode;
    }
    
    
    public abstract void parse();

    protected void appendIndents(StringBuilder sb) {
        for (int i = 0; i < this.getIndent(); i++) {
            sb.append(Node.INDENT_CHAR);
        }
    }

    protected void appendLine(StringBuilder sb) {
        sb.append("\r\n");
    }
    
    public boolean mark=false;
}

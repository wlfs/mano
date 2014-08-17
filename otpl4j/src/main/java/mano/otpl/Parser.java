/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.otpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Parser {

    protected Parser() {

    }

    protected String filename;
    protected java.io.BufferedReader reader;
    private long line = 0;
    private Map<Long, String> lines;
    String leftDelimiter = "{{";
    String rightDelimiter = "}}";

    public String getSourceName() {
        return filename;
    }

    public long getCurrentLine() {
        return line;
    }

    public void open(String file) throws FileNotFoundException,IOException {
        java.io.FileInputStream fs = new java.io.FileInputStream(file);
        reader = new java.io.BufferedReader(new java.io.InputStreamReader(fs,"utf-8"));
        filename = file;
    }

    protected boolean isDigital(char c) {
        return (c >= 48 && c <= 57);
    }

    protected boolean isLetter(char c) {
        return (c >= 65 && c <= 90 || c >= 97 && c <= 122);
    }

    protected boolean isAlphanumeric(char c) {
        return isDigital(c) || isLetter(c);
    }

    protected boolean isWhitespace(char c) {
        return (c == ' ' || c == '\t');
    }

    public void parse() throws IOException {
        line=0;
        lines = new HashMap<>();
        dom = new Document(this);
        String s;
        while ((s = reader.readLine()) != null) {
            line++;
            lines.put(line, s);
            this.findLine(s, 0);
            parsePlian("\r\n");////todo: 补足一个回车，因为分析时的消耗
        }
    }

    protected void findLine(String line, int index) {
        if (line == null || "".equals(line)) {
            return;
        }
        this.findDelimiter(line, index);

    }

    private void findDelimiter(String source, int index) {
        int old=index;
        String ld = "<!--" + leftDelimiter;
        String rd = rightDelimiter + "-->";
        int start = source.indexOf(ld, index);
        if (start < 0) {
            ld = leftDelimiter;
            rd = rightDelimiter;
            start = source.indexOf(ld, index);
        }
        
        if (start > -1) {
            
            int end = this.parseRange(source, start + ld.length(), ld, rd);
            //int next = source.indexOf(leftDelimiter, end); //确认中间是否有分割符
            if (end < 0) {
                this.reportError("分割符");
            }
            
            String tmp=source.substring(start + ld.length(), end).trim();
            if(inLiteral){
                if (assertKeyword("/literal",tmp) > -1) {
                    inLiteral=false;
                }
                else{
                    parsePlian(source.substring(index, end + rd.length()));
                }
            }
            else{
                parsePlian(source.substring(index, start));
                this.parseMarkup(tmp);
            }
            this.findLine(source.substring(end + rd.length()), 0);
        } else {
            parsePlian(source.substring(index));
        }

    }

    protected void parsePlian(String source) {
        if (inComment) {
            return;
        }
        dom.append(Span.create(this, Node.LEXS_PLAIN, source));
    }

    protected int parseIdentifier(String source, int index) {
        if (source == null || index < 0 || index > source.length() || !this.isLetter(source.charAt(index))) {
            return -1;
        }
        int ori = index;
        for (; index < source.length(); index++) {
            if (!this.isAlphanumeric(source.charAt(index))) {
                break;
            }
        }
        return ori == index ? -1 : index;
    }

    protected int parseNumber(String source, int index) {
        if (source == null || index < 0 || index > source.length()) {
            return -1;
        }
        int ori = index;
        for (; index < source.length(); index++) {
            if (!this.isDigital(source.charAt(index))) {
                break;
            }
        }
        return ori == index ? -1 : index;
    }

    protected int parseRange(String source, int index, String open, String close) {
        if (index < 0 || index >= source.length() || open == null || open.length() < 1 || close == null || close.length() < 1) {
            return -1;
        }
        boolean like = open.equals(close);
        int matches = 1;
        for (; index < source.length(); index++) {
            if (source.charAt(index) == '\\') {//转义
                index++;
            } else if (!like && source.charAt(index) == open.charAt(0)) {//确定开始
                boolean tmp = true;
                for (int i = 0; i < open.length(); i++) {
                    if (source.charAt(index + i) != open.charAt(i)) {
                        tmp = false;
                        break;
                    }
                }
                if (tmp) {
                    index += open.length();
                    matches += 1;
                }
            } else if (source.charAt(index) == close.charAt(0)) {//确定结束
                boolean tmp = true;
                for (int i = 0; i < close.length(); i++) {
                    if (source.charAt(index + i) != close.charAt(i)) {
                        tmp = false;
                        break;
                    }
                }
                if (tmp) {
                    matches -= 1;
                    if (matches == 0) {
                        return index;
                    }
                    index += close.length();
                }
            }
        }

        return -1;
    }
    protected Node cnode;

    protected void reportError(String cacuse) {
        if (cnode != null) {
            cacuse += "," + cnode.getLine() + "(" + cnode.getSourceFilename() + ")";
        } else {
            cacuse += "," + this.line + "(" + this.filename + ")";
        }
        throw new java.lang.RuntimeException(cacuse);
    }

    protected int assertKeyword(String key, String source) {
        return assertKeyword(key, source, 0);
    }

    protected int assertKeyword(String key, String source, int index) {
        if (key == null || key.length() == 0 || source == null || source.length() == 0) {
            return -1;
        }
        boolean tmp = false;
        int total = source.length();
        for (; index < total; index++) {
            if (!this.isWhitespace(source.charAt(index))) {
                break;
            }
        }

        for (; index < total; index++) {
            if (source.charAt(index) == key.charAt(0)) {
                tmp = true;
                for (int i = 0; i < key.length(); i++) {
                    if (index + i >= total || source.charAt(index + i) != key.charAt(i)) {
                        tmp = false;
                        break;
                    }
                }
                if (tmp) {
                    break;
                } else {
                    index++;
                }
            } else {
                return -1;
            }
        }
        if (tmp) {
            return index + key.length();
        }
        return -1;
    }
    protected boolean inLiteral = false;
    protected boolean inComment = false;
    protected Document dom;

    protected void parseMarkup(String source) {
        int index;
        if (source.startsWith("/*")) { //块注释结束
            inComment = true;
        } else if (source.startsWith("*/")) {//块注释开始
            inComment = false;
        } else if (inComment || source.startsWith("//")) { //当前为注释中
            //ignored
        } else if ((index = assertKeyword("literal", source)) > -1) { //end if
            inLiteral=true;
        } else if ((index = assertKeyword("/if", source)) > -1) { //end if
            Node node = End.create(this, Node.LEXS_ENDIF);
            dom.append(node);
        } else if ((index = assertKeyword("/for", source)) > -1) { //end if
            Node node = End.create(this, Node.LEXS_ENDFOR);
            dom.append(node);
        } else if ((index = assertKeyword("/each", source)) > -1) { //end if
            Node node = End.create(this, Node.LEXS_ENDEACH);
            dom.append(node);
        } else if ((index = assertKeyword("/while", source)) > -1) { //end if
            Node node = End.create(this, Node.LEXS_ENDWHILE);
            dom.append(node);
        } else if ((index = assertKeyword("/block", source)) > -1) { //end if
            Node node = End.create(this, Node.LEXS_ENDBLOCK);
            dom.append(node);
        } else if ((index = assertKeyword("block", source)) > -1) { //end if
            if ((index = assertKeyword(":", source, index)) < 0) {
                this.reportError("语法错误");
            }
            int found = this.parseIdentifier(source, index);
            if (found < 0) {
                this.reportError("非法的标识符");
            }
            Node node = Block.create(this, Node.LEXB_BLOCK, source.substring(index, found), Node.LEXS_ENDBLOCK);
            dom.append(node);
        } else if ((index = assertKeyword("elif", source)) > -1) { //end if
            Node node = Block.create(this, Node.LEXB_ELIF, source.substring(index), Node.LEXB_ELIF, Node.LEXB_ELSE, Node.LEXS_ENDIF);
            dom.append(node);
        } else if ((index = assertKeyword("else", source)) > -1) {//if for each while
            Node node = Block.create(this, Node.LEXB_ELSE, source.substring(index), Node.LEXS_ENDFOR, Node.LEXS_ENDIF, Node.LEXS_ENDWHILE,Node.LEXS_ENDEACH);
            //Node node = new End(this,"else", source.substring(index));
            dom.append(node);
        } else if ((index = assertKeyword("if", source)) > -1) { //if语句
            /*String test = this.parseExpr(s, index);
             if (test == null || "".equals(test.trim())) {
             this.reportError("syntax error,illegal markup. look like as a if statement but dont found cod");
             }*/
            //Node node = new If(this,"if", source.substring(index));
            Node node = Block.create(this, Node.LEXB_IF, source.substring(index), Node.LEXB_ELIF, Node.LEXB_ELSE, Node.LEXS_ENDIF);
            dom.append(node);
        } else if ((index = assertKeyword("for", source)) > -1) { //语句
            Node node = Block.create(this, Node.LEXB_FOR, source.substring(index), Node.LEXS_ENDFOR, Node.LEXB_ELSE);
            //node.PName=id;
            dom.append(node);
        } else if ((index = assertKeyword("each", source)) > -1) { //语句
            Node node = Block.create(this, Node.LEXB_EACH, source.substring(index), Node.LEXS_ENDEACH, Node.LEXB_ELSE);
            dom.append(node);
        } else if ((index = assertKeyword("while", source)) > -1) { //语句
            Node node = Block.create(this, Node.LEXB_WHILE, source.substring(index), Node.LEXS_ENDWHILE, Node.LEXB_ELSE);
            dom.append(node);
        } else if ((index = assertKeyword("break", source)) > -1) { //语句
            Node node = Span.create(this, Node.LEXS_BREAK, source.substring(index));
            dom.append(node);
        } else if ((index = assertKeyword("continue", source)) > -1) { //语句
            Node node = Span.create(this, Node.LEXS_BREAK, source.substring(index));
            dom.append(node);
        } else if ((index = assertKeyword("layout", source)) > -1) { //语句
            if (dom.Layout != null) {
                this.reportError("不能存在多个 layout 标签.");
            }
            int tmp = assertKeyword("'", source, index);
            if (tmp < 0) {
                this.reportError("未设置 layout 路径.");
            }
            //source=source.substring(tmp);
            int end = this.parseRange(source, tmp, "'", "'");
            if (tmp < 0) {
                this.reportError("未设置 layout 路径.");
            }

            Node node = Span.create(this, Node.LEXS_LAYOUT, source.substring(tmp, end));
            dom.Layout = node;
        } else if ((index = assertKeyword("body", source)) > -1) { //语句
            if (dom.Body != null) {
                this.reportError("不能存在多个 body 标签.");
            }

            Node node = Span.create(this, Node.LEXS_BODY, source.substring(index));
            dom.Body = node;
            dom.append(node);
        } else if ((index = assertKeyword("include", source)) > -1) { //语句

            int tmp = assertKeyword("'", source, index);
            if (tmp < 0) {
                this.reportError("未设置 include 路径.");
            }
            //source=source.substring(tmp);
            int end = this.parseRange(source, tmp, "'", "'");
            if (tmp < 0) {
                this.reportError("未设置 include 路径.");
            }

            Node node = Span.create(this, Node.LEXS_INCLUDE, source.substring(tmp, end));
            dom.append(node);
        } else if ((index = assertKeyword("place", source)) > -1) { //语句
            Node node = Span.create(this, Node.LEXS_PLACE, source.substring(index));
            dom.append(node);
        } else {
            //String code;
            Node node;
            if (source.startsWith("@")) {
                node = Span.create(this, Node.LEXS_RAW, source);
            } else {
                node = Span.create(this, Node.LEXS_PRINT, source);
            }
            dom.append(node);
        }
        //System.out.println(s);
    }

    public void compile(String toFilename) throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        //dom.compile(sb);
        System.out.println(sb.toString());
        java.io.FileOutputStream fs = new java.io.FileOutputStream(toFilename);
        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fs));
        writer.append(sb);
        writer.flush();
        writer.close();

    }

    public abstract void parse(Node node);

}

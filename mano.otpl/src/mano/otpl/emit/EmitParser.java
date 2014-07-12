/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl.emit;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.otpl.*;
import mano.util.Utility;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class EmitParser extends Parser {

    public static void main(String[] args) {
        EmitParser parser = new EmitParser();
        /*Object t = paraser.parseExpr("123.56", 0);//13
         System.out.println(t);*/
        //http://java.chinaitlab.com/base/922270.html

        try {
            parser.open("E:\\repositories\\java\\mano\\mano.server\\server\\wwwroot\\views\\tpl\\member.tpl.html");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {

            String path = "E:\\repositories\\java\\mano\\mano.server\\server\\tmp\\";
            String name = Integer.toHexString(parser.getSourceName().hashCode()) + ".il";
            File file = new File(path + name);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fs = new FileOutputStream(file);
            OutputStreamWriter write = new OutputStreamWriter(fs, "UTF-8");
            parser.compile(fs);
            fs.close();
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("compiling done");
    }

    @Override
    public void parse() throws IOException {
        super.parse();

        this.dom.parse();
        codes.add(OpCode.create(OpCodes.EXIT));
    }

    LinkedList<OpCode> codes = new LinkedList<>();
    long addr = 0;

    private long newAddress() {
        addr++;
        return addr;
    }

    @Override
    public void parse(Node node) {//OpCode loop_start, OpCode loop_end
        if (node == null || node.mark) {
            return;
        }
        if (node.isBlock()) {
            parse((Block) node);
        } else {
            parse((Span) node);
        }
    }

    private void parse(Block node) {
        if (node == null) {
            return;
        }
        if (false) {
            System.out.println(node.getSource());
            for (Node sub : node.getNodes()) {
                sub.parse();
            }
            return;
        }
        switch (node.getNodeType()) {
            case Node.LEXB_BLOCK:
                break;
            case Node.LEXB_DOM:
                codes.add(OpCode.create(OpCodes.DOM));
                for (Node sub : node.getNodes()) {
                    sub.parse();
                }
                return;
            case Node.LEXB_EACH:
                break;
            case Node.LEXB_ELSE:
                throw new java.lang.RuntimeException("else");
            case Node.LEXB_FOR: {
                String source = node.getSource();
                int index;
                if ((index = assertKeyword(":", source)) < 0) {
                    this.reportError("语法错误");
                }
                int found = this.parseIdentifier(source, index);
                if (found < 0) {
                    this.reportError("非法的标识符");
                }
                String id = source.substring(index, found);
                OpCode ret = OpCode.label();
                OpCode begin = OpCode.create(OpCodes.BEGIN_BLOCK);
                OpCode end = OpCode.create(OpCodes.END_BLOCK, begin);
                OpCode label = OpCode.label();
                OpCode label2 = OpCode.label();
                OpCode label3 = OpCode.label();
                codes.add(begin);

                LinkedList<OpCode> args = new LinkedList<>();
                AtomicInteger count = new AtomicInteger(0);
                this.parseExpr(source.substring(found), 0, args, count);//参数
                if (!args.isEmpty()) {
                    count.addAndGet(1);
                }
                while (true) {
                    OpCode n = args.pollFirst();
                    if (n == null) {
                        break;
                    }
                    codes.add(n);
                }

                if (count.get() == 3) {

                } else if (count.get() == 2) {
                    codes.add(OpCode.create(OpCodes.LOAD_INTEGER, 1L));
                } else if (count.get() == 1) {
                    codes.add(OpCode.create(OpCodes.LOAD_INTEGER, 0L));
                    codes.add(OpCode.create(OpCodes.LOAD_INTEGER, 1L));
                } else {
                    this.reportError("参数错误");
                }
                String part = Integer.toHexString(UUID.randomUUID().hashCode());
                String max = id + "$max_" + part;
                String step = id + "$stp_" + part;

                codes.add(OpCode.create(OpCodes.SET_VAR, step));
                codes.add(OpCode.create(OpCodes.SET_VAR, id));
                codes.add(OpCode.create(OpCodes.SET_VAR, max));

                //比较两个数的大小，如果初始大于max则跳到else，如果有的话
                codes.add(OpCode.create(OpCodes.LOAD_VAR, id));
                codes.add(OpCode.create(OpCodes.LOAD_VAR, max));
                codes.add(OpCode.create(OpCodes.OP_GTE));
                codes.add(OpCode.create(OpCodes.CONDITION_JUMP, end, label2)); //第一次判断
                codes.add(label);
                codes.add(OpCode.create(OpCodes.LOAD_VAR, step));
                codes.add(OpCode.create(OpCodes.LOAD_VAR, id));
                codes.add(OpCode.create(OpCodes.OP_ADD));
                codes.add(OpCode.create(OpCodes.SET_VAR, id));
                codes.add(OpCode.create(OpCodes.LOAD_VAR, id));
                codes.add(OpCode.create(OpCodes.LOAD_VAR, max));
                codes.add(OpCode.create(OpCodes.OP_GTE));
                codes.add(OpCode.create(OpCodes.CONDITION_JUMP, ret, label2));//循环判断
                codes.add(label2);
                for (Node sub : node.getNodes()) {
                    if (sub.getNodeType() == Node.LEXS_BREAK) {
                        sub.mark = true;
                        codes.add(OpCode.create(OpCodes.JUMP, ret));
                    } else if (sub.getNodeType() == Node.LEXS_CONTINUE) {
                        sub.mark = true;
                        codes.add(OpCode.create(OpCodes.JUMP, label));
                    } else {
                        sub.parse();
                    }
                }
                codes.add(OpCode.create(OpCodes.JUMP, label));
                codes.add(end);
                Node next = node.getNextNode();
                if (next != null && next.getNodeType() == Node.LEXB_ELSE) {
                    next.mark = true;

                    begin = OpCode.create(OpCodes.BEGIN_BLOCK);
                    end = OpCode.create(OpCodes.END_BLOCK, begin);
                    codes.add(begin);
                    for (Node sub : ((Block) next).getNodes()) {
                        sub.parse();
                    }
                    codes.add(OpCode.create(OpCodes.JUMP, ret));
                    codes.add(end);
                }
                codes.add(ret);
                return;
            }
            case Node.LEXB_ELIF:
                throw new java.lang.RuntimeException("elif");
            case Node.LEXB_IF:
                OpCode ret = OpCode.label();
                OpCode begin = OpCode.create(OpCodes.BEGIN_BLOCK);
                OpCode end = OpCode.create(OpCodes.END_BLOCK, begin);
                OpCode label = OpCode.label();
                codes.add(begin);
                this.parseExpr(node.getSource(), 0);
                codes.add(OpCode.create(OpCodes.CONDITION_JUMP, label, end));
                codes.add(label);
                for (Node sub : node.getNodes()) {
                    sub.parse();
                }
                codes.add(OpCode.create(OpCodes.JUMP, ret));
                codes.add(end);

                Node next = node.getNextNode();
                while (true) {
                    if (next != null && next.getNodeType() == Node.LEXB_ELIF) {
                        next.mark = true;

                        begin = OpCode.create(OpCodes.BEGIN_BLOCK);
                        end = OpCode.create(OpCodes.END_BLOCK, begin);
                        label = OpCode.label();
                        codes.add(begin);
                        this.parseExpr(next.getSource(), 0);
                        codes.add(OpCode.create(OpCodes.CONDITION_JUMP, label, end));
                        codes.add(label);
                        for (Node sub : ((Block) next).getNodes()) {
                            sub.parse();
                        }
                        codes.add(OpCode.create(OpCodes.JUMP, ret));
                        codes.add(end);

                        next = next.getNextNode();
                    } else {
                        break;
                    }
                }
                if (next != null && next.getNodeType() == Node.LEXB_ELSE) {
                    next.mark = true;

                    begin = OpCode.create(OpCodes.BEGIN_BLOCK);
                    end = OpCode.create(OpCodes.END_BLOCK, begin);
                    codes.add(begin);
                    for (Node sub : ((Block) next).getNodes()) {
                        sub.parse();
                    }
                    codes.add(OpCode.create(OpCodes.JUMP, ret));
                    codes.add(end);
                }
                codes.add(ret);
                return;
            case Node.LEXB_WHILE:
                break;
        }
        System.out.println("no parsing:" + node.getNodeType());
    }

    private void parse(Span node) {
        if (node == null) {
            return;
        }
        if (false) {
            System.out.println(node.getSource());
            return;
        }
        switch (node.getNodeType()) {
            case Node.LEXS_BODY:
            case Node.LEXS_BREAK:
            case Node.LEXS_CALL:
            case Node.LEXS_CCALL:
            case Node.LEXS_CONTINUE:
            case Node.LEXS_RAW:
            case Node.LEXS_ENDBLOCK:
            case Node.LEXS_ENDEACH:
            case Node.LEXS_ENDFOR:
            case Node.LEXS_ENDIF:
            case Node.LEXS_ENDWHILE:
            case Node.LEXS_EXIT:
            case Node.LEXS_INCLUDE:
            case Node.LEXS_LAYOUT:
                throw new java.lang.RuntimeException("bbbbb");
            case Node.LEXS_PLAIN:
                codes.add(OpCode.create(OpCodes.PRINT_STR, node.getSource()));
                return;
            case Node.LEXS_PRINT:
                this.parseExpr(node.getSource(), 0);
                codes.add(OpCode.create(OpCodes.PRINT, node.getSource()));
                return;
            case Node.LEXS_SET:
                break;
        }
        System.out.println("no parsing:" + node.getNodeType());
    }

    boolean canSwap = false;

    /**
     * 查找最后一个元素查看是否是运算操作
     *
     * @return
     */
    private OpCode findSwap(LinkedList<OpCode> link) {
        if (!canSwap) {
            return null;
        }
        canSwap = false;
        OpCode code = null;
        try {
            code = link.getLast();
        } catch (NoSuchElementException e) {
        }
        if (code == null) {
            return null;
        }
        ArrayList<Short> list = new ArrayList<>();
        list.add(OpCodes.OP_ADD.getValue());
        list.add(OpCodes.OP_AND.getValue());
        list.add(OpCodes.OP_DIV.getValue());
        list.add(OpCodes.OP_EQ.getValue());
        list.add(OpCodes.OP_GT.getValue());
        list.add(OpCodes.OP_GTE.getValue());
        list.add(OpCodes.OP_LT.getValue());
        list.add(OpCodes.OP_LTE.getValue());
        list.add(OpCodes.OP_MOD.getValue());
        list.add(OpCodes.OP_MUL.getValue());
        list.add(OpCodes.OP_NEQ.getValue());
        list.add(OpCodes.OP_NOT.getValue());
        list.add(OpCodes.OP_NOT.getValue());
        list.add(OpCodes.OP_OR.getValue());
        list.add(OpCodes.OP_SUB.getValue());
        if (list.contains(code.getCode().getValue())) {
            link.removeLast();
            return code;
        }
        return null;
    }

    private OpCode findSwap() {
        return findSwap(codes);
    }

    private boolean isDot(String source, int index) {
        if (source == null || index >= source.length()) {
            return false;
        }
        return source.charAt(index) == '.';
    }

    protected void parseExpr(String source, int index) {
        parseExpr(source, index, codes, new AtomicInteger(0));
    }

    protected void parseExpr(String source, int index, LinkedList<OpCode> link, AtomicInteger argCount) {
        if (source == null || index >= source.length()) {
            return;
        }

        for (; index < source.length(); index++) {
            if (this.isWhitespace(source.charAt(index))) {//空白
                //ignored
            } else if (source.charAt(index) == '$' || source.charAt(index) == '.' || this.isLetter(source.charAt(index))) {//视图对象
                boolean local = false;
                boolean memberacc = false;
                if (source.charAt(index) == '$') {
                    local = true;
                    index++;
                } else if (source.charAt(index) == '.') {
                    memberacc = true;
                    index++;
                }

                int tmp = parseIdentifier(source, index);
                if (tmp < 0) {
                    this.reportError("syntax error,illegal identifier at " + index);
                }
                String id = source.substring(index, tmp);
                index = tmp - 1;
                OpCode swap = findSwap();
                //预测后续
                if (index + 1 >= source.length()) {
                    //处理

                    if (memberacc) {
                        link.add(OpCode.create(OpCodes.LOAD_PROPERTY, id));//获取成员
                        link.add(OpCode.create(OpCodes.CALL));//执行
                    } else {
                        this.codes.add(OpCode.create(OpCodes.LOAD_VAR, id));//变量
                    }
                    if (swap != null) {
                        link.add(swap);
                    }
                    return;
                }
                int type = 0;
                if (source.charAt(index + 1) == '[') {
                    type = 1;//索引
                    tmp = this.parseRange(source, index + 2, "[", "]");
                } else if (source.charAt(index + 1) == '(') {
                    type = 2;//函数
                    tmp = this.parseRange(source, index + 2, "(", ")");
                }
                if (type != 0) {
                    if (tmp < 0) {
                        this.reportError("syntax error,illegal characters at " + index);
                    }
                    LinkedList<OpCode> args = new LinkedList<>();
                    AtomicInteger count = new AtomicInteger(0);
                    this.parseExpr(source.substring(index + 2, tmp), 0, args, count);//参数
                    if (!args.isEmpty()) {
                        count.addAndGet(1);
                    }
                    int args_len = count.get();
                    index = tmp + 1;
                    if (memberacc) {
                        if (type == 1) {
                            link.add(OpCode.create(OpCodes.LOAD_PROPERTY, id));//获取成员
                            link.add(OpCode.create(OpCodes.CALL));//执行
                        } else {
                            link.add(OpCode.create(OpCodes.LOAD_METHOD, args_len, id));//获取成员
                        }
                    } else {
                        link.add(OpCode.create(OpCodes.LOAD_VAR, id));//变量
                    }
                    if (type == 1) {
                        link.add(OpCode.create(OpCodes.INDEXER, args_len, id));//查找索引器
                    }
                    while (true) {
                        OpCode n = args.pollFirst();
                        if (n == null) {
                            break;
                        }
                        link.add(n);
                    }
                    link.add(OpCode.create(OpCodes.CALL, args_len));//执行
                    if (swap != null) {
                        this.codes.add(swap);
                    }
                } else {
                    if (memberacc) {
                        link.add(OpCode.create(OpCodes.LOAD_PROPERTY, id));//获取成员
                        link.add(OpCode.create(OpCodes.CALL));//执行
                    } else {
                        link.add(OpCode.create(OpCodes.LOAD_VAR, id));//变量
                    }
                    if (swap != null) {
                        link.add(swap);
                    }
                }
            } else if (this.isDigital(source.charAt(index))) {//数字
                int tmp = this.parseNumber(source, index);
                if (tmp < 0) {
                    this.reportError("syntax error,illegal identifier at " + index);
                }
                String part = source.substring(index, tmp);
                index = tmp;
                OpCode swap = findSwap();
                if (index < source.length() && source.charAt(index) == '.') {
                    tmp = this.parseNumber(source, index + 1);
                    if (tmp < 0) {
                        this.reportError("syntax error,illegal identifier at " + index);
                    }
                    part += "." + source.substring(index + 1, tmp);
                    index = tmp;
                    link.add(OpCode.create(OpCodes.LOAD_NUMBER, Double.parseDouble(part)));
                } else {
                    link.add(OpCode.create(OpCodes.LOAD_INTEGER, Long.parseLong(part)));
                }
                if (swap != null) {
                    link.add(swap);
                }
                index--;
                //load_arg number

            } else if (source.charAt(index) == '(') { //括号
                int tmp = this.parseRange(source, index + 1, "(", ")");
                if (tmp < 0) {
                    this.reportError("syntax error,illegal characters at " + index);
                }
                this.parseExpr(source.substring(index + 1, tmp), 0);
                index = tmp + 1;
            } else if (source.charAt(index) == '\'') {//字符串
                int tmp = this.parseRange(source, index + 1, "'", "'");
                if (tmp < 0) {
                    this.reportError("syntax error,illegal characters at " + index);
                }
                OpCode swap = findSwap();
                link.add(OpCode.create(OpCodes.LOAD_STR, source.substring(index + 1, tmp)));
                if (swap != null) {
                    link.add(swap);
                }
                index = tmp;
            } else if (source.charAt(index) == '+') {//运算符
                link.add(OpCode.create(OpCodes.OP_ADD));
                canSwap = true;
            } else if (source.charAt(index) == '-') {//运算符
                if (index + 1 < source.length() && this.isDigital(source.charAt(index + 1))) {//负数
                    int tmp = this.parseNumber(source, index + 1);
                    if (tmp < 0) {
                        this.reportError("syntax error,illegal identifier at " + index);
                    }
                    String part = "-" + source.substring(index, tmp);
                    index = tmp;
                    OpCode swap = findSwap();
                    if (index < source.length() && source.charAt(index) == '.') {
                        tmp = this.parseNumber(source, index + 1);
                        if (tmp < 0) {
                            this.reportError("syntax error,illegal identifier at " + index);
                        }
                        part += "." + source.substring(index + 1, tmp);
                        index = tmp;
                        link.add(OpCode.create(OpCodes.LOAD_NUMBER, Double.parseDouble(part)));
                    } else {
                        link.add(OpCode.create(OpCodes.LOAD_INTEGER, Long.parseLong(part)));
                    }

                    if (swap != null) {
                        link.add(swap);
                    }
                    index--;
                } else {
                    link.add(OpCode.create(OpCodes.OP_SUB));
                    canSwap = true;
                }
            } else if (source.charAt(index) == '*') {//运算符
                link.add(OpCode.create(OpCodes.OP_MUL));
                canSwap = true;
            } else if (source.charAt(index) == '/') {//运算符
                link.add(OpCode.create(OpCodes.OP_DIV));
                canSwap = true;
            } else if (source.charAt(index) == '%') {//运算符
                link.add(OpCode.create(OpCodes.OP_MOD));
                canSwap = true;
            } else if (source.charAt(index) == '=' && index + 1 < source.length() && source.charAt(index + 1) == '=') {//运算符
                link.add(OpCode.create(OpCodes.OP_EQ));
                canSwap = true;
                index += 1;
            } else if (source.charAt(index) == '!' && index + 1 < source.length() && source.charAt(index + 1) == '=') {//运算符
                link.add(OpCode.create(OpCodes.OP_NEQ));
                canSwap = true;
                index += 1;
            } else if (source.charAt(index) == '>' && index + 1 < source.length() && source.charAt(index + 1) == '=') {//运算符
                link.add(OpCode.create(OpCodes.OP_GTE));
                canSwap = true;
                index += 1;
            } else if (source.charAt(index) == '<' && index + 1 < source.length() && source.charAt(index + 1) == '=') {//运算符
                link.add(OpCode.create(OpCodes.OP_LTE));
                index += 1;
                canSwap = true;
            } else if (source.charAt(index) == '|' && index + 1 < source.length() && source.charAt(index + 1) == '|') {//运算符
                link.add(OpCode.create(OpCodes.OP_OR));
                index += 1;
                canSwap = true;
            } else if (source.charAt(index) == '&' && index + 1 < source.length() && source.charAt(index + 1) == '&') {//运算符
                link.add(OpCode.create(OpCodes.OP_AND));
                index += 1;
                canSwap = true;
            } else if (source.charAt(index) == '>') {//运算符
                link.add(OpCode.create(OpCodes.OP_GT));
                canSwap = true;
            } else if (source.charAt(index) == '<') {//运算符
                link.add(OpCode.create(OpCodes.OP_LT));
                canSwap = true;
            } else if (source.charAt(index) == '!') {//运算符
                link.add(OpCode.create(OpCodes.OP_NOT));
                canSwap = true;
            } else if (source.charAt(index) == ',') {//参数(使用时必须做修正)
                argCount.addAndGet(1);
            } else {
                this.reportError("syntax error,illegal characters at " + index);
            }
        }

    }
    /*
     public void parse(Document node) {
     codes.add(OpCode.create(OpCodes.DOM));
     for (Node sub : node.getNodes()) {
     sub.parse();
     }
     }

     public void parse(If node) {

     OpCode begin = OpCode.create(OpCodes.BEGIN_BLOCK);
     OpCode end = OpCode.create(OpCodes.END_BLOCK, begin);
     OpCode label = OpCode.create(OpCodes.NOP);
     codes.add(begin);
     this.parseExpr(node.getSource(), 0);
     codes.add(OpCode.create(OpCodes.CONDITION_JUMP, label, end));
     codes.add(label);
     for (Node sub : node.getNodes()) {
     sub.parse();
     }

     codes.add(end);
     }

     public void parse(Plain node) {
     codes.add(OpCode.create(OpCodes.PRINT_STR, node.getSource()));
     }

     public void parse(Print node) {
     this.parseExpr(node.getSource(), 0);
     codes.add(OpCode.create(OpCodes.PRINT, node.getSource()));
     }
     */

    public void compile(OutputStream output) throws IOException {

        this.parse();

        for (OpCode code : codes) {
            code.setAdress(this.newAddress());
        }

        for (OpCode code : codes) {
            this.compileT(code, output);
        }
    }

    /*private void compileTest(OpCode code, OutputStream output) throws IOException {
     //output.write(Utility.toBytes(code.getAddress()));
     String s = code.getAddress() + "";
     if (OpCodes.BEGIN_BLOCK.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.CONDITION_JUMP.equals(code.getCode())) {
     s += code.getCode();
     s += code.getElement(0).getAddress();
     s += code.getElement(1).getAddress();
     } else if (OpCodes.DOM.equals(code.getCode())) {
     s = "MANO-IL010101";
     } else if (OpCodes.END_BLOCK.equals(code.getCode())) {
     s += code.getCode();
     s += code.getElement(0).getAddress();
     } else if (OpCodes.JUMP.equals(code.getCode())) {
     s += code.getCode();
     s += code.getElement(0).getAddress();
     } else if (OpCodes.LOAD_DECIMAL.equals(code.getCode())) {
     s += code.getCode();
     s += code.getString();
     } else if (OpCodes.LOAD_EVN_ARG.equals(code.getCode())) {
     s += code.getCode();
     s += code.getString().length();//bytes
     s += code.getString();//bytes
     } else if (OpCodes.LOAD_INTEGER.equals(code.getCode())) {
     s += code.getCode();
     s += code.getString();
     } else if (OpCodes.LOAD_PROPERTY.equals(code.getCode())) {
     s += code.getCode();
     s += code.getString().length();//bytes
     s += code.getString();//bytes
     } else if (OpCodes.LOAD_METHOD.equals(code.getCode())) {
     s += code.getCode();
     s += code.getString().length();//bytes
     s += code.getString();//bytes
     } else if (OpCodes.LOAD_STR.equals(code.getCode())) {
     s += code.getCode();
     s += code.getString().length();//bytes
     s += code.getString();//bytes
     } else if (OpCodes.CALL.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.INDEXER.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.NOP.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_ADD.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_AND.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_DIV.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_EQ.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_GT.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_GTE.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_LT.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_LTE.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_MOD.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_MUL.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_NEQ.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_NOT.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_OR.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.OP_SUB.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.PRINT.equals(code.getCode())) {
     s += code.getCode();
     } else if (OpCodes.PRINT_STR.equals(code.getCode())) {
     s += code.getCode();
     s += code.getString().length();//bytes
     s += code.getString();//bytes
     }
     output.write(s.getBytes());
     output.write("\r\n".getBytes());
     }*/
    private void compile(OpCode code, OutputStream output) throws IOException {
        if (!OpCodes.DOM.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getAddress()));
            output.write(Utility.toBytes(code.getCode().getValue()));
        }
        if (OpCodes.BEGIN_BLOCK.equals(code.getCode())) {
        } else if (OpCodes.CONDITION_JUMP.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getElement(0).getAddress()));
            output.write(Utility.toBytes(code.getElement(1).getAddress()));
        } else if (OpCodes.DOM.equals(code.getCode())) {
            output.write("OTPL-IL".getBytes());//id7
            output.write(Utility.toBytes((short) 11));//ver
            output.write((int) ((byte) 1));//utf-8
            output.write(Utility.toBytes(0L));//file mod time8
            output.write(Utility.toBytes(0L));//gen time8
        } else if (OpCodes.END_BLOCK.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getElement(0).getAddress()));
        } else if (OpCodes.EXIT.equals(code.getCode())) {

        } else if (OpCodes.JUMP.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getElement(0).getAddress()));
        } else if (OpCodes.LOAD_NUMBER.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getNumber()));
        } else if (OpCodes.LOAD_VAR.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes();
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.LOAD_INTEGER.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getLong()));//
        } else if (OpCodes.LOAD_PROPERTY.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes();
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.LOAD_METHOD.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getInt()));
            byte[] bytes = code.getString().getBytes();
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.LOAD_STR.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes();
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.CALL.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getInt()));
        } else if (OpCodes.INDEXER.equals(code.getCode())) {
        } else if (OpCodes.NOP.equals(code.getCode())) {
        } else if (OpCodes.OP_ADD.equals(code.getCode())) {
        } else if (OpCodes.OP_AND.equals(code.getCode())) {
        } else if (OpCodes.OP_DIV.equals(code.getCode())) {
        } else if (OpCodes.OP_EQ.equals(code.getCode())) {
        } else if (OpCodes.OP_GT.equals(code.getCode())) {
        } else if (OpCodes.OP_GTE.equals(code.getCode())) {
        } else if (OpCodes.OP_LT.equals(code.getCode())) {
        } else if (OpCodes.OP_LTE.equals(code.getCode())) {
        } else if (OpCodes.OP_MOD.equals(code.getCode())) {
        } else if (OpCodes.OP_MUL.equals(code.getCode())) {
        } else if (OpCodes.OP_NEQ.equals(code.getCode())) {
        } else if (OpCodes.OP_NOT.equals(code.getCode())) {
        } else if (OpCodes.OP_OR.equals(code.getCode())) {
        } else if (OpCodes.OP_SUB.equals(code.getCode())) {
        } else if (OpCodes.PRINT.equals(code.getCode())) {
        } else if (OpCodes.PRINT_STR.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes();
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.SET_VAR.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes();
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        }
    }

    private void compileT(OpCode code, OutputStream output) throws IOException {
        String s = "";
        if (!OpCodes.DOM.equals(code.getCode())) {
            s += code.getAddress() + " ";
            s += code.getCode().getValue();
        }
        if (OpCodes.BEGIN_BLOCK.equals(code.getCode())) {
        } else if (OpCodes.CONDITION_JUMP.equals(code.getCode())) {
            s += code.getElement(0).getAddress() + " ";
            s += code.getElement(0).getAddress() + " ";
        } else if (OpCodes.DOM.equals(code.getCode())) {
            s += "OTPL-IL";
            s += (short) 11;
            s += (byte) 11;
            s += 0L;
            s += 0L;
        } else if (OpCodes.END_BLOCK.equals(code.getCode())) {
            s += code.getElement(0).getAddress() + " ";
        } else if (OpCodes.EXIT.equals(code.getCode())) {

        } else if (OpCodes.JUMP.equals(code.getCode())) {
            s += code.getElement(0).getAddress() + " ";
        } else if (OpCodes.LOAD_NUMBER.equals(code.getCode())) {
            s += code.getNumber() + " ";
            output.write(Utility.toBytes(code.getNumber()));
        } else if (OpCodes.LOAD_VAR.equals(code.getCode())) {
            s += code.getString().getBytes().length + " ";
            s += code.getString() + " ";
        } else if (OpCodes.LOAD_INTEGER.equals(code.getCode())) {
            s += code.getLong() + " ";
        } else if (OpCodes.LOAD_PROPERTY.equals(code.getCode())) {
            s += code.getString().getBytes().length + " ";
            s += code.getString() + " ";
        } else if (OpCodes.LOAD_METHOD.equals(code.getCode())) {
            s += code.getInt() + " ";
            s += code.getString().getBytes().length + " ";
            s += code.getString() + " ";
        } else if (OpCodes.LOAD_STR.equals(code.getCode())) {
            s += code.getString().getBytes().length + " ";
            s += code.getString() + " ";
        } else if (OpCodes.CALL.equals(code.getCode())) {
            s += code.getInt() + " ";
        } else if (OpCodes.INDEXER.equals(code.getCode())) {
        } else if (OpCodes.NOP.equals(code.getCode())) {
        } else if (OpCodes.OP_ADD.equals(code.getCode())) {
        } else if (OpCodes.OP_AND.equals(code.getCode())) {
        } else if (OpCodes.OP_DIV.equals(code.getCode())) {
        } else if (OpCodes.OP_EQ.equals(code.getCode())) {
        } else if (OpCodes.OP_GT.equals(code.getCode())) {
        } else if (OpCodes.OP_GTE.equals(code.getCode())) {
        } else if (OpCodes.OP_LT.equals(code.getCode())) {
        } else if (OpCodes.OP_LTE.equals(code.getCode())) {
        } else if (OpCodes.OP_MOD.equals(code.getCode())) {
        } else if (OpCodes.OP_MUL.equals(code.getCode())) {
        } else if (OpCodes.OP_NEQ.equals(code.getCode())) {
        } else if (OpCodes.OP_NOT.equals(code.getCode())) {
        } else if (OpCodes.OP_OR.equals(code.getCode())) {
        } else if (OpCodes.OP_SUB.equals(code.getCode())) {
        } else if (OpCodes.PRINT.equals(code.getCode())) {
        } else if (OpCodes.PRINT_STR.equals(code.getCode())) {
            s += code.getString().getBytes().length + " ";
            s += code.getString() + " ";
        } else if (OpCodes.SET_VAR.equals(code.getCode())) {
            s += code.getString().getBytes().length + " ";
            s += code.getString() + " ";
        }
        output.write(s.getBytes());
    }
}

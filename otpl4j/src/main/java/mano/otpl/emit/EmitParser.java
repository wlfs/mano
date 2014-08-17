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
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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

    /*public static void mains(String[] args) {
     EmitParser parser = new EmitParser();
     //Object t = paraser.parseExpr("123.56", 0);//13
     //System.out.println(t);
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
     //OutputStreamWriter write = new OutputStreamWriter(fs, "UTF-8");
     parser.parse();
     parser.compile(fs);
     fs.close();
     } catch (IOException ex) {
     Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
     }
     System.out.println("compiling done");
     }*/
    @Override
    public void parse() throws IOException {
        this.parseParent(null);
    }
    EmitParser parent;

    private void parseParent(Document content) throws IOException {
        this.conentDom = content;
        super.parse();

        if (dom.Layout != null) {
            parent = new EmitParser();
            String file = dom.Layout.getSource();
            if (file.startsWith("./") || file.startsWith(".\\")) {
                file = file.substring(2);
            } else if (file.startsWith("/") || file.startsWith("\\")) {
                file = file.substring(1);
            }
            parent.open(Paths.get(Paths.get(this.getSourceName()).getParent().toString(), file).toString());
            parent.parseParent(dom);

            if (parent.reader != null) {
                parent.reader.close();
                parent.reader=null;
            }

            //setparent
            //parent.parse(parent.dom, null, null);
        } else {//  if (this.conentDom == null)
            codes.add(OpCode.create(OpCodes.DOM));
            parse(dom, null, null);
            codes.add(OpCode.create(OpCodes.EXIT));
        }

    }

    LinkedList<OpCode> codes = new LinkedList<>();
    long addr = 0;

    private long newAddress() {
        addr++;
        return addr;
    }

    private long newAddress(int size) {
        addr++;
        long r = addr;
        addr += size;
        return r;
    }

    @Override
    public void parse(Node node) {//OpCode loop_start, OpCode loop_end
        if (node == null || node.mark) {
            return;
        }
        cnode = node;
        if (node.isBlock()) {
            parseBlock((Block) node, null, null);
        } else {
            parseSpan((Span) node, null, null);
        }
    }

    public void parse(Node node, OpCode loop_start, OpCode loop_end) {
        if (node == null || node.mark) {
            return;
        }
        cnode = node;
        if (node.isBlock()) {
            parseBlock((Block) node, loop_start, loop_end);

        } else {
            parseSpan((Span) node, loop_start, loop_end);
        }
    }

    private void parseBlock(Block node, OpCode loop_start, OpCode loop_end) {
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
                //codes.add(OpCode.create(OpCodes.DOM));
                for (Node sub : node.getNodes()) {
                    parse(sub, null, null);
                }
                return;
            case Node.LEXB_EACH: {
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
                OpCode elseLable = OpCode.label();
                OpCode testLabel = OpCode.label();
                OpCode continueLabel = OpCode.label();

                String part = Integer.toHexString(UUID.randomUUID().hashCode());
                String iterator = id + "$itel_" + part;
                String hasnext = id + "$hnxt_" + part;
                String next = id + "$nxt_" + part;

                this.parseExpr(source.substring(found), 0);//参数
                codes.add(OpCode.create(OpCodes.LOAD_ITERATOR));//将对象转换为迭代器，如果对象为null则会创建一个空迭代
                codes.add(OpCode.create(OpCodes.SET_VAR, iterator));//保存到迭代器变量
                codes.add(OpCode.create(OpCodes.LOAD_VAR, iterator));//载入迭代器对象
                codes.add(OpCode.create(OpCodes.LOAD_METHOD, 0, "hasNext"));//找到迭代器方法
                codes.add(OpCode.create(OpCodes.SET_VAR, hasnext));//保存迭代器方法变量
                codes.add(OpCode.create(OpCodes.LOAD_VAR, iterator));//载入迭代器对象
                codes.add(OpCode.create(OpCodes.LOAD_METHOD, 0, "next"));//找到迭代器方法
                codes.add(OpCode.create(OpCodes.SET_VAR, next));//保存迭代器方法变量

                //第一次判断
                codes.add(OpCode.create(OpCodes.LOAD_VAR, iterator));
                codes.add(OpCode.create(OpCodes.LOAD_VAR, hasnext));
                codes.add(OpCode.create(OpCodes.CALL, 0));
                codes.add(OpCode.create(OpCodes.CONDITION_JUMP, continueLabel, elseLable));
                codes.add(testLabel);
                //循环判断
                codes.add(OpCode.create(OpCodes.LOAD_VAR, iterator));
                codes.add(OpCode.create(OpCodes.LOAD_VAR, hasnext));
                codes.add(OpCode.create(OpCodes.CALL, 0));
                codes.add(OpCode.create(OpCodes.JUMP_FLASE, ret));
                codes.add(continueLabel);
                //设置迭代值
                codes.add(OpCode.create(OpCodes.LOAD_VAR, iterator));
                codes.add(OpCode.create(OpCodes.LOAD_VAR, next));
                codes.add(OpCode.create(OpCodes.CALL, 0));
                codes.add(OpCode.create(OpCodes.SET_VAR, id));

                //循环体
                for (Node sub : node.getNodes()) {
                    parse(sub, testLabel, ret);
                }
                codes.add(OpCode.create(OpCodes.JUMP, testLabel));
                codes.add(elseLable);

                //each-else body
                Node nextNode = node.getNextNode();
                if (nextNode != null && nextNode.getNodeType() == Node.LEXB_ELSE) {
                    nextNode.mark = true;

                    for (Node sub : ((Block) nextNode).getNodes()) {
                        parse(sub, null, null);
                    }

                    codes.add(OpCode.create(OpCodes.JUMP, ret));
                }
                codes.add(ret);
                return;
            }
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
                    parse(sub, label, ret);
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
                        parse(sub, null, null);
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
                    parse(sub, label, ret);
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
                            parse(sub, label, ret);
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
                        parse(sub, label, ret);
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
    Document conentDom;

    private void parseSpan(Span node, OpCode loop_start, OpCode loop_end) {
        if (node == null) {
            return;
        }
        if (false) {
            System.out.println(node.getSource());
            return;
        }
        switch (node.getNodeType()) {
            case Node.LEXS_BODY: {
                if (this.conentDom == null) {
                    throw new java.lang.RuntimeException("语法错误，布局模板不能单独解析");
                }
                this.parse(conentDom);
                return;
            }
            case Node.LEXS_BREAK:
                if (loop_end == null) {
                    throw new java.lang.RuntimeException("语法错误，不在循环中");
                }
                codes.add(OpCode.create(OpCodes.JUMP, loop_end));
                return;
            case Node.LEXS_CALL:
            case Node.LEXS_CCALL:
                break;
            case Node.LEXS_CONTINUE:
                if (loop_start == null) {
                    throw new java.lang.RuntimeException("语法错误，不在循环中");
                }
                codes.add(OpCode.create(OpCodes.JUMP, loop_start));
                return;
            case Node.LEXS_RAW:
            case Node.LEXS_ENDBLOCK:
            case Node.LEXS_ENDEACH:
            case Node.LEXS_ENDFOR:
            case Node.LEXS_ENDIF:
            case Node.LEXS_ENDWHILE:
            case Node.LEXS_EXIT:
            case Node.LEXS_INCLUDE:
            case Node.LEXS_LAYOUT:

                break;
            case Node.LEXS_PLACE: {
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
                index = this.parseIdentifier(source, found + 1);
                boolean required = false;
                if (index < 0) {
                    required = false;
                } else {
                    String tmp = source.substring(found + 1, index).trim();
                    if ("".equals(tmp) || "false".equals(tmp)) {
                        required = false;
                    } else if ("true".equals(tmp)) {
                        required = true;
                    } else {
                        this.reportError("必须是 true 或 false");
                    }
                }

                if (conentDom == null || conentDom.blocks == null || !conentDom.blocks.containsKey(id)) {//TODO: 处理失败？
                    if (required) {
                        this.reportError("block 是必须的 但未提供或未找到");
                    } else {
                        return;
                    }
                }

                Block block = conentDom.blocks.get(id);

                for (Node sub : block.getNodes()) {
                    this.parse(sub, loop_start, loop_end);//是否需要传loop?
                }
                return;
            }
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
    private OpCode findSwap(List<OpCode> l) {
        if (!canSwap) {
            return null;
        }
        LinkedList<OpCode> link;

        if (l instanceof Tuple) {
            link = ((Tuple) l).list;
        } else {
            link = (LinkedList) l;
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

    protected void parseExpr(String source, int index, List<OpCode> link, AtomicInteger argCount) {
        if (source == null || index >= source.length()) {
            return;
        }

        for (; index < source.length(); index++) {
            if (this.isWhitespace(source.charAt(index))) {//空白
                //ignored
            } else if (source.charAt(index) == '@' || source.charAt(index) == '.' || this.isLetter(source.charAt(index))) {//视图对象
                boolean local = false;
                boolean memberacc = false;
                if (source.charAt(index) == '@') {
                    local = true;//TODO: 仅调用
                    index++;
                } else if (source.charAt(index) == '.') {
                    memberacc = true;//移动到下部
                    index++;
                }

                int tmp = parseIdentifier(source, index);
                if (tmp < 0) {
                    this.reportError("syntax error,illegal identifier at " + index);
                }
                String id = source.substring(index, tmp);
                index = tmp - 1;
                OpCode swap = findSwap(link);
                if (id.equals("true") || id.equals("false") || id.equals("null")) {
                    link.add(OpCode.create(OpCodes.LOAD_STR, "[!--SYS-TYPE--$" + id + "]"));
                    if (swap != null) {
                        link.add(swap);
                    }
                    return;
                }
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
                int tinx = 0;
                int tmp2 = 0;
                if ((tmp = this.assertKeyword("[", source, index + 1)) > -1) {
                    type = 1;//索引
                    tinx = tmp;
                    tmp = this.parseRange(source, tmp + 1, "[", "]");
                } else if ((tmp = this.assertKeyword("(", source, index + 1)) > -1) {
                    type = 2;//函数
                    tinx = tmp;
                    tmp = this.parseRange(source, tmp + 1, "(", ")");
                }
                if (type != 0) {
                    if (tmp < 0) {
                        this.reportError("syntax error,illegal characters at " + index);
                    }
                    LinkedList<OpCode> args = new LinkedList<>();
                    AtomicInteger count = new AtomicInteger(0);
                    this.parseExpr(source.substring(tinx, tmp), 0, args, count);//参数
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
                OpCode swap = findSwap(link);
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
                Tuple tup = new Tuple();
                this.parseExpr(source.substring(index + 1, tmp), 0, tup, new AtomicInteger());
                link.add(tup);
                index = tmp + 1;
            } else if (source.charAt(index) == '\'') {//字符串
                int tmp = this.parseRange(source, index + 1, "'", "'");
                if (tmp < 0) {
                    this.reportError("syntax error,illegal characters at " + index);
                }
                OpCode swap = findSwap(link);
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
                    OpCode swap = findSwap(link);
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

    private void setAddress(OpCode code) {
        if (code instanceof Tuple) {
            for (OpCode sub : (Tuple) code) {
                setAddress(sub);
            }
        } else {
            code.setAdress(this.newAddress());
        }
    }

    public void compile(String source, String target) throws IOException {
        this.addr=0;
        this.canSwap=false;
        this.cnode=null;
        this.codes.clear();
        this.conentDom=null;
        this.dom=null;
        this.filename=null;
        this.inComment=false;
        this.inLiteral=false;
        this.parent=null;
        this.reader=null;
        try (java.io.FileInputStream in = new java.io.FileInputStream(source)) {
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(in,"utf-8"));
            filename = source;
            this.parse();
            reader.close();
            reader=null;
        }

        try (java.io.FileOutputStream out = new java.io.FileOutputStream(target)) {            
            this.compile(out);
            out.close();
        }
    }

    public void compile(OutputStream output) throws IOException {

        if (parent != null) {
            parent.compile(output);
            return;
        }

        //this.parse();
        for (OpCode code : codes) {
            setAddress(code);
        }

        for (OpCode code : codes) {
            this.compile(code, output);
        }
    }
    Charset charset=Charset.forName("utf-8");
    private void compile(OpCode code, OutputStream output) throws IOException {
        if (code instanceof Tuple) {
            for (OpCode sub : (Tuple) code) {
                compile(sub, output);
            }
            return;
        }

        if (!OpCodes.DOM.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getAddress()));
            output.write(Utility.toBytes(code.getCode().getValue()));
        }
        if (OpCodes.BEGIN_BLOCK.equals(code.getCode())) {
        } else if (OpCodes.CONDITION_JUMP.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getElement(0).getAddress()));
            output.write(Utility.toBytes(code.getElement(1).getAddress()));
        } else if (OpCodes.JUMP.equals(code.getCode()) || OpCodes.JUMP_FLASE.equals(code.getCode()) || OpCodes.JUMP_TRUE.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getElement(0).getAddress()));
        } else if (OpCodes.DOM.equals(code.getCode())) {
            output.write("OTPL-IL".getBytes(charset));//id7
            output.write(Utility.toBytes((short) 11));//ver
            output.write(0x1);//utf-8
            
            output.write(Utility.toBytes(0L));//file mod time8
            output.write(Utility.toBytes(0L));//gen time8
        } else if (OpCodes.END_BLOCK.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getElement(0).getAddress()));
        } else if (OpCodes.EXIT.equals(code.getCode())) {

        } else if (OpCodes.LOAD_ITERATOR.equals(code.getCode())) {

        } else if (OpCodes.LOAD_NUMBER.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getNumber()));
        } else if (OpCodes.LOAD_VAR.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes(charset);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.LOAD_INTEGER.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getLong()));//
        } else if (OpCodes.LOAD_PROPERTY.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes(charset);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.LOAD_METHOD.equals(code.getCode())) {
            output.write(Utility.toBytes(code.getInt()));
            byte[] bytes = code.getString().getBytes(charset);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.LOAD_STR.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes(charset);
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
            byte[] bytes = code.getString().getBytes(charset);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else if (OpCodes.SET_VAR.equals(code.getCode())) {
            byte[] bytes = code.getString().getBytes(charset);
            output.write(Utility.toBytes(bytes.length));
            output.write(bytes);
        } else {
            this.reportError("未实现命令");
        }
    }

    private void compileT(OpCode code, OutputStream output) throws IOException {
        String s = "";
        if (!OpCodes.DOM.equals(code.getCode())) {
            s += code.getAddress() + " ";
            s += code.getCode().getName() + " ";
        }
        if (OpCodes.BEGIN_BLOCK.equals(code.getCode())) {
        } else if (OpCodes.CONDITION_JUMP.equals(code.getCode())) {
            s += code.getElement(0).getAddress() + " ";
            s += code.getElement(1).getAddress() + " ";
        } else if (OpCodes.DOM.equals(code.getCode())) {
            s += "OTPL-IL";
            s += (short) 11;
            s += " ";
            s += (byte) 11;
            s += " ";
            s += mano.DateTime.now() + " ";
            s += mano.DateTime.now() + " ";
            s += this.addr + " ";
        } else if (OpCodes.END_BLOCK.equals(code.getCode())) {
            s += code.getElement(0).getAddress() + " ";
        } else if (OpCodes.EXIT.equals(code.getCode())) {

        } else if (OpCodes.JUMP.equals(code.getCode())) {
            s += code.getElement(0).getAddress() + " ";
        } else if (OpCodes.LOAD_NUMBER.equals(code.getCode())) {
            s += code.getNumber() + " ";
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
        s += "\r\n";
        output.write(s.getBytes());
    }
}

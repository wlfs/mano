/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.otpl.python;

import mano.otpl.Node;
import mano.otpl.Parser;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class PyParaser extends Parser {
    
    protected String parseExpr(String s, int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = index; i < s.length(); i++) {
            //each:name coll=>x,y{}
            //{$name.call()} //$name object.getName()
            //loop:i true
            //for:i 0:100
            //func:xyz yy=uu xx=hh hh=jj
            //item | xy=hh xx=hh
            //item.prop | xy=hh xx=hh
            //item[index] | xy=hh xx=hh
            //if xy<10 && yy
            //call(abc,dbc)
            //@call(params)
            //var:x=10
            //var:x
            //x+y 数字运算
            //str('join'，'x','y') 连接字符串
            if (this.isWhitespace(s.charAt(i))) {//空白
                continue;
            } else if (s.charAt(i) == '@') {//函数调用，不输出回值
                int end = parseIdentifier(s, i + 1);
                if (end < 0) {
                    this.reportError("syntax error,illegal identifier at " + i);
                }
                //new MemberNode(id,false);
                i = end + 1;
                if (s.charAt(i) == '.') { //成员
                    //new MemberNode(id,parent);
                } else if (s.charAt(i) == '[') { //索引
                    //new IndexNode(id,parent);
                    int tmp = this.parseRange(s, i + 1, "[", "]");
                    if (tmp < 0) {
                        this.reportError("syntax error,illegal characters at " + i);
                    }
                    sb.append('[');
                    sb.append(this.parseExpr(s.substring(i + 1, tmp), 0));
                    sb.append(']');
                    i = tmp + 1;
                } else if (s.charAt(i) == '(') { //正确，调用
                    //get params
                    //call(expr,const) //end
                    int tmp = this.parseRange(s, i + 1, "(", ")");
                    if (tmp < 0) {
                        this.reportError("syntax error,illegal characters at " + i);
                    }
                    sb.append('(');
                    sb.append(this.parseExpr(s.substring(i + 1, tmp), 0));
                    sb.append(')');
                    i = tmp + 1;
                }
            } else if (s.charAt(i) == '$') {//访问临时变量
                int end = parseIdentifier(s, i + 1);
                if (end < 0) {
                    this.reportError("syntax error,illegal identifier at " + i);
                }
                sb.append(s.substring(i + 1, end));
                i = end;
                //if (i < s.length() && !(s.charAt(i) == '.' || s.charAt(i) == '[' || s.charAt(i) == '(' || s.charAt(i) == ' ')) {
                //    this.reportError("syntax error,illegal characters at " + i);
                //}
                i--;
            } else if (s.charAt(i) == '(') {//域或函数
                int tmp = this.parseRange(s, i + 1, "(", ")");
                if (tmp < 0) {
                    this.reportError("syntax error,illegal characters at " + i);
                }
                sb.append('(');
                String x = s.substring(i + 1, tmp);
                sb.append(this.parseExpr(s.substring(i + 1, tmp), 0));
                sb.append(')');
                i = tmp;
            } else if (s.charAt(i) == '\'') {//字符串
                int tmp = this.parseRange(s, i + 1, "'", "'");
                if (tmp < 0) {
                    this.reportError("syntax error,illegal characters at " + i);
                }
                sb.append('"');
                sb.append(s.substring(i + 1, tmp));
                sb.append('"');
                i = tmp;
            } else if (s.charAt(i) == '.') {//访问属性
                int tmp = parseIdentifier(s, i + 1);
                if (tmp < 0) {
                    this.reportError("syntax error,illegal identifier at " + i);
                }
                sb.append('.');
                sb.append(s.substring(i + 1, tmp));
                i = tmp - 1;

            } else if (s.charAt(i) == '[') {//访问索引
                int tmp = this.parseRange(s, i + 1, "[", "]");
                if (tmp < 0) {
                    this.reportError("syntax error,illegal characters at " + i);
                }
                sb.append('[');
                sb.append(this.parseExpr(s.substring(i + 1, tmp), 0));
                sb.append(']');
                i = tmp;
            } else if (s.charAt(i) == '+') {//运算符
                sb.append('+');
            } else if (s.charAt(i) == '-') {//运算符
                sb.append('-');
            } else if (s.charAt(i) == '*') {//运算符
                sb.append('+');
            } else if (s.charAt(i) == '/') {//运算符
                sb.append('+');
            } else if (s.charAt(i) == '%') {//运算符
                sb.append('%');
            } else if (s.charAt(i) == '=' && i + 1 < s.length() && s.charAt(i + 1) == '=') {//运算符
                sb.append("==");
                i += 1;
            } else if (s.charAt(i) == '!' && i + 1 < s.length() && s.charAt(i + 1) == '=') {//运算符
                sb.append("!=");
                i += 1;
            } else if (s.charAt(i) == '>' && i + 1 < s.length() && s.charAt(i + 1) == '=') {//运算符
                sb.append(">=");
                i += 1;
            } else if (s.charAt(i) == '<' && i + 1 < s.length() && s.charAt(i + 1) == '=') {//运算符
                sb.append("<=");
                i += 1;
            } else if (s.charAt(i) == '>') {//运算符
                sb.append('>');
            } else if (s.charAt(i) == '<') {//运算符
                sb.append('<');
            } else if (s.charAt(i) == ',') {//参数
                sb.append(',');
            } else if (this.isLetter(s.charAt(i))) {//视图对象
                int tmp = parseIdentifier(s, i);
                if (tmp < 0) {
                    this.reportError("syntax error,illegal identifier at " + i);
                }
                sb.append(s.substring(i, tmp));//Python 调用与本地调用一样，不需要特殊处理
                i = tmp;
                //if (i < s.length() && !(s.charAt(i) == '.' || s.charAt(i) == '[' || s.charAt(i) == '(' || s.charAt(i) == ' ')) {
                //    this.reportError("syntax error,illegal characters at " + i);
                //}
                i--;
            } else if (this.isDigital(s.charAt(i))) {//数字
                int tmp = this.parseNumber(s, i);
                if (tmp < 0) {
                    this.reportError("syntax error,illegal identifier at " + i);
                }
                sb.append(s.substring(i, tmp));
                i = tmp;
                if (i < s.length() && s.charAt(i) == '.') {
                    tmp = this.parseNumber(s, i + 1);
                    if (tmp < 0) {
                        this.reportError("syntax error,illegal identifier at " + i);
                    }
                    sb.append('.');
                    sb.append(s.substring(i + 1, tmp));
                    i = tmp;
                }
                i--;
            } else {
                this.reportError("syntax error,illegal characters at " + i);
            }
        }
        return sb.toString();
    }

    @Override
    public void parse(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

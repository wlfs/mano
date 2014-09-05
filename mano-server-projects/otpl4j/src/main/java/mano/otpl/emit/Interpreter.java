/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl.emit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import mano.otpl.OtplViewEngine;
import mano.util.LinkedMap;
import mano.util.LinkedMap.LinkedNode;
import mano.util.Utility;

/**
 * 实现 Open-TPL 编译文件(OTC)的解释器。
 *
 * @see https://github.com/diosay/open-tpl
 * @author jun <jun@diosay.com>
 */
public class Interpreter {

    InputStream stream;
    String version;
    byte[] buf;
    String charsetName;
    long sourceLastMTime;
    long ilCTime;
    LinkedMap<Long, OpCode> cmds;
    LinkedNode<Long, OpCode> current;
    boolean running = true;
    //Stack<Object> stack;
    //Map<String, Object> args;
    OutputStream output;
    static final String TRUE = "[!--SYS-TYPE--$true]";
    static final String FLASE = "[!--SYS-TYPE--$false]";
    static final String NULL = "[!--SYS-TYPE--$null]";
    OtplViewEngine.OutProxy environment;

    public void set(String name, Object val) {
        //args.put(name, val);
    }

    public void setOut(OutputStream stream) {
        this.output = stream;
    }

    public void init(OtplViewEngine.OutProxy env) {
        buf = new byte[26];
        cmds = new LinkedMap<>();
        environment = env;

    }

    public void exec(String filename) throws FileNotFoundException, IOException {
        stream = new FileInputStream(filename);

        this.readHeader();

        if (current == null) {
            getCode();
            current = cmds.getFirstNode();
            exec(current);
        }
        while (running) {

            if (current.getNext() == null) {
                getCode();
            }
            if (current.getNext() == null) {
                exec(cmds.getLastNode());
            } else {
                exec(current.getNext());
            }
        }
        current = null;
        running = true;
        stream.close();
        stream = null;
        //LinkedList<int> f;
    }

    private void error(String msg) {
        throw new java.lang.RuntimeException(msg);
    }

    private void readHeader() throws IOException {
        int len = stream.read(buf, 0, 26);
        if (len != 26) {
            error("输入错误。");
        }
        version = new String(buf, 0, 7);
        version += " " + Utility.toShort(buf, 7);
        if (buf[9] == 1) {
            charsetName = "utf-8";
        } else {
            charsetName = "utf-8";//default
        }
        sourceLastMTime = Utility.toLong(buf, 10);
        ilCTime = Utility.toLong(buf, 18);

    }

    private byte[] getString(int left) throws IOException {
        int len = 0;
        int index = 0;
        byte[] arg = new byte[left];
        while (left > 0) {
            if (left >= left - index) {
                len = stream.read(arg, index, left - index);
            } else {
                len = stream.read(arg, index, left);
            }
            index += len;
            left -= len;
        }
        return arg;
    }

    private OpCode getCode() throws IOException {
        int len = stream.read(buf, 0, 10);
        if (len != 10) {
            error("输入错误。");
        }

        long addr = Utility.toLong(buf, 0);
        OpCodes op = OpCodes.parse(buf, 8);
        OpCode code = null;
        if (OpCodes.BEGIN_BLOCK.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.CONDITION_JUMP.equals(op)) {
            len = stream.read(buf, 0, 16);
            if (len != 16) {
                error("输入错误。");
            }
            code = OpCode.create(op,
                    OpCode.create(OpCodes.NOP).setAdress(Utility.toLong(buf, 0)),
                    OpCode.create(OpCodes.NOP).setAdress(Utility.toLong(buf, 8)))
                    .setAdress(addr);
        } else if (OpCodes.DOM.equals(op)) {
            error("错误的指令。");
        } else if (OpCodes.END_BLOCK.equals(op)) {
            len = stream.read(buf, 0, 8);
            if (len != 8) {
                error("输入错误。");
            }
            code = OpCode.create(op,
                    OpCode.create(OpCodes.NOP).setAdress(Utility.toLong(buf, 0)))
                    .setAdress(addr);
        } else if (OpCodes.EXIT.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.JUMP.equals(op) || OpCodes.JUMP_FLASE.equals(op) || OpCodes.JUMP_TRUE.equals(op)) {
            len = stream.read(buf, 0, 8);
            if (len != 8) {
                error("输入错误。");
            }
            code = OpCode.create(op,
                    OpCode.create(OpCodes.NOP).setAdress(Utility.toLong(buf, 0)))
                    .setAdress(addr);
        } else if (OpCodes.LOAD_NUMBER.equals(op)) {
            len = stream.read(buf, 0, 8);
            if (len != 8) {
                error("输入错误。");
            }
            code = OpCode.create(op, Utility.toDouble(buf, 0))
                    .setAdress(addr);
        } else if (OpCodes.LOAD_VAR.equals(op)) {
            len = stream.read(buf, 0, 4);
            if (len != 4) {
                error("输入错误。");
            }
            code = OpCode.create(op, getString(Utility.toInt(buf, 0)))
                    .setAdress(addr);

        } else if (OpCodes.LOAD_INTEGER.equals(op)) {
            len = stream.read(buf, 0, 8);
            if (len != 8) {
                error("输入错误。");
            }
            code = OpCode.create(op, Utility.toLong(buf, 0))
                    .setAdress(addr);
        } else if (OpCodes.LOAD_PROPERTY.equals(op)) {
            len = stream.read(buf, 0, 4);
            if (len != 4) {
                error("输入错误。");
            }
            code = OpCode.create(op, getString(Utility.toInt(buf, 0)))
                    .setAdress(addr);
        } else if (OpCodes.LOAD_METHOD.equals(op)) {
            len = stream.read(buf, 0, 8);
            if (len != 8) {
                error("输入错误。");
            }
            //Utility.toInt(buf, 0),
            code = OpCode.create(op, Utility.toInt(buf, 0), getString(Utility.toInt(buf, 4)))
                    .setAdress(addr);
        } else if (OpCodes.LOAD_STR.equals(op)) {
            len = stream.read(buf, 0, 4);
            if (len != 4) {
                error("输入错误。");
            }
            code = OpCode.create(op, getString(Utility.toInt(buf, 0)))
                    .setAdress(addr);
        } else if (OpCodes.CALL.equals(op)) {
            len = stream.read(buf, 0, 4);
            if (len != 4) {
                error("输入错误。");
            }
            code = OpCode.create(op, Utility.toInt(buf, 0)).setAdress(addr);
        } else if (OpCodes.LOAD_ITERATOR.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.INDEXER.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.NOP.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_ADD.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_AND.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_DIV.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_EQ.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_GT.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_GTE.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_LT.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_LTE.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_MOD.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_MUL.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_NEQ.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_NOT.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_OR.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.OP_SUB.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.PRINT.equals(op)) {
            code = OpCode.create(op).setAdress(addr);
        } else if (OpCodes.PRINT_STR.equals(op)) {
            len = stream.read(buf, 0, 4);
            if (len != 4) {
                error("输入错误。");
            }
            code = OpCode.create(op, getString(Utility.toInt(buf, 0)))
                    .setAdress(addr);

        } else if (OpCodes.SET_VAR.equals(op)) {
            len = stream.read(buf, 0, 4);
            if (len != 4) {
                error("输入错误。");
            }
            code = OpCode.create(op, getString(Utility.toInt(buf, 0)))
                    .setAdress(addr);
        }

        if (code == null) {
            error("错误的指令");
        }

        cmds.addLast(addr, code);
        return code;
    }

    private void next() throws IOException {
        if (current.getNext() == null) {
            getCode();
        }
        exec(current.getNext());
    }

    private void jump(long addr) throws IOException {
        if (cmds.containsKey(addr)) {
            exec(cmds.get(addr));
        } else {
            while (true) {
                if (addr == getCode().getAddress()) {
                    exec(cmds.get(addr));
                    break;
                }
            }
        }
    }

    private void exec(LinkedNode<Long, OpCode> node) throws IOException {
        current = node;
        OpCode code = node.getValue();

        if (OpCodes.BEGIN_BLOCK.equals(code.getCode())) {
            next();
        } else if (OpCodes.CONDITION_JUMP.equals(code.getCode())) {
            if (toBoolean(environment.stack.pop())) {
                jump(code.getElement(0).getAddress());
            } else {
                jump(code.getElement(1).getAddress());
            }
        } else if (OpCodes.JUMP_FLASE.equals(code.getCode())) {
            if (!toBoolean(environment.stack.pop())) {
                jump(code.getElement(0).getAddress());
            }
        } else if (OpCodes.JUMP_TRUE.equals(code.getCode())) {
            if (toBoolean(environment.stack.pop())) {
                jump(code.getElement(0).getAddress());
            }
        } else if (OpCodes.DOM.equals(code.getCode())) {
            error("非法指令");
        } else if (OpCodes.END_BLOCK.equals(code.getCode())) {
            next();
        } else if (OpCodes.EXIT.equals(code.getCode())) {
            this.running = false;
        } else if (OpCodes.JUMP.equals(code.getCode())) {
            jump(code.getElement(0).getAddress());
        } else if (OpCodes.LOAD_NUMBER.equals(code.getCode())) {
            environment.stack.push(code.getNumber());
        } else if (OpCodes.LOAD_VAR.equals(code.getCode())) {
            String key = new String(code.getBytes());
            if (key == null || "".equals(key) || !environment.args.containsKey(key)) {
                throw new java.lang.RuntimeException("变量未定义：" + key);
            }
            environment.stack.push(environment.args.get(key));
        } else if (OpCodes.LOAD_INTEGER.equals(code.getCode())) {
            environment.stack.push(code.getLong());
        } else if (OpCodes.LOAD_METHOD.equals(code.getCode())) {
            String key = new String(code.getBytes());
            environment.stack.push(getMethod(environment.stack.pop(), key, code.getInt()));
        } else if (OpCodes.LOAD_PROPERTY.equals(code.getCode())) {
            String key = new String(code.getBytes());
            environment.stack.push(getProperty(environment.stack.pop(), key));
        } else if (OpCodes.LOAD_STR.equals(code.getCode())) {
            String str = new String(code.getBytes());
            switch (str) {
                case FLASE:
                    environment.stack.push(false);
                    break;
                case TRUE:
                    environment.stack.push(true);
                    break;
                case NULL:
                    environment.stack.push(null);
                    break;
                default:
                    environment.stack.push(str);
                    break;
            }

        } else if (OpCodes.CALL.equals(code.getCode())) {
            Object result = call(code.getInt());
            if (result != null && result instanceof Void) {
            } else {
                environment.stack.push(result);
            }
        } else if (OpCodes.LOAD_ITERATOR.equals(code.getCode())) {
            environment.stack.push(this.toIterator(environment.stack.pop()));
        } else if (OpCodes.INDEXER.equals(code.getCode())) {
            Object result = call(code.getInt());
            if (result != null && result instanceof Void) {

            } else {
                environment.stack.push(result);
            }
        } else if (OpCodes.NOP.equals(code.getCode())) {
            //pass
        } else if (OpCodes.OP_ADD.equals(code.getCode())
                || OpCodes.OP_SUB.equals(code.getCode())
                || OpCodes.OP_MUL.equals(code.getCode())
                || OpCodes.OP_DIV.equals(code.getCode())
                || OpCodes.OP_MOD.equals(code.getCode())
                || OpCodes.OP_GT.equals(code.getCode())
                || OpCodes.OP_GTE.equals(code.getCode())
                || OpCodes.OP_LT.equals(code.getCode())
                || OpCodes.OP_LTE.equals(code.getCode())) {
            Object b = environment.stack.pop();
            Object a = environment.stack.pop();
            environment.stack.push(number_op(code.getCode(), a, b));
        } else if (OpCodes.OP_AND.equals(code.getCode()) || OpCodes.OP_OR.equals(code.getCode())) {
            Object b = environment.stack.pop();
            Object a = environment.stack.pop();
            environment.stack.push(logic_op(code.getCode(), a, b));
        } else if (OpCodes.OP_EQ.equals(code.getCode())) {
            Object b = environment.stack.pop();
            Object a = environment.stack.pop();
            if (a != null) {
                environment.stack.push(a.equals(b));
            } else if (b != null) {
                environment.stack.push(b.equals(a));
            } else {
                environment.stack.push(a == b);
            }
        } else if (OpCodes.OP_NEQ.equals(code.getCode())) {
            Object b = environment.stack.pop();
            Object a = environment.stack.pop();
            if (a != null) {
                environment.stack.push(!a.equals(b));
            } else if (b != null) {
                environment.stack.push(!b.equals(a));
            } else {
                environment.stack.push(a != b);
            }
        } else if (OpCodes.OP_NOT.equals(code.getCode())) {
            Object a = environment.stack.pop();
            if (a != null) {
                environment.stack.push(!this.toBoolean(a));
            } else {
                environment.stack.push(true); //空表示“非”，反则为真？
            }
        } else if (OpCodes.PRINT.equals(code.getCode())) {
            Object ob = environment.stack.pop();

            print(ob);
        } else if (OpCodes.PRINT_STR.equals(code.getCode())) {
            printStr(code.getBytes());
        } else if (OpCodes.SET_VAR.equals(code.getCode())) {
            String name = new String(code.getBytes());
            Object val = environment.stack.pop();
            environment.args.put(name, val);
        }
    }

    private void print(Object obj) {
        if(obj==null){
            return;
        }
        try {
            printStr(obj.toString().getBytes(charsetName));
        } catch (UnsupportedEncodingException ex) {
            throw new java.lang.RuntimeException(ex);
        }
    }

    private void printStr(byte[] bytes) {
        try {
            output.write(bytes);
        } catch (IOException ex) {
            throw new java.lang.RuntimeException(ex);
        }
        //System.out.print(new String(bytes));
    }

    private boolean toBoolean(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Boolean) {
            return (boolean) obj;
        }

        return Boolean.class.cast(obj);
    }

    private Object getMethod(Object obj, String name, int argCount) throws IOException {
        Class<?> clazz = obj.getClass();
        if (clazz.isPrimitive()) {
            error("数据类型不匹配，不是一个效果的对象。");
        }
        Object result = null;

        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(name) && m.getParameterCount() == argCount) {
                if (result != null) {
                    error("对象存在多个同名成员函数，" + name);
                }
                result = m;
                break;//TODO:对象存在多个同名成员函数?????
            }
        }

        if (result == null) {
            error("成员方法未找到或不存在，" + name);
        } else {
            //stack.push(argCount);
            environment.stack.push(obj);//返回栈顶，供调用使用
        }
        return result;
    }

    private Object getProperty(Object obj, String name) throws IOException {
        Class<?> clazz = obj.getClass();
        if (clazz.isPrimitive()) {
            error("数据类型不匹配，不是一个效果的对象。");
        }
        Object result = null;

        String tmp = null;
        if (name.startsWith("get") && name.length() >= 4) {
            if (!Character.isUpperCase(name.charAt(0))) {
                tmp = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
        } else {
            tmp = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        if (tmp != null) {
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                if (m.getName().equalsIgnoreCase(tmp)) {
                    if (result != null) {
                        error("存在多个对象成员，" + tmp);
                    }
                    result = m;
                }
            }
        }
        if (result == null) {
            Field[] fields = clazz.getFields();
            for (Field m : fields) {
                if (m.getName().equalsIgnoreCase(name)) {
                    if (result != null) {
                        error("存在多个对象成员，" + name);
                    }
                    result = m;
                }
            }
        }

        if (result == null) {
            error("属性未找到或不存在，" + name);
        } else {
            //stack.push(0);
            environment.stack.push(obj);//返回栈顶，供调用使用

        }
        return result;
    }

    private Object call(int argCount) {
        Object[] params = new Object[argCount];
        for (int i = argCount - 1; i >= 0; i--) {
            params[i] = environment.stack.pop();
        }
        Object member = environment.stack.pop();
        Object host = null;
        if (member instanceof Method) {
            Method call = (Method) member;
            //int len=call.getParameterCount();

            if (!Modifier.isStatic(call.getModifiers())) {
                host = environment.stack.pop();
            }
            Class<?>[] types = call.getParameterTypes();
            for (int i = 0; i < argCount; i++) {
                params[i] = Utility.cast(types[i], params[i]);
            }
            call.setAccessible(true);
            try {
                return call.invoke(host, params);
            } catch (InvocationTargetException ex) {
                throw new java.lang.RuntimeException(ex.getTargetException());
                //error("调用函数失败，" + ex.getMessage() + "。" + call.getName());
            } catch (Exception ex) {
                throw new java.lang.RuntimeException(ex);
                //error("调用函数失败，" + ex.getMessage() + "。" + call.getName());
            }
        } else if (member instanceof Field) {
            Field field = (Field) member;
            if (!Modifier.isStatic(field.getModifiers())) {
                host = environment.stack.pop();
            }
            try {
                return field.get(host);
            } catch (Exception ex) {
                error("调用属性（字段）方法函数失败，" + ex.getMessage() + "。" + field.getName());
            }
        }
        error("调用函数失败，对象不匹配。");
        return null;
    }

    private Object number_op(OpCodes op, Object a, Object b) {

        double av = Utility.toDouble(a);
        double bv = Utility.toDouble(b);

        if (OpCodes.OP_ADD.equals(op)) {
            return Utility.asNumber(Math.max(Utility.geTypeCode(a.getClass()),
                    Utility.geTypeCode(b.getClass())),
                    av + bv);
        } else if (OpCodes.OP_DIV.equals(op)) {
            return Utility.asNumber(Math.max(Utility.geTypeCode(a.getClass()),
                    Utility.geTypeCode(b.getClass())),
                    av / bv);
        } else if (OpCodes.OP_GT.equals(op)) {
            return av > bv;
        } else if (OpCodes.OP_GTE.equals(op)) {
            return av >= bv;
        } else if (OpCodes.OP_LT.equals(op)) {
            return av < bv;
        } else if (OpCodes.OP_LTE.equals(op)) {
            return av <= bv;
        } else if (OpCodes.OP_MOD.equals(op)) {
            return Utility.asNumber(Math.max(Utility.geTypeCode(a.getClass()),
                    Utility.geTypeCode(b.getClass())),
                    av % bv);
        } else if (OpCodes.OP_MUL.equals(op)) {
            return Utility.asNumber(Math.max(Utility.geTypeCode(a.getClass()),
                    Utility.geTypeCode(b.getClass())),
                    av * bv);
        } else if (OpCodes.OP_SUB.equals(op)) {
            return Utility.asNumber(Math.max(Utility.geTypeCode(a.getClass()),
                    Utility.geTypeCode(b.getClass())),
                    av - bv);
        }
        this.error("数据类型不匹配，不能作数值运算。");
        return null;
    }

    private boolean logic_op(OpCodes op, Object a, Object b) {

        if (OpCodes.OP_AND.equals(op)) {
            return this.toBoolean(a) && this.toBoolean(b);
        } else if (OpCodes.OP_OR.equals(op)) {
            return this.toBoolean(a) || this.toBoolean(b);
        }

        error("数据类型不匹配，不能作数值运算。");

        return false;
    }

    private Object toIterator(Object obj) {
        if (obj == null) {
            obj = new Object[0];
        }

        if (obj instanceof Iterable) {
            return ((Iterable) obj).iterator();
        } else if (obj instanceof Map) {
            return ((Iterable) ((Map) obj).entrySet()).iterator();
        } else if (obj.getClass().isArray()) {
            final Object array = obj;
            final int size = Array.getLength(obj);
            return new Iterator() {
                int current = 0;

                @Override
                public boolean hasNext() {
                    return current < size;
                }

                @Override
                public Object next() {
                    return Array.get(array, current++);
                }

            };
        }
        this.error("give object is a non-terable object." + obj.getClass());
        return obj;
    }

}

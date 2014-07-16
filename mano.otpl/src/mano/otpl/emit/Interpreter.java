/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl.emit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.util.LinkedMap;
import mano.util.LinkedMap.LinkedNode;
import mano.util.Utility;

/**
 *
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
    Stack<Object> stack;
    Map<String, Object> args;
    OutputStream output;
    static final String TRUE = "[!--SYS-TYPE--$true]";
    static final String FLASE = "[!--SYS-TYPE--$false]";
    static final String NULL = "[!--SYS-TYPE--$null]";

    public static void main(String[] args) {
        EmitParser.mains(args);
        Interpreter interpreter = new Interpreter();
        try {
            interpreter.init();
            interpreter.setOut(System.out);
            interpreter.exec("E:\\repositories\\java\\mano\\mano.server\\server\\tmp\\4c0dbce1.il");
        } catch (IOException ex) {
            Logger.getLogger(Interpreter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String field = "i am is a field";
    public String oprop = "i am is a auto property";

    public String getProp() {
        return "i am is a property(getProp())";
    }

    public int addTest(int a, int b) {
        return a + b;
    }

    public void set(String name, Object val) {
        args.put(name, val);
    }

    public void setOut(OutputStream stream) {
        this.output = stream;
    }

    public void init() {
        buf = new byte[26];
        cmds = new LinkedMap<>();
        stack = new Stack<>();
        args = new HashMap<>();

        args.put("title", "OPTL-IL TEST");
        args.put("obj", this);
        args.put("list", new String[]{"abx", "fttf"});
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
            charsetName = "unkonw";
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
            if (toBoolean(stack.pop())) {
                jump(code.getElement(0).getAddress());
            } else {
                jump(code.getElement(1).getAddress());
            }
        } else if (OpCodes.JUMP_FLASE.equals(code.getCode())) {
            if (!toBoolean(stack.pop())) {
                jump(code.getElement(0).getAddress());
            }
        } else if (OpCodes.JUMP_TRUE.equals(code.getCode())) {
            if (toBoolean(stack.pop())) {
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
            stack.push(code.getNumber());
        } else if (OpCodes.LOAD_VAR.equals(code.getCode())) {
            String key = new String(code.getBytes());
            if (key == null || "".equals(key) || !args.containsKey(key)) {
                throw new java.lang.RuntimeException("变量未定义：" + key);
            }
            stack.push(args.get(key));
        } else if (OpCodes.LOAD_INTEGER.equals(code.getCode())) {
            stack.push(code.getLong());
        } else if (OpCodes.LOAD_METHOD.equals(code.getCode())) {
            String key = new String(code.getBytes());
            stack.push(getMethod(stack.pop(), key, code.getInt()));
        } else if (OpCodes.LOAD_PROPERTY.equals(code.getCode())) {
            String key = new String(code.getBytes());
            stack.push(getProperty(stack.pop(), key));
        } else if (OpCodes.LOAD_STR.equals(code.getCode())) {
            String str = new String(code.getBytes());
            switch (str) {
                case FLASE:
                    stack.push(false);
                    break;
                case TRUE:
                    stack.push(true);
                    break;
                case NULL:
                    stack.push(null);
                    break;
                default:
                    stack.push(str);
                    break;
            }

        } else if (OpCodes.CALL.equals(code.getCode())) {
            Object result = call(code.getInt());
            if (result != null && result instanceof Void) {
            } else {
                stack.push(result);
            }
        } else if (OpCodes.LOAD_ITERATOR.equals(code.getCode())) {
            stack.push(this.toIterator(stack.pop()));
        } else if (OpCodes.INDEXER.equals(code.getCode())) {
            Object result = call(code.getInt());
            if (result != null && result instanceof Void) {

            } else {
                stack.push(result);
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
            Object b = stack.pop();
            Object a = stack.pop();
            stack.push(number_op(code.getCode(), a, b));
        } else if (OpCodes.OP_AND.equals(code.getCode()) || OpCodes.OP_OR.equals(code.getCode())) {
            Object b = stack.pop();
            Object a = stack.pop();
            stack.push(logic_op(code.getCode(), a, b));
        } else if (OpCodes.OP_EQ.equals(code.getCode())) {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a != null) {
                stack.push(a.equals(b));
            } else if (b != null) {
                stack.push(b.equals(a));
            } else {
                stack.push(a == b);
            }
        } else if (OpCodes.OP_NEQ.equals(code.getCode())) {
            Object b = stack.pop();
            Object a = stack.pop();
            if (a != null) {
                stack.push(!a.equals(b));
            } else if (b != null) {
                stack.push(!b.equals(a));
            } else {
                stack.push(a != b);
            }
        } else if (OpCodes.OP_NOT.equals(code.getCode())) {
            Object a = stack.pop();
            if (a != null) {
                stack.push(!this.toBoolean(a));
            } else {
                stack.push(true); //空表示“非”，反则为真？
            }
        } else if (OpCodes.PRINT.equals(code.getCode())) {
            Object ob = stack.pop();
            if (ob == null) {
                int x = 8;
            }
            print(ob);
        } else if (OpCodes.PRINT_STR.equals(code.getCode())) {
            printStr(code.getBytes());
        } else if (OpCodes.SET_VAR.equals(code.getCode())) {
            String name = new String(code.getBytes());
            Object val = stack.pop();
            this.args.put(name, val);
        }
    }

    private void print(Object obj) {
        printStr(obj.toString().getBytes());
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
            stack.push(obj);//返回栈顶，供调用使用
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
            stack.push(obj);//返回栈顶，供调用使用

        }
        return result;
    }

    private Object call(int argCount) {
        Object[] params = new Object[argCount];
        for (int i = argCount - 1; i >= 0; i--) {
            params[i] = stack.pop();
        }
        Object member = stack.pop();
        Object host = null;
        if (member instanceof Method) {
            Method call = (Method) member;
            //int len=call.getParameterCount();

            if (!Modifier.isStatic(call.getModifiers())) {
                host = stack.pop();
            }
            Class<?>[] types = call.getParameterTypes();
            for (int i = 0; i < argCount; i++) {
                params[i] = Number.cast(types[i], params[i]);
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
                host = stack.pop();
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

        double av = Number.toDouble(a);
        double bv = Number.toDouble(b);

        if (OpCodes.OP_ADD.equals(op)) {
            return Number.asNumber(Math.max(Number.geTypeCode(a.getClass()),
                    Number.geTypeCode(b.getClass())),
                    av + bv);
        } else if (OpCodes.OP_DIV.equals(op)) {
            return Number.asNumber(Math.max(Number.geTypeCode(a.getClass()),
                    Number.geTypeCode(b.getClass())),
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
            return Number.asNumber(Math.max(Number.geTypeCode(a.getClass()),
                    Number.geTypeCode(b.getClass())),
                    av % bv);
        } else if (OpCodes.OP_MUL.equals(op)) {
            return Number.asNumber(Math.max(Number.geTypeCode(a.getClass()),
                    Number.geTypeCode(b.getClass())),
                    av * bv);
        } else if (OpCodes.OP_SUB.equals(op)) {
            return Number.asNumber(Math.max(Number.geTypeCode(a.getClass()),
                    Number.geTypeCode(b.getClass())),
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

    static class Number {

        static final int OBJECT = 0,
                NUM_SHORT = 1,
                NUM_INTEGER = 2,
                NUM_LONG = 3,
                NUM_FLOAT = 4,
                NUM_DOUBLE = 5;

        private static int geTypeCode(Class<?> clazz) {
            switch (clazz.getName()) {
                case "long":
                case "java.lang.Long":
                    return Number.NUM_LONG;
                case "int":
                case "java.lang.Integer":
                    return Number.NUM_INTEGER;
                case "double":
                case "java.lang.Double":
                    return Number.NUM_DOUBLE;
                case "float":
                case "java.lang.Float":
                    return Number.NUM_INTEGER;
                case "short":
                case "java.lang.Short":
                    return Number.NUM_SHORT;
                default:
                    return 0;
            }
        }

        private static <T> T cast(Class<T> clazz, Object obj) {
            Object result;
            int code = geTypeCode(clazz);
            switch (code) {
                case Number.NUM_DOUBLE:
                case Number.NUM_FLOAT:
                case Number.NUM_INTEGER:
                case Number.NUM_LONG:
                case Number.NUM_SHORT:
                    result = asNumber(code, toDouble(obj));
                    break;
                default:
                    return clazz.cast(obj);
            }
            return (T) result;
        }

        private static double toDouble(Object obj) {
            return Double.parseDouble(obj.toString());
        }

        private static Object asNumber(int type, double obj) {
            Object result;

            switch (type) {
                case Number.NUM_DOUBLE:
                    return obj;
                case Number.NUM_FLOAT:
                    return (float) obj;
                case Number.NUM_INTEGER:
                    return (int) obj;
                case Number.NUM_LONG:
                    return (long) obj;
                case Number.NUM_SHORT:
                    return (short) obj;
                default:
                    return obj;
            }
        }

    }

}

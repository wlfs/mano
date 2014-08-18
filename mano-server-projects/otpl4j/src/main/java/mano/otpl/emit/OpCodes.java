/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl.emit;

//http://msdn.microsoft.com/zh-cn/library/system.reflection.emit.opcodes(v=vs.110).aspx
import mano.util.Utility;

/**
 *
 * @author jun <jun@diosay.com>
 */
public enum OpCodes {

    /**
     * 什么都不做。
     */
    NOP(0, "nop"),
    /**
     * IL文件的头信息。
     * <p>
     * IL原形：标识([MANO-IL]7)版本(2)字符编码类型(1)原文件最后修改时间(8)IL生成时间(8),共26字节。</p>
     */
    DOM(1, "dom"),
    /**
     * 打印一个指定长度的字符串。
     * <p>
     * IL原形：地址(8)操作(2)字符串长度(4)数据(n)，共 14+n 字节。</p>
     */
    PRINT_STR(2, "ptstr"),
    /**
     * 从栈顶弹出一个对象并打印。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    PRINT(3, "pt"),
    /**
     * 表示一个块的开始。 块的开始会初始化一个独立的变量存储区。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    //@Deprecated
    BEGIN_BLOCK(4, "nop"),
    /**
     * 表示一个块的结束。 块的结束会销毁当前独立的变量存储区。
     * <p>
     * IL原形：地址(8)操作(2)块开始地址(8)，共 18 字节。</p>
     */
    @Deprecated
    END_BLOCK(5, "nop"),
    /**
     * 表示无条件跳到指定地址。
     * <p>
     * IL原形：地址(8)操作(2)目标地址(8)，共 18 字节。</p>
     */
    JUMP(6, "jmp"),
    /**
     * 从栈顶弹出一个对象并判断真假，并跳到指定地址。
     * <p>
     * IL原形：地址(8)操作(2)条件为真的地址(8)条件为假的地址(8)，共 26 字节。</p>
     */
    CONDITION_JUMP(7, "jmp_c"),
    /**
     * 根据索引从变量集合中获取一个对象，并压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)索引长度(4)字符串索引(n)，共 14+n 字节。</p>
     */
    LOAD_VAR(8, "ldloc"),
    /**
     * 从栈顶弹出一个对象，获取其成员函数，并压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)参数个数(4)名称长度(4)名称(n)，共 18+n 字节。</p>
     */
    LOAD_METHOD(9, "ldm_m"),
    /**
     * 从栈顶弹出一个对象，获取其成员属性，并压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)索引长度(4)名称(n)，共 14+n 字节。</p>
     */
    LOAD_PROPERTY(10, "ldm_p"),
    /**
     * 载入一个数字，并压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)数字(8)，共 18 字节。</p>
     */
    LOAD_NUMBER(11, "ldu_db"),
    /**
     * 载入一个整数，并压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)数字(8)，共 18 字节。</p>
     */
    LOAD_INTEGER(12, "ldu_l"),
    /**
     * 载入一个字符串，并压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)字符串长度(4)数据(n)，共 14+n 字节。</p>
     */
    LOAD_STR(13, "ldstr"),
    /**
     * 从栈顶弹出一个对象,根据索引从成员中获取一个对象，并压入栈顶。decimal
     * <p>
     * IL原形：地址(8)操作(2)索引长度(4)名称(n)，共 14+n 字节。</p>
     */
    INDEXER(14, "ldm_i"),
    /**
     * 从栈顶弹出一个函数对象,执行并将返回值（如果有）压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    CALL(15, "call"),
    /**
     * 从栈顶弹出两个对象,相加并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_ADD(16, "add"),
    /**
     * 从栈顶弹出两个对象,相减并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_SUB(17, "sub"),
    /**
     * 从栈顶弹出两个对象,相乘减并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_MUL(18, "mul"),
    /**
     * 从栈顶弹出两个对象,相除并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_DIV(19, "div"),
    /**
     * 从栈顶弹出两个对象,相除取余并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_MOD(20, "mod"),
    /**
     * 从栈顶弹出两个对象,进行等于比较并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_EQ(21, "eq"),
    /**
     * 从栈顶弹出两个对象,进行不等于比较并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_NEQ(22, "neq"),
    /**
     * 从栈顶弹出两个对象,进行大于等于比较并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_GTE(23, "gte"),
    /**
     * 从栈顶弹出两个对象,进行小于等于比较并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_LTE(24, "lte"),
    /**
     * 从栈顶弹出两个对象,进行大于比较并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_GT(25, "gt"),
    /**
     * 从栈顶弹出两个对象,进行小于比较并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_LT(26, "lt"),
    /**
     * 从栈顶弹出一个对象,进行取反并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_NOT(27, "not"),
    /**
     * 从栈顶弹出两个对象,进行“与”运算并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_AND(28, "and"),
    /**
     * 从栈顶弹出两个对象,进行“或”运算并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_OR(29, "or"),
    /**
     * 完成并退出执行。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    EXIT(30, "exit"),
    /**
     * 从栈顶弹出一个对象,并将该对幅值给索引的变量 。
     * <p>
     * IL原形：地址(8)操作(2)索引长度(4)名称(n)，共 14+n 字节。</p>
     */
    SET_VAR(31, "stloc"),
    /**
     * 从栈顶弹出一个对象,取相反数并将结果压入栈顶。????
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    @Deprecated
    OP_OPPOSITE(32, "opp"),
    /**
     * 从栈顶弹出一个对象,转换为迭代器并将结果压入栈顶。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    LOAD_ITERATOR(33, "ldcur"),
    /**
     * 从栈顶弹出一个对象,如果该值为true则跳到指定地址。
     * <p>
     * IL原形：地址(8)操作(2)目标地址(8)，共 18 字节。</p>
     */
    JUMP_TRUE(34, "jmp_t"),
    /**
     * 从栈顶弹出一个对象,如果该值为flast则跳到指定地址。
     * <p>
     * IL原形：地址(8)操作(2)目标地址(8)，共 18 字节。</p>
     */
    JUMP_FLASE(35, "jmp_f"),
    /**
     * 从栈顶弹出一个对象。
     * <p>
     * IL原形：地址(8)操作(2)，共 10 字节。</p>
     */
    OP_POP(36, "pop"),/**
     * 包含一个外部IL文件并执行。
     * <p>
     * IL原形：地址(8)操作(2)字符串长度(4)数据(n)，共 14+n 字节。</p>
     */
    INCLUDE(37, "inc"),
    //http://msdn.microsoft.com/zh-cn/library/34dk387t.aspx
    /* 定义一个宏用于DEBUG调试。
     * <p>
     * IL原形：地址(8)操作(2)原行号(4)文件名长度(4)文件名(n)，共 18+n 字节。</p>
     */
    LINE(38, "line"),;

    private final short value;
    private final String name;

    private OpCodes(int val, String op) {
        value = (short) val;
        name = op;
    }

    public String getName() {
        return name;
    }

    public short getValue() {
        return this.value;
    }

    public static OpCodes parse(byte[] bytes, int index) {
        short val = Utility.toShort(bytes, index);
        for (OpCodes code : OpCodes.values()) {
            if (val == code.value) {
                return code;
            }
        }
        throw new mano.InvalidOperationException();
    }

    public final boolean equals(OpCodes other) {
        return this.value == other.value;
    }
}

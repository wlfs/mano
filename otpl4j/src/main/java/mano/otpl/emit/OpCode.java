/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl.emit;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class OpCode {

    private long address;
    private OpCode[] elements;
    private OpCodes opcode;
    private String arg;
    private double decArg;
    private long longArg;
    private int intArg;
    private byte[] bytesArg;
    protected OpCode() {

    }

    public OpCode setAdress(long addr) {
        address = addr;
        return this;
    }

    public long getAddress() {
        return address;
    }

    public OpCode getElement(int index) {
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        if (this.elements == null || this.elements.length <= index) {
            return null;
        }
        return this.elements[index];
    }
    
    public String getString(){
        return this.arg;
    }
    
    public double getNumber(){
        return this.decArg;
    }
    
    public long getLong(){
        return this.longArg;
    }
    public int getInt(){
        return this.intArg;
    }
    public byte[] getBytes(){
        return this.bytesArg;
    }

    public OpCodes getCode() {
        return this.opcode;
    }
     public static OpCode label() {
         return create(OpCodes.NOP);
     }
    public static OpCode create(OpCodes opcode, OpCode... elements) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.elements = elements;
        return code;
    }

    public static OpCode create(OpCodes opcode, String arg) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.arg = arg;
        return code;
    }
    
    public static OpCode create(OpCodes opcode, long arg) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.longArg = arg;
        return code;
    }
    
    public static OpCode create(OpCodes opcode, int arg) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.intArg = arg;
        return code;
    }
    
    public static OpCode create(OpCodes opcode, double arg) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.decArg = arg;
        return code;
    }
    public static OpCode create(OpCodes opcode, byte[] arg) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.bytesArg = arg;
        return code;
    }
    
    public static OpCode create(OpCodes opcode, int i,String s) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.intArg = i;
        code.arg = s;
        return code;
    }
    public static OpCode create(OpCodes opcode, int i,byte[] b) {
        OpCode code = new OpCode();
        code.opcode = opcode;
        code.intArg = i;
        code.bytesArg=b;
        return code;
    }
}

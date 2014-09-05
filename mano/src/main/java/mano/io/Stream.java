/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 表示一个封装了 InputStream 与 OutputStream的API的抽象流。
 * @author jun <jun@diosay.com>
 */
public class Stream {
    
    public final InputStream input;
    
    public final OutputStream output;
    
    public Stream(InputStream in,OutputStream out){
        this.input=in;
        this.output=out;
    }
    
    public synchronized long available() throws IOException{
        return this.input.available();
    }
    
    public synchronized void write(byte[] buf, int index, int count) throws IOException{
        this.output.write(buf, index, count);
    }
    
    public synchronized void write(byte[] buf) throws IOException{
        this.write(buf, 0, buf.length);
    }
    
    public synchronized void write(int b) throws IOException{
        this.output.write(b);
    }
    
    public synchronized int read(byte[] buf, int index, int count) throws IOException{
        return this.input.read(buf, index, count);
    }
    
    public synchronized int read(byte[] buf) throws IOException{
        return this.input.read(buf, 0, buf.length);
    }
    
    public synchronized int read() throws IOException{
        return this.input.read();
    }
    /**
     * 立即刷新输出流。
     * @throws IOException 
     */
    public synchronized void flush() throws IOException{
        this.output.flush();
    }
    
    /**
     * 关闭内部关联的流。
     * @throws IOException 
     */
    public synchronized void close() throws IOException{
        this.input.close();
        this.output.close();
    }
    
    /**
     * 从输入流的当前位置跳过指定字节数。
     * @param n 要跳过的字节数量。
     * @return 实际路过的字节数。
     * @throws IOException 
     */
    public synchronized long skip(long n) throws IOException{
        return this.input.skip(n);
    }
    
    /**
     * 记录当前输入流的位置，以便可以使用 <code>reset</code> 方法回到该位置。
     * @param readlimit 在标记之后允许读取在最大字节数,超这个数值后标记奖失效。注意：该参数并不是总是有效，这取决于提供的 InputStream.
     * @throws IOException 
     */
    public synchronized void mark(int readlimit) throws IOException{
        this.input.mark(readlimit);
    }
    
    public boolean markSupported(){
        return this.input.markSupported();
    }
    
    public synchronized void reset() throws IOException{
        this.input.reset();
    }
}

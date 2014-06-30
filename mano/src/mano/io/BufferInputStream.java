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

/**
 * 表示一个缓冲区输入流。
 * @author jun <jun@diosay.com>
 */
public class BufferInputStream extends InputStream {

    public final Buffer buffer;
    protected int pos;
    protected int mark;
    public BufferInputStream(Buffer buf){
        if (buf == null) {
            throw new NullPointerException();
        }
        this.buffer=buf;
        this.pos=this.mark=buf.position();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.buffer.position(this.mark);
    }
    
    /**
     * 
     * @param readlimit 该参数暂未使用。
     */
    @Override
    public synchronized void mark(int readlimit) {
        this.mark=this.buffer.position(); //TODO: 未解决标记的失效。
    }

    @Override
    public void close() throws IOException {
        //nothing
    }

    @Override
    public int available() throws IOException {
        return this.buffer.length();
    }

    @Override
    public long skip(long n) throws IOException {
        if(n<0 || n+this.buffer.position()>this.buffer.length()){
            return -1;
        }
        this.buffer.position(this.buffer.position()+(int)n);
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.buffer.read(b, off, len);
    }
    
    @Override
    public int read() throws IOException {
        return this.buffer.read();
    }
}

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class BufferOutputStream extends OutputStream{

    public final Buffer buffer;
    public BufferOutputStream(Buffer buf){
        if (buf == null) {
            throw new NullPointerException();
        }
        this.buffer=buf;
    }
    
    @Override
    public void close() throws IOException {
        //nothing
    }

    @Override
    public void flush() throws IOException {
        this.buffer.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.buffer.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.buffer.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer.write(b);
    }
    
}

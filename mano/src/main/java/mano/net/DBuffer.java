/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.nio.ByteBuffer;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class DBuffer implements Buffer {
    protected ByteBuffer inner;
    protected DBuffer(ByteBuffer buffer){
        if(buffer==null){
            throw new java.lang.IllegalArgumentException("buffer");
        }
        this.inner=buffer;
    }
    
    public ByteBuffer inner() {
        return inner;
    }
    
    public static DBuffer warp(ByteBuffer buffer){
        return new DBuffer(buffer);
    }
}

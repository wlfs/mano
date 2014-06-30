/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import mano.util.Pool;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class BufferPool extends Pool<Buffer> {

    private int bucketSize = 16;//2,048(字符)*2（UTF-8）=4096 default for one GET_{URL}_HTTP/1.1/R/N=15 2000（有效字符） 1024*4/10
    private int bufferSize = 4096;//0.5k 64k/bucket

    public BufferPool() {
        this(4096, 16);
    }

    public BufferPool(int size, int bucket) {
        bufferSize = size;
        bucketSize = bucket;
    }

    @Override
    protected Buffer create() {
        byte[] bucket = new byte[bucketSize * bufferSize];

        Buffer result = new Buffer(bucket, 0, bufferSize);
        for (int i = 1; i < bucketSize; i++) {
            this.put(new Buffer(bucket, bufferSize * i, bufferSize));
        }
        return result;
    }

    @Override
    public void put(Buffer item) {
        if (item == null || item.capacity != bufferSize) {
            return;
        }
        item.reset();
        super.put(item);
    }

}

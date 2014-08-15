/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import mano.util.Pool;
import java.nio.ByteBuffer;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ByteBufferPool extends Pool<ByteBuffer> {

    private int bucketSize = 64;//32k default for one
    private int bufferSize = 512;//0.5k

    public ByteBufferPool() {
        this(512, 64);
    }

    public ByteBufferPool(int size, int bucket) {
        bufferSize = size;
        bucketSize = bucket;
    }

    @Override
    protected ByteBuffer create() {
        ByteBuffer slice;
        ByteBuffer base = ByteBuffer.allocateDirect(bucketSize * bufferSize);

        base.limit(bufferSize);
        ByteBuffer result = base.slice();
        for (int i = 1; i < bucketSize; i++) {
            base.position(bufferSize * i).limit(bufferSize * i + bufferSize);
            slice = base.slice();
            this.put(slice);
        }
        return result;
    }

    @Override
    public void put(ByteBuffer item) {
        if (item == null || item.capacity() != bufferSize) {
            return;
        }
        item.clear();
        super.put(item);
    }

}

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 表示一个用于交互操作的字节缓冲区。
 *
 * @author jun <jun@diosay.com>
 */
public class Buffer {

    public final byte[] array;
    public final int offset;
    public final int capacity;
    protected int pos;
    protected int len;

    public Buffer(byte[] arr, int index, int count) {
        if (arr == null) {
            throw new NullPointerException();
        }

        this.array = arr;
        this.offset = Math.max(index, 0);
        this.capacity = Math.min(this.offset + count, arr.length);
        this.pos = this.offset;
        this.len = this.capacity;
    }

    public synchronized int read(byte[] buf, int index, int count) {
        if (buf == null) {
            throw new NullPointerException();
        } else if (index < 0 || count < 0 || count > buf.length - index) {
            throw new IndexOutOfBoundsException();
        }

        if (this.pos >= this.len) {
            return -1;
        }

        int size = this.len - this.pos;
        if (size >= count) {
            size = count;
        }
        if (size <= 0) {
            return 0;
        }
        System.arraycopy(this.array, this.pos, buf, index, size);
        pos += size;

        return size;
    }

    public int read(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException();
        }
        return this.read(buf, 0, buf.length);
    }

    public synchronized int read() {
        return (this.pos <= this.len) ? (this.array[this.pos++] & 0xff) : -1;
    }

    public synchronized int write(byte[] buf, int index, int count) {
        if (buf == null) {
            throw new NullPointerException();
        } else if (index < 0 || count < 0 || count > buf.length - index) {
            throw new IndexOutOfBoundsException();
        }

        if (this.pos >= this.len) {
            return -1;
        }

        int size = this.len - this.pos;
        if (size >= count) {
            size = count;
        }
        if (size <= 0) {
            return 0;
        }
        System.arraycopy(buf, index, this.array, this.offset + this.pos, size);
        pos += size;

        return size;
    }

    public int write(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException();
        }
        return this.write(buf, 0, buf.length);
    }

    public synchronized int write(int b) {
        if (this.len - this.pos <= 0) {
            return -1;
        }
        this.array[this.pos++] = (byte) b;
        return 1;
    }

    /**
     * 刷新缓冲区以确认有效数据长度，以便之后的读操作。 该操作会将当前 pos 设置为当前 len，并将 pos 设置为 0。
     *
     * @return
     */
    public synchronized Buffer flush() {
        this.len = this.pos;
        this.pos = 0;
        return this;
    }

    /**
     * 整理缓冲区，以便之后的写操作。 该操作会将 len 到 pos 之间的数据移动数组开始位置，并将 len 设置为数组容量，pos
     * 设置为移动的有效长度。
     *
     * @return
     */
    public synchronized Buffer compact() {
        int size = this.len - this.pos;

        if (size > 0) {
            System.arraycopy(this.array, this.offset + this.pos, this.array, this.offset, size);
            this.pos = size;
            this.len = this.capacity;
        }
        return this;
    }

    /**
     * 将缓冲区重置为初始状态，但不会改变已存在的数据。
     *
     * @return
     */
    public synchronized Buffer reset() {
        this.pos = 0;
        this.len = this.capacity;
        return this;
    }

    /**
     * 获取当前缓冲区的有效数据长度。
     * <p>
     * 注意：如果未进行 <code>flush</code> 或 <code>compact</code> 操作，返回值可能不是真实的数据长度。</p>
     *
     * @return 有效数据长度。
     */
    public int length() {
        return this.len - this.pos;
    }

    public int position() {
        return this.pos;
    }

    /**
     * 定位缓冲区的位置。
     *
     * @param n 新位置的值，如果该值大于当前长度，则会将长度设置该值。
     * @return 当前位置，失败返回 -1。
     */
    public synchronized int position(int n) {
        if (n < 0 || n > this.capacity) {
            return -1;
        } else if (n >= this.len) {
            this.len = n;
            this.pos = n;
        } else {
            this.pos = n;
        }
        return n;
    }

    /**
     * 获取当前相对于数组的真实偏移位置。
     *
     * @return
     */
    public int arrayOffset() {
        return this.offset + this.pos;
    }

    @Override
    public String toString() {
        return this.getClass() + "[pos=" + this.pos + " len=" + this.len + " off=" + this.offset + " cap=" + this.capacity + " cur=" + 0 + "]";
    }

    private static byte[] CRLF = new byte[]{'\r', '\n'};

    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle) {
        return bytesIndexOf(haystack, index, count, needle, 0, -1);
    }

    public static int bytesIndexOf(byte[] haystack, int index, int count, byte[] needle, int needleIndex, int needleCount) {
        if (index + count > haystack.length) {
            return -1;
        }

        if (needleCount == -1) {
            needleCount = needle.length;
        }

        if (count < needleCount || needleIndex + needleCount > needle.length) {
            return -1;
        }

        for (int i = index; i < count + index; i++) {
            if (haystack[i] == needle[needleIndex]) //找到第一匹配的位置
            {
                for (int j = 0; j < needleCount - 1; j++) //连续匹配
                                                                                                                                                                                                                                                                {
                    if (haystack[i + j] != needle[j + needleIndex]) //如果中途不匹配
                    {
                        return bytesIndexOf(haystack, i + 1, count - (i - index) - 1, needle,needleIndex,needleCount);//从不匹配位置回溯
                    }
                }
                return i;
            }
        }

        return -1;
    }

    public synchronized String readln() throws UnsupportedEncodingException {
        int off = this.offset + this.pos;
        if(this.len<CRLF.length){
            return null;
        }
        int index = bytesIndexOf(this.array, off, this.len, CRLF);
        if (index < 0) {
            return null;
        }
        int count = index - off;
        String result = "";
        if (count > 0) {
            result = new String(this.array, off, count, "UTF-8");//存在3次复制
        }
        this.pos += count + CRLF.length;
        return result;
    }

    public int write(ByteBuffer buf){
        int size=this.len-this.pos;
        if(size>=buf.remaining()){
            size=buf.remaining();
        }
        if(size<=0){
            return 0;
        }
        buf.get(this.array, this.offset+this.pos, size);
        this.pos+=size;
        return size;
    }
    
    public int write(Stream stream) throws IOException{
        int size=this.len-this.pos;
        if(size<=0){
            return 0;
        }
        int result=0;
        int count;
        while((count=stream.read(this.array, this.offset+this.pos, size))>0){
            size-=count;
            result+=count;
            this.pos+=count;
        }
        return result;
    }
    
    public boolean hasRemaining(){
        return this.len-this.pos>0;
    }
    
}

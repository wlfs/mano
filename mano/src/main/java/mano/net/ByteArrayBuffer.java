/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import mano.Resettable;
import mano.io.Stream;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ByteArrayBuffer implements Buffer, Resettable {

    protected ByteBuffer inner;
    public byte[] array;

    public ByteArrayBuffer(byte[] array, int index, int length) {
        inner = ByteBuffer.wrap(array, index, length);
        array = inner.array();
    }

    public ByteArrayBuffer(byte[] array) {
        this(array, 0, array.length);
    }

    public ByteArrayBuffer(int capacity) {
        inner = ByteBuffer.allocate(capacity);
        array = inner.array();
    }

    public ByteBuffer inner() {
        return inner;
    }

    public synchronized void read(byte[] buffer, int offset, int length) {
        /*if (buffer == null) {
         throw new NullPointerException();
         } else if (offset < 0 || length < 0 || length > buffer.length - offset) {
         throw new IndexOutOfBoundsException();
         }*/

        inner.get(buffer, offset, length);

        /*inner.remaining();
         inner.position();
        
         if (this.pos >= this.len) {
         return -1;
         }

         int size = this.len - this.pos;
         if (size >= length) {
         size = length;
         }
         if (size <= 0) {
         return 0;
         }
         System.arraycopy(this.array, this.pos, buffer, offset, size);
         pos += size;

         return size;*/
    }

    public void read(byte[] buf) {
        this.read(buf, 0, buf.length);
        /*if (buf == null) {
         throw new NullPointerException();
         }
         return this.read(buf, 0, buf.length);*/
    }

    /**
     *
     * @return 读取并返回一下个字节
     */
    public synchronized int read() {
        return inner.get() & 0xff;
        //return (this.pos <= this.len) ? (this.array[this.pos++] & 0xff) : -1;
    }

    public synchronized void write(byte[] buf, int index, int count) {
        inner.put(buf, count, count);
        /*if (buf == null) {
         throw new NullPointerException();
         } else if (index < 0 || count < 0 || count > buf.length - index) {
         throw new IndexOutOfBoundsException();
         }
         inner.put(buf, count, count);
         return 0;
         if (this.pos >= this.capacity) {//cap->len
         return -1;
         }*/
        /*int size = this.capacity - this.pos;//cap->len
         if (size >= count) {
         size = count;
         }
         if (size <= 0) {
         return 0;
         }
         System.arraycopy(buf, index, this.array, this.offset + this.pos, size);
         pos += size;

         return size;*/
    }

    public void write(byte[] buf) {
        this.write(buf, 0, buf.length);
        /*if (buf == null) {
         throw new NullPointerException();
         }
         return this.write(buf, 0, buf.length);*/
    }

    public synchronized void write(int b) {
        inner.put((byte) b);

        /*if (this.capacity - this.pos <= 0) {//cap->len,TODO:准确度
         return -1;
         }
         this.array[this.pos++] = (byte) b;
         return 1;*/
    }

    /**
     * 刷新缓冲区以确认有效数据长度，以便之后的读操作。 该操作会将当前 pos 设置为当前 len，并将 pos 设置为 0。
     *
     * @return
     */
    public synchronized void flush() {
        inner.flip();
        /*this.len = this.pos;
         this.pos = 0;
         return this;*/
    }

    /**
     * 整理缓冲区，以便之后的写操作。 该操作会将 len 到 pos 之间的数据移动数组开始位置，并将 len 设置为数组容量，pos
     * 设置为移动的有效长度。
     *
     * @return
     */
    public synchronized void compact() {
        inner.compact();
        /*int size = this.len - this.pos;
        
         if (size > 0) {
         System.arraycopy(this.array, this.offset + this.pos, this.array, this.offset, size);
         this.pos = size;
         this.len = this.capacity;
         }
         return this;*/
    }

    /**
     * 将缓冲区重置为初始状态，但不会改变已存在的数据。
     *
     * @return
     */
    @Override
    public synchronized void reset() {
        inner.clear();
        //this.pos = 0;
        //this.len = this.capacity;
    }

    /**
     * 获取当前缓冲区的有效数据长度。
     * <p>
     * 注意：如果未进行 <code>flush</code> 或 <code>compact</code> 操作，返回值可能不是真实的数据长度。</p>
     *
     * @return 有效数据长度。
     */
    public int length() {
        return inner.limit() - inner.position();
        //return this.len - this.pos;
    }

    public int position() {
        return inner.position();
        //return this.pos;
    }

    /**
     * 定位缓冲区的位置。
     *
     * @param n 新位置的值，如果该值大于当前长度，则会将长度设置该值。
     * @return 当前位置，失败返回 -1。
     */
    public synchronized int position(int n) {
        inner.position(n);
        return n;
        /*if (n < 0 || n > this.capacity) {
         return -1;
         } else if (n >= this.len) {
         this.len = n;
         this.pos = n;
         } else {
         this.pos = n;
         }
         return n;*/
    }

    /**
     * 获取当前相对于数组的真实偏移位置。
     *
     * @return
     */
    public int arrayOffset() {
        return inner.arrayOffset();
        //return this.offset + this.pos;
    }

    @Override
    public String toString() {
        return inner.toString();
        //return this.getClass() + "[pos=" + this.pos + " len=" + this.len + " off=" + this.offset + " cap=" + this.capacity + " cur=" + 0 + "]";
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
                    if (i + j >= count + index || haystack[i + j] != needle[j + needleIndex]) //如果中途不匹配
                    {
                        return bytesIndexOf(haystack, i + 1, count - (i - index) - 1, needle, needleIndex, needleCount);//从不匹配位置回溯
                    }
                }
                return i;
            }
        }

        return -1;
    }

    public synchronized String readln(String charset) throws UnsupportedEncodingException {
        return readln(Charset.forName(charset));
    }

    public synchronized String readln(Charset charset) {
        int off = inner.arrayOffset() + inner.position();
        if (inner.limit() < CRLF.length) {
            return null;
        }
        int index = bytesIndexOf(inner.array(), off, this.length(), CRLF);
        if (index < 0) {
            return null;
        }
        //int count = index - off;
        String result = readstr(off, index - off, charset);
        //if (count > 0) {
        //    result = new String(this.array, off, count, charset);//存在3次复制
        //    //Logger.info(result);
        //}
        //this.pos += CRLF.length;
        inner.position(inner.position() + CRLF.length);
        return result;
    }

    public synchronized String readln() throws UnsupportedEncodingException {
        return readln("UTF-8");
    }

    public synchronized String readstr(int off, int count, String charset) throws UnsupportedEncodingException {
        return this.readstr(off, count, Charset.forName(charset));
    }

    public synchronized String readstr(int off, int count, Charset charset) {
        String result = new String(inner.array(), off, count, charset);
        //this.pos += count;
        inner.position(inner.position() + count);
        return result;
    }

    public synchronized String readstr(int off, int count) throws UnsupportedEncodingException {
        return readstr(off, count, "UTF-8");
    }

    public synchronized String readstr(String charset) throws UnsupportedEncodingException {
        return readstr(inner.arrayOffset() + inner.position(), this.length(), charset);
    }

    public synchronized String readstr() throws UnsupportedEncodingException {
        return readstr("UTF-8");
    }

    public void write(ByteBuffer buf) {
        inner.put(buf);
        /*
         int size = this.len - this.pos;
         if (size >= buf.remaining()) {
         size = buf.remaining();
         }
         if (size <= 0) {
         return 0;
         }
         buf.get(this.array, this.offset + this.pos, size);
         this.pos += size;
         return size;*/
    }

    public int write(Stream stream) throws IOException {
        int size = inner.limit() - inner.position();
        if (size <= 0) {
            return 0;
        }
        int result = 0;
        int count;
        while ((count = stream.read(inner.array(), inner.arrayOffset() + inner.position(), size)) > 0) {
            size -= count;
            result += count;
            inner.position(inner.position() + count);
        }
        return result;
    }

    public boolean hasRemaining() {
        return inner.hasRemaining();
        //return this.len - this.pos > 0;
    }

}

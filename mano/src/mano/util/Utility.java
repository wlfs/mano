/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Utility {

    //http://blog.sina.com.cn/s/blog_7a35101201012n0b.html
    //http://blog.csdn.net/snakeqi/article/details/344069
    public static String[] split(String s, String spliter, boolean removeEmptyItem) {
        String[] arr = (s == null) ? new String[0] : s.split(spliter);
        if (removeEmptyItem) {
            ArrayList<String> temp = new ArrayList<>();
            for (int i = 0; i < arr.length; i++) {
                if (!"".equals(arr[i].trim())) {
                    temp.add(arr[i].trim());
                }
            }
            arr = temp.toArray(new String[0]);
        }
        return arr;
    }

    public static String[] split(String s, String spliter) {
        return split(s, spliter, false);
    }

    public static byte[] toBytes(short s) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (s & 0xff);
        bytes[1] = (byte) ((s >> 8) & 0xff);
        return bytes;
    }

    public static short toShort(byte[] bytes, int index) {
        
        return ByteBuffer.wrap(bytes, index, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        //return (short) ((0xff & bytes[index]) | (0xff & (bytes[index + 1] << 8)));
    }

    public static byte[] toBytes(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (i & 0xff);
        bytes[1] = (byte) ((i & 0xff00) >> 8);
        bytes[2] = (byte) ((i & 0xff0000) >> 16);
        bytes[3] = (byte) ((i & 0xff000000) >> 24);
        return bytes;
    }

    public static int toInt(byte[] bytes, int index) {
        return (0xff & bytes[index])
                | (0xff00 & (bytes[index + 1] << 8))
                | (0xff0000 & (bytes[index + 2] << 16))
                | (0xff000000 & (bytes[index + 3] << 24));
    }

    public static byte[] toBytes(long l) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (l & 0xff);
        bytes[1] = (byte) ((l >> 8) & 0xff);
        bytes[2] = (byte) ((l >> 16) & 0xff);
        bytes[3] = (byte) ((l >> 24) & 0xff);
        bytes[4] = (byte) ((l >> 32) & 0xff);
        bytes[5] = (byte) ((l >> 40) & 0xff);
        bytes[6] = (byte) ((l >> 48) & 0xff);
        bytes[7] = (byte) ((l >> 56) & 0xff);
        return bytes;
    }

    public static long toLong(byte[] bytes, int index) {
        return (0xffL & (long) bytes[index])
                | (0xff00L & ((long) bytes[index + 1] << 8))
                | (0xff0000L & ((long) bytes[index + 2] << 16))
                | (0xff000000L & ((long) bytes[index + 3] << 24))
                | (0xff00000000L & ((long) bytes[index + 4] << 32))
                | (0xff0000000000L & ((long) bytes[index + 5] << 40))
                | (0xff000000000000L & ((long) bytes[index + 6] << 48))
                | (0xff00000000000000L & ((long) bytes[index + 7] << 56));
    }

    public static byte[] toBytes(double d) {
        return toBytes(Double.doubleToLongBits(d));
    }

    public static double toDouble(byte[] bytes, int index) {
        return Double.longBitsToDouble(toLong(bytes, index));
    }
    
    public static byte[] toBytes(float f) {
        return toBytes(Float.floatToIntBits(f));
    }

    public static float toFloat(byte[] bytes, int index) {
        return Float.intBitsToFloat(toInt(bytes, index));
    }
}

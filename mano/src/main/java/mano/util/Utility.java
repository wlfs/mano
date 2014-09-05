/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mano.Mano;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Utility {

    static final Pattern pattern = Pattern.compile("\\{\\s*([\\w\\-_\\.]+)\\s*\\}");

    public static void prepareProperties(final Properties props) {
        if (props == null) {
            throw new IllegalArgumentException("props");
        }
        props.entrySet().stream().forEach(item -> {
            if (item.getValue() == null) {
                return;
            }
            String key = item.getKey().toString();
            props.put(item.getKey(), getAndReplaceMarkup(key, props));
        });
    }

    public static String getAndReplaceMarkup(String key, Map... maps) {
        if (maps == null || maps.length == 0) {
            throw new IllegalArgumentException("maps");
        }
        Object value = null;
        for (Map map : maps) {
            if (map.containsKey(key)) {
                value = map.get(key);
                break;
            }
        }

        if (value == null) {
            throw new IllegalArgumentException("undefined：" + key);
        }
        StringBuilder sb = new StringBuilder(value.toString());

        Matcher matcher = pattern.matcher(sb);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (key.equals(name)) {
                throw new IllegalArgumentException("not call salf：" + key);
            }
            sb.replace(matcher.start(), matcher.end(), getAndReplaceMarkup(name, maps));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Mano.getProperties().setProperty("server.dir", "{user.dir}/..");
        Mano.getProperties().setProperty("server.config", "{server.dir}/conf/server.xml");
        Utility.prepareProperties(Mano.getProperties());

        int x = 0;
    }

    public static Path combinePath(String first, String... more) {
        if (more.length != 0) {
            ArrayList<String> tmp = new ArrayList<>(more.length);
            for (String s : more) {
                if (s == null || "".equals(s.trim()) || "\\".equals(s.trim()) || "/".equals(s.trim())) {
                    continue;
                }
                tmp.add(s.trim());
            }
            if (!tmp.isEmpty()) {
                if (first == null || "".equals(first.trim()) || "\\".equals(first.trim()) || "/".equals(first.trim())) {
                    first = tmp.get(0);
                    tmp.remove(0);
                }
            }

            more = tmp.toArray(new String[0]);
        }

        return Paths.get(first, more);
    }

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

    public static final int OBJECT = 0,
            NUM_SHORT = 1,
            NUM_INTEGER = 2,
            NUM_LONG = 3,
            NUM_FLOAT = 4,
            NUM_DOUBLE = 5,
            BOOLEAN = 6,
            DATETIME = 7;

    public static int geTypeCode(Class<?> clazz) {
        switch (clazz.getName()) {
            case "long":
            case "java.lang.Long":
                return Utility.NUM_LONG;
            case "int":
            case "java.lang.Integer":
                return Utility.NUM_INTEGER;
            case "double":
            case "java.lang.Double":
                return Utility.NUM_DOUBLE;
            case "float":
            case "java.lang.Float":
                return Utility.NUM_INTEGER;
            case "short":
            case "java.lang.Short":
                return Utility.NUM_SHORT;
            case "boolean":
            case "java.lang.Boolean":
                return Utility.BOOLEAN;
            case "mano.DateTime":
                return Utility.DATETIME;
            default:
                return 0;
        }
    }

    public static <T> T cast(Class<T> clazz, Object obj) {
        Object result;
        int code = geTypeCode(clazz);
        switch (code) {
            case Utility.NUM_DOUBLE:
            case Utility.NUM_FLOAT:
            case Utility.NUM_INTEGER:
            case Utility.NUM_LONG:
            case Utility.NUM_SHORT:
                result = asNumber(code, toDouble(obj));
                break;
            case Utility.BOOLEAN:
                result = Boolean.parseBoolean(obj.toString());
                break;
            default:
                return clazz.cast(obj);
        }
        return (T) result;
    }

    public static double toDouble(Object obj) {
        return Double.parseDouble(obj.toString());
    }

    public static Object asNumber(int type, double obj) {
        Object result;

        switch (type) {
            case Utility.NUM_DOUBLE:
                return obj;
            case Utility.NUM_FLOAT:
                return (float) obj;
            case Utility.NUM_INTEGER:
                return (int) obj;
            case Utility.NUM_LONG:
                return (long) obj;
            case Utility.NUM_SHORT:
                return (short) obj;
            default:
                return obj;
        }
    }
    
    
    public static void copyFile(String src, String target) throws IOException {
            copyFile(new File(src), new File(target));
        }

        public static void copyFile(File src, File target) throws IOException {
            if (!src.exists() || !src.isFile()) {
                throw new FileNotFoundException("源文件不存或不是文件：" + src);
            }

            if (target.exists() && target.isFile()) {
                target.delete();
            }

            File parent = target.getParentFile();
            if (!parent.exists() || (parent.exists() && !parent.isDirectory())) {
                parent.mkdirs();
            }

            target.createNewFile();

            try (FileInputStream input = new FileInputStream(src)) {
                try (FileOutputStream out = new FileOutputStream(target)) {
                    out.getChannel().transferFrom(input.getChannel(), 0, input.getChannel().size());
                }
            }
        }

        public static void copyFolder(String src, String target) throws IOException {
            copyFolder(new File(src), new File(target));
        }

        public static void copyFolder(File src, File target) throws IOException {
            if (!src.exists() || !src.isDirectory()) {
                throw new FileNotFoundException("源目录不存在或不是目录：" + src);
            }
            if (!target.exists() || !target.isDirectory()) {
                if (!target.mkdirs()) {
                    throw new IOException("创建目标目录失败：" + target);
                }
            }
            for (File child : src.listFiles()) {
                if (child.isDirectory()) {
                    copyFolder(src.toString() + "/" + child.getName(), target.toString() + "/" + child.getName());
                } else if (child.isFile()) {
                    copyFile(src.toString() + "/" + child.getName(), target.toString() + "/" + child.getName());
                }
            }
        }

        public static void deleteFile(String filename) {
            new File(filename).delete();
        }

        public static void deleteFolder(String filename) {
            deleteFolder(new File(filename));
        }

        public static void deleteFolder(File file) {
            if (file.exists() && file.isDirectory()) {
                for (File child : file.listFiles()) {
                    if (child.isFile()) {
                        child.delete();
                    } else {
                        deleteFolder(child);
                    }
                }
                file.delete();
            }

        }

}

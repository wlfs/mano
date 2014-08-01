/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import mano.util.Utility;

/**
 * 表示由 4 个 数值（0-32767） 值组成的版本号。 此类不能被继承。
 * @author jun <jun@diosay.com>
 */
public final class Version {

    /**
     * 获取当前 Version 对象版本号的主要版本号部分的值。
     */
    public final int major;
    /**
     * 获取当前 Version 对象版本号的次要版本号部分的值。
     */
    public final int minor;
    /**
     * 获取当前 Version 对象版本号的内部版本号部分的值。
     */
    public final int build;
    /**
     * 获取当前 Version 对象版本号的修订号部分的值。
     */
    public final int revision;
    /**
     * 获取当前 Version 对象的绝对值。
     */
    public final long value;

    public Version(int major, int minor, int build, int revision) {
        if (major < 0 || major > Short.MAX_VALUE) {
            throw new IllegalArgumentException("major");
        }
        if (minor < 0 || minor > Short.MAX_VALUE) {
            throw new IllegalArgumentException("minor");
        }
        if (build < 0 || build > Short.MAX_VALUE) {
            throw new IllegalArgumentException("build");
        }
        if (revision < 0 || revision > Short.MAX_VALUE) {
            throw new IllegalArgumentException("revision");
        }

        this.major = major;
        this.minor = minor;
        this.build = build;
        this.revision = revision;

        this.value = Utility.toLong(toBytes(), 0);
    }
    
    public Version(int major, int minor, int build) {
        this(major,minor,build,0);
    }
    
    public Version(int major, int minor) {
        this(major,minor,0,0);
    }
    
    public Version(int major) {
        this(major,0,0,0);
    }
    
    public Version() {
        this(0,0,0,0);
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[8];
        byte[] tmp = Utility.toBytes((short) major);
        int i = 0;
        bytes[i++] = tmp[0];
        bytes[i++] = tmp[1];

        tmp = Utility.toBytes((short) minor);
        bytes[i++] = tmp[0];
        bytes[i++] = tmp[1];

        tmp = Utility.toBytes((short) build);
        bytes[i++] = tmp[0];
        bytes[i++] = tmp[1];

        tmp = Utility.toBytes((short) revision);
        bytes[i++] = tmp[0];
        bytes[i++] = tmp[1];
        return bytes;
    }

    public String toString(String spliter) {
        return major + spliter + minor + spliter + build + spliter + revision;
    }
    
    @Override
    public String toString() {
        return toString(".");
    }

    public static Version parse(byte[] bytes, int index) {

        Version ver = new Version(Utility.toShort(bytes, index),
                Utility.toShort(bytes, index + 2),
                Utility.toShort(bytes, index + 2),
                Utility.toShort(bytes, index + 2));
        return ver;
    }

    public static Version parse(String version) {
        return null;
    }

}

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.util.logging;

import java.io.Serializable;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class Level implements Serializable {

        /**
         * 关闭所有日志。
         */
        public static Level OFF = new Level("OFF", -1);
        /**
         * 表示所有级别的日志均会输出。
         */
        public static Level ALL = new Level("ALL", 0);
        /**
         * 表示一个提示信息。
         */
        public static Level INFO = new Level("INFO", 100);
        /**
         * 表示一个调试信息。
         */
        public static Level DEBUG = new Level("DEBUG", 200);
        /**
         * 表示一个跟踪信息。
         */
        public static Level TRACE = new Level("TRACE", 300);
        /**
         * 表示一个警告信息。
         */
        public static Level WARNING = new Level("WARNING", 400);
        /**
         * 表示一错误信息。
         */
        public static Level ERROR = new Level("ERROR", 500);
        /**
         * 致命错误。
         */
        public static Level FATAL = new Level("FATAL", 600);

        public final int value;
        public final String name;

        public Level(String levelName, int levelValue) {
            name = levelName;
            value = levelValue;
            java.util.logging.SocketHandler h;
        }

        @Override
        public String toString() {
            return this.name + "[" + this.value + "]";
        }

    }

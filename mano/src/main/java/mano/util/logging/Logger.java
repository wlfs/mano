/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.io.Serializable;
import mano.util.ProviderMapper;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class Logger {

    public static final int /**
             * 记录的最低级别，表示所有日志记录将会输出。
             */
            ALL = 0,
            /**
             * 表明消息在粗粒度级别上突出强调应用程序的运行过程。
             */
            INFO = 100,
            /**
             * 指出细粒度信息事件对调试应用程序是非常有帮助的。
             */
            DEBUG = 200,
            /**
             * 表示只是用于跟踪的信息，如效率的统计分析。
             */
            TRACE = 300,
            /**
             * 表明会出现潜在错误的情形。
             */
            WARNING = 400,
            /**
             * 指出虽然发生错误事件，但仍然不影响系统的继续运行。
             */
            ERROR = 500,
            /**
             * 指出每个严重的错误事件将会导致应用程序的退出。
             */
            FATAL = 600,
            /**
             * 记录的最高级别，表示不输出任何日志记录。
             */
            OFF = 1000;

    private LogProvider provider;

    public static final class Level implements Serializable {

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
        }

        @Override
        public String toString() {
            return this.name + "[" + this.value + "]";
        }

    }

    private static Logger defLogger;

    public static Logger getDefault() {
        if (defLogger == null) {
            defLogger = new Logger(new CansoleLogProvider());
        }
        return defLogger;
    }

    public Logger(LogProvider provider) {
        this.provider = provider;
    }

    public boolean isInfoEnabled() {
        return provider.isEnabled(Logger.INFO);
    }

    public void info(Object message) {
        if (isInfoEnabled()) {
            provider.write(Logger.INFO, message);
        }
    }

    public void info(String message, Throwable t) {
        if (isInfoEnabled()) {
            provider.write(Logger.INFO, message, t);
        }
    }

    public void info(String format, Object... args) {
        if (isInfoEnabled()) {
            provider.write(Logger.INFO, format, args);
        }
    }

    public boolean isDebugEnabled() {
        return provider.isEnabled(Logger.DEBUG);
    }

    public void debug(Object message) {
        if (isDebugEnabled()) {
            provider.write(Logger.DEBUG, message);
        }
    }

    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(Logger.DEBUG, message, t);
        }
    }

    public void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(Logger.DEBUG, format, args);
        }
    }

    public boolean isTraceEnabled() {
        return provider.isEnabled(Logger.TRACE);
    }

    public void trace(Object message) {
        if (isDebugEnabled()) {
            provider.write(Logger.TRACE, message);
        }
    }

    public void trace(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(Logger.TRACE, message, t);
        }

    }

    public void trace(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(Logger.TRACE, format, args);
        }
    }

    public boolean isErrorEnabled() {
        return provider.isEnabled(Logger.ERROR);
    }

    public void error(Object message) {
        if (isDebugEnabled()) {
            provider.write(Logger.ERROR, message);
        }
    }

    public void error(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(Logger.ERROR, message, t);
        }
    }

    public void error(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(Logger.ERROR, format, args);
        }
    }

    public boolean isWarnEnabled() {
        return provider.isEnabled(Logger.WARNING);
    }

    public void warn(Object message) {
        if (isDebugEnabled()) {
            provider.write(Logger.WARNING, message);
        }
    }

    public void warn(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(Logger.WARNING, message, t);
        }
    }

    public void warn(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(Logger.WARNING, format, args);
        }
    }

    public boolean isFatalEnabled() {
        return provider.isEnabled(Logger.FATAL);
    }

    public void fatal(Object message) {
        if (isDebugEnabled()) {
            provider.write(Logger.FATAL, message);
        }
    }

    public void fatal(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(Logger.FATAL, message, t);
        }
    }

    public void fatal(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(Logger.FATAL, format, args);
        }
    }
}

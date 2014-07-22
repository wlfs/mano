/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

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

    private static ILogger logger;

    public static ILogger getLogger(String providerName) throws InstantiationException {
        return (ILogger) ProviderMapper.newInstance(providerName, Logger.class);
    }

    private static ILogger getLogger() {
        if (logger == null) {
            try {
                logger = getLogger(Logger.class.getName());
            } catch (InstantiationException igored) {
                logger=new CansoleLogger();
            }
        }
        return logger;
    }

    public static boolean isInfoEnabled() {
        return getLogger().isEnabled(Logger.INFO);
    }

    public static void info(Object message) {
        if (isInfoEnabled()) {
            getLogger().log(Logger.INFO, message);
        }
    }

    public static void info(String message, Throwable t) {
        if (isInfoEnabled()) {
            getLogger().log(Logger.INFO, message, t);
        }
    }

    public static void info(String format, Object... args) {
        if (isInfoEnabled()) {
            getLogger().log(Logger.INFO, format, args);
        }
    }

    public static boolean isDebugEnabled() {
        return getLogger().isEnabled(Logger.DEBUG);
    }

    public static void debug(Object message) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.DEBUG, message);
        }
    }

    public static void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.DEBUG, message, t);
        }
    }

    public static void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.DEBUG, format, args);
        }
    }

    public static boolean isTraceEnabled() {
        return getLogger().isEnabled(Logger.TRACE);
    }

    public static void trace(Object message) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.TRACE, message);
        }
    }

    public static void trace(String message, Throwable t) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.TRACE, message, t);
        }

    }

    public static void trace(String format, Object... args) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.TRACE, format, args);
        }
    }

    public static boolean isErrorEnabled() {
        return getLogger().isEnabled(Logger.ERROR);
    }

    public static void error(Object message) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.ERROR, message);
        }
    }

    public static void error(String message, Throwable t) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.ERROR, message, t);
        }
    }

    public static void error(String format, Object... args) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.ERROR, format, args);
        }
    }

    public static boolean isWarnEnabled() {
        return getLogger().isEnabled(Logger.WARNING);
    }

    public static void warn(Object message) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.WARNING, message);
        }
    }

    public static void warn(String message, Throwable t) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.WARNING, message, t);
        }
    }

    public static void warn(String format, Object... args) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.WARNING, format, args);
        }
    }

    public static boolean isFatalEnabled() {
        return getLogger().isEnabled(Logger.FATAL);
    }

    public static void fatal(Object message) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.FATAL, message);
        }
    }

    public static void fatal(String message, Throwable t) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.FATAL, message, t);
        }
    }

    public static void fatal(String format, Object... args) {
        if (isDebugEnabled()) {
            getLogger().log(Logger.FATAL, format, args);
        }
    }
}

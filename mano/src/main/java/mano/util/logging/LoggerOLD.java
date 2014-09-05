/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;


/**
 *
 * @author jun <jun@diosay.com>
 */
@Deprecated
public final class LoggerOLD {

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

    

    private static LoggerOLD defLogger;

    public static LoggerOLD getDefault() {
        if (defLogger == null) {
            defLogger = new LoggerOLD(new CansoleLogProvider());
        }
        return defLogger;
    }

    public LoggerOLD(LogProvider provider) {
        this.provider = provider;
    }

    public boolean isInfoEnabled() {
        return provider.isEnabled(LoggerOLD.INFO);
    }

    public void info(Object message) {
        if (isInfoEnabled()) {
            provider.write(LoggerOLD.INFO, message);
        }
    }

    public void info(String message, Throwable t) {
        if (isInfoEnabled()) {
            provider.write(LoggerOLD.INFO, message, t);
        }
    }

    public void info(String format, Object... args) {
        if (isInfoEnabled()) {
            provider.write(LoggerOLD.INFO, format, args);
        }
    }

    public boolean isDebugEnabled() {
        return provider.isEnabled(LoggerOLD.DEBUG);
    }

    public void debug(Object message) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.DEBUG, message);
        }
    }

    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.DEBUG, message, t);
        }
    }

    public void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.DEBUG, format, args);
        }
    }

    public boolean isTraceEnabled() {
        return provider.isEnabled(LoggerOLD.TRACE);
    }

    public void trace(Object message) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.TRACE, message);
        }
    }

    public void trace(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.TRACE, message, t);
        }

    }

    public void trace(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.TRACE, format, args);
        }
    }

    public boolean isErrorEnabled() {
        return provider.isEnabled(LoggerOLD.ERROR);
    }

    public void error(Object message) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.ERROR, message);
        }
    }

    public void error(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.ERROR, message, t);
        }
    }

    public void error(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.ERROR, format, args);
        }
    }

    public boolean isWarnEnabled() {
        return provider.isEnabled(LoggerOLD.WARNING);
    }

    public void warn(Object message) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.WARNING, message);
        }
    }

    public void warn(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.WARNING, message, t);
        }
    }

    public void warn(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.WARNING, format, args);
        }
    }

    public boolean isFatalEnabled() {
        return provider.isEnabled(LoggerOLD.FATAL);
    }

    public void fatal(Object message) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.FATAL, message);
        }
    }

    public void fatal(String message, Throwable t) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.FATAL, message, t);
        }
    }

    public void fatal(String format, Object... args) {
        if (isDebugEnabled()) {
            provider.write(LoggerOLD.FATAL, format, args);
        }
    }
}

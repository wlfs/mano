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
public interface ILogger {
    
    boolean isEnabled(int level);
    void log(int level,Object obj);
    void log(int level,String format, Object... args);
    void log(int level,String message,Throwable t);
    
    /*public boolean isInfoEnabled();

    public void info(Object message);

    public void info(Object message, Throwable t);

    public void infoFormat(String format, Object... args);

    public boolean isDebugEnabled();

    public void debug(Object message);

    public void debug(Object message, Throwable t);

    public void debugFormat(String format, Object... args);

    public boolean isTraceEnabled();

    public void trace(Object message);

    public void trace(Object message, Throwable t);

    public void traceFormat(String format, Object... args);

    public boolean isErrorEnabled();

    public void error(Object message);

    public void error(Object message, Throwable t);

    public void errorFormat(String format, Object... args);

    public boolean isWarnEnabled();

    public void warn(Object message);

    public void warn(Object message, Throwable t);

    public void warnFormat(String format, Object... args);*/
}

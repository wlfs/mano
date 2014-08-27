/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.io.Closeable;
import java.io.Flushable;

/**
 * 日志处理程序抽象类。
 * @author jun <jun@diosay.com>
 */
public abstract class Handler implements Closeable, Flushable {

    private volatile Level level = Level.ALL;
    private volatile LogFormatter formatter = new SimpleFormatter();

    public final boolean log(LogEntry entry) throws Exception {
        if (isLoggable(entry)) {
            logBefor(entry);
            boolean result= doLog(entry);
            logAfter(entry);
            return result;
        }
        return false;
    }

    protected void logBefor(LogEntry entry){
        
    }
    protected void logAfter(LogEntry entry){
        
    }
    
    protected boolean isLoggable(LogEntry entry) {

        return true;
    }

    protected abstract boolean doLog(LogEntry entry) throws Exception;

    /**
     * Get the logging message level, for example Level.SEVERE.
     *
     * @return the logging message level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Set the logging message level, for example Level.SEVERE.
     *
     * @param level the logging message level
     */
    public void setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        this.level = level;
    }

    public synchronized void setFormatter(LogFormatter newFormatter) {
        formatter = newFormatter;
    }

    /**
     * Return the <tt>Formatter</tt> for this <tt>Handler</tt>.
     *
     * @return the <tt>Formatter</tt> (may be null).
     */
    public LogFormatter getFormatter() {
        return formatter;
    }

}

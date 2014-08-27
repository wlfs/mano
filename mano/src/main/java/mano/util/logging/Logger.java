/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.util.concurrent.ExecutionException;
import mano.Action;
import mano.DateTime;
import mano.service.Intent;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Logger {

    private Action<Intent> action;
    
    public Logger(){
        this.action=(intent)->{
            if (intent.isFaulted()) {
                intent.getException().printStackTrace(System.out);
            }
        };
    }
    
    public static Logger getLog(){
        return new Logger();
    }
    
    public String getName() {
        return "root";
    }
    
    protected LogEntry create(Thread thread, StackTraceElement[] traces) {
        LogEntry entry = new LogEntry();
        entry.setTime(DateTime.now());
        entry.setLoggerName(this.getName());
        entry.setThreadId(thread.getId());
        if (traces != null && traces.length > 2) {
            entry.setSourceClassName(traces[2].getClassName());
            entry.setSourceMethodName(traces[2].getMethodName());
            entry.setSourceLineNumber(traces[2].getLineNumber());
        }
        return entry;
    }

    protected void doLog(LogEntry entry) {
        Intent bag = Intent.create("mano.service.logging", "log");
        bag.set("entry", entry);
        bag.submit(this.action);
    }

    public void info(CharSequence message, Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.INFO);
        entry.setMessage(message);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    public void info(CharSequence message) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.INFO);
        entry.setMessage(message);
        this.doLog(entry);
    }
    public void info(Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.INFO);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    
    
    public void debug(CharSequence message, Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.DEBUG);
        entry.setMessage(message);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    public void debug(CharSequence message) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.DEBUG);
        entry.setMessage(message);
        this.doLog(entry);
    }
    public void debug(Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.DEBUG);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    
    
    public void trace(CharSequence message, Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.TRACE);
        entry.setMessage(message);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    public void trace(CharSequence message) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.TRACE);
        entry.setMessage(message);
        this.doLog(entry);
    }
    public void trace(Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.TRACE);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    
    
    public void warn(CharSequence message, Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.WARNING);
        entry.setMessage(message);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    public void warn(CharSequence message) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.WARNING);
        entry.setMessage(message);
        this.doLog(entry);
    }
    public void warn(Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.WARNING);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    
    public void error(CharSequence message, Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.ERROR);
        entry.setMessage(message);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    public void error(CharSequence message) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.ERROR);
        entry.setMessage(message);
        this.doLog(entry);
    }
    public void error(Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.ERROR);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    
    public void fatal(CharSequence message, Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.FATAL);
        entry.setMessage(message);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
    public void fatal(CharSequence message) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.FATAL);
        entry.setMessage(message);
        this.doLog(entry);
    }
    public void fatal(Throwable thrown) {
        Thread thread = Thread.currentThread();
        LogEntry entry = create(thread, thread.getStackTrace());
        entry.setLevel(Level.FATAL);
        entry.setThrown(thrown);
        this.doLog(entry);
    }
}

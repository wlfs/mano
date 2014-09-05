/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import mano.DateTime;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class LogEntry {

    /**
     * @serial The Throwable (if any) associated with log message
     */
    private Throwable thrown;
    /**
     * @serial Logging message level
     */
    private Level level;
    /**
     * @serial Name of the source Logger.
     */
    private String loggerName;
    /**
     * @serial Class that issued logging call
     */
    private String sourceClassName;

    /**
     * @serial Method that issued logging call
     */
    private String sourceMethodName;

    /**
     * @serial Non-localized raw message text
     */
    private CharSequence message;

    /**
     * @serial Thread ID for thread that issued logging call.
     */
    private long threadId;
    
    private long lineNumber;
    
    private DateTime time;

    private transient boolean needToInferCaller;
    private transient Logger logger;
    private StackTraceElement trace;

    /**
     * Get the source Logger's name.
     *
     * @return source logger name (may be null)
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * Set the source Logger's name.
     *
     * @param name the source logger name (may be null)
     */
    public void setLoggerName(String name) {
        loggerName = name;
    }

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

    /**
     * Get the name of the class that (allegedly) issued the logging request.
     * <p>
     * Note that this sourceClassName is not verified and may be spoofed. This
     * information may either have been provided as part of the logging call, or
     * it may have been inferred automatically by the logging framework. In the
     * latter case, the information may only be approximate and may in fact
     * describe an earlier call on the stack frame.
     * <p>
     * May be null if no information could be obtained.
     *
     * @return the source class name
     */
    public String getSourceClassName() {
        return sourceClassName;
    }

    /**
     * Set the name of the class that (allegedly) issued the logging request.
     *
     * @param sourceClassName the source class name (may be null)
     */
    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    /**
     * Get the name of the method that (allegedly) issued the logging request.
     * <p>
     * Note that this sourceMethodName is not verified and may be spoofed. This
     * information may either have been provided as part of the logging call, or
     * it may have been inferred automatically by the logging framework. In the
     * latter case, the information may only be approximate and may in fact
     * describe an earlier call on the stack frame.
     * <p>
     * May be null if no information could be obtained.
     *
     * @return the source method name
     */
    public String getSourceMethodName() {
        return sourceMethodName;
    }

    /**
     * Set the name of the method that (allegedly) issued the logging request.
     *
     * @param sourceMethodName the source method name (may be null)
     */
    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }
    
    public void setSourceLineNumber(long line) {
        lineNumber=line;
    }
    public long getSourceLineNumber() {
        return lineNumber;
    }
    public DateTime getTime(){
        return time;
    }

    /**
     * Get the "raw" log message, before localization or formatting.
     * <p>
     * May be null, which is equivalent to the empty string "".
     * <p>
     * This message may be either the final text or a localization key.
     * <p>
     * During formatting, if the source logger has a localization ResourceBundle
     * and if that ResourceBundle has an entry for this message string, then the
     * message string is replaced with the localized value.
     *
     * @return the raw message string
     */
    public CharSequence getMessage() {
        return message;
    }

    /**
     * Set the "raw" log message, before localization or formatting.
     *
     * @param message the raw message string (may be null)
     */
    public void setMessage(CharSequence message) {
        this.message = message;
    }

    /**
     * Get an identifier for the thread where the message originated.
     * <p>
     * This is a thread identifier within the Java VM and may or may not map to
     * any operating system ID.
     *
     * @return thread ID
     */
    public long getThreadId() {
        return threadId;
    }

    /**
     * Set an identifier for the thread where the message originated.
     *
     * @param id the thread ID
     */
    public void setThreadId(long id) {
        this.threadId = id;
    }

    /**
     * Get any throwable associated with the log record.
     * <p>
     * If the event involved an exception, this will be the exception object.
     * Otherwise null.
     *
     * @return a throwable
     */
    public Throwable getThrown() {
        return thrown;
    }

    /**
     * Set a throwable associated with the log event.
     *
     * @param thrown a throwable (may be null)
     */
    public void setThrown(Throwable thrown) {
        this.thrown = thrown;
    }
    
    public void setTime(DateTime time){
        this.time=time;
    }
    
    public Logger getLogger(){
        return logger;
    }
    public void setLogger(Logger log){
        logger=log;
    }
}

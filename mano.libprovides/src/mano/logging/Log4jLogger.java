/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.logging;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.logging.Level;
import mano.util.logging.LogProvider;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author jun
 */
public class Log4jLogger implements LogProvider {

    Logger logger;

    public Log4jLogger() {
        //java.util.logging.LogManager.getLogManager().
        //PropertyConfigurator.configure("log4j.xml");
        //new org.apache.logging.log4j.simple.SimpleLogger();
        //org.apache.logging.log4j.LogManager.getLogger(null)
        logger =org.apache.logging.log4j.status.StatusLogger.getLogger();
        
        //org.apache.log4j.ConsoleAppender

    }
    public Log4jLogger(Class<?> clazz) {

        //PropertyConfigurator.configure("log4j.xml");
        //new org.apache.logging.log4j.simple.SimpleLogger();
        //org.apache.logging.log4j.LogManager.getLogger(null)
        logger =org.apache.logging.log4j.status.StatusLogger.getLogger();
//logger = LogManager.getLogger(clazz);
        //org.apache.log4j.ConsoleAppender

    }

    /*@Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(Object message) {
        logger.info(message);
    }

    @Override
    public void info(Object message, Throwable t) {
        logger.info(message, t);
    }

    @Override
    public void infoFormat(String format, Object... args) {
        logger.info(String.format(format, args));
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(Object message) {
        logger.debug(message);
    }

    @Override
    public void debug(Object message, Throwable t) {
        logger.debug(message, t);
    }

    @Override
    public void debugFormat(String format, Object... args) {
        logger.debug(format, args);

    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(Object message) {
        logger.trace(message);
    }

    @Override
    public void trace(Object message, Throwable t) {
        logger.trace(message, t);

    }

    @Override
    public void traceFormat(String format, Object... args) {
        trace(String.format(format, args));
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(Object message) {
        logger.error(message);
    }

    @Override
    public void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    @Override
    public void errorFormat(String format, Object... args) {
        logger.error(format, args);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(Object message) {
        logger.warn(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        logger.warn(message, t);
    }

    @Override
    public void warnFormat(String format, Object... args) {
        logger.warn(format, args);
    }*/

    @Override
    public boolean isEnabled(int level) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(int level, Object obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(int level, String format, Object... args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(int level, String message, Throwable t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

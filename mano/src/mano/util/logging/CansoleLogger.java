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
public class CansoleLogger implements ILogger {

    public CansoleLogger(){}
    public CansoleLogger(Class<?> clazz){}
    
    @Override
    public boolean isEnabled(int level) {
        return true;
    }

    @Override
    public void log(int level, Object obj) {
        System.out.print(DateTime.now());
        System.out.print(" ["+level+"] ");
        System.out.println(obj);
    }

    @Override
    public void log(int level, String format, Object... args) {
        System.out.print(DateTime.now());
        System.out.print(" ["+level+"] ");
        System.out.println(String.format(format, args));
    }

    @Override
    public void log(int level, String message, Throwable t) {
        System.out.print(DateTime.now());
        System.out.print(" ["+level+"] ");
        System.out.println(message);
        if(t!=null){
            t.printStackTrace(System.out);
        }
    }
}

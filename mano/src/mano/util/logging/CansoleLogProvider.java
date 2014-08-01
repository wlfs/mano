/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.util.logging;

import java.util.Map;
import mano.DateTime;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class CansoleLogProvider implements LogProvider {

    public CansoleLogProvider(){}
    public CansoleLogProvider(Class<?> clazz){}
    
    @Override
    public boolean isEnabled(int level) {
        return true;
    }

    @Override
    public void write(int level, Object obj) {
        System.out.print(DateTime.now());
        System.out.print(" ["+level+"] ");
        System.out.println(obj);
    }

    @Override
    public void write(int level, String format, Object... args) {
        System.out.print(DateTime.now());
        System.out.print(" ["+level+"] ");
        System.out.println(String.format(format, args));
    }

    @Override
    public void write(int level, String message, Throwable t) {
        System.out.print(DateTime.now());
        System.out.print(" ["+level+"] ");
        System.out.println(message);
        if(t!=null){
            t.printStackTrace(System.out);
        }
    }

    @Override
    public void init(Map<String, String> params) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

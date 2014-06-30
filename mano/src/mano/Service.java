/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import mano.util.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Service implements Runnable {

    protected Activator act;
    protected Logger log;
    protected String name;
    

    public Logger logger() {
        return this.log;
    }

    public Activator activator() {
        return this.act;
    }

    public void init(String serviceName, Activator activator, Logger logger) {
        this.name=serviceName;
        this.act = activator;
        this.log = logger;
    }
    
    public void param(String name,Object value){
        System.out.println(name + "=" + value);
    }
    
    public Object param(String name){
        return null;
    }

    public void stop(){
        
    }
    
    @Override
    public String toString(){
        return this.getClass()+"["+ this.name +"]";
    }
}

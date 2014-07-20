/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import java.util.Map;
import mano.util.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Service implements Runnable {
    private ServiceContainer container;
    

    public void init(ServiceContainer container, Map<String, String> params) {
        this.container=container;
    }

    /*public void init(String serviceName, Activator activator, Logger logger) {
     this.name=serviceName;
     this.loader = activator;
     this.logger = logger;
     }
    
    public void param(String name, Object value) {
        System.out.println(name + "=" + value);
    }

    public Object param(String name) {
        return null;
    }*/

    public void stop() {

    }

    public ServiceContainer getContainer() {
        return container;
    }

    public abstract String getServiceName();

    @Override
    public String toString() {
        return this.getClass() + "[" + getServiceName() + "]";
    }
}

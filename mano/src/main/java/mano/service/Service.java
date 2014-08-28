/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.util.Map;
import java.util.Properties;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Service implements Runnable {

    private ServiceContainer container;
    private Properties properties;

    protected Service() {
        properties = new Properties();
        container = ServiceManager.getInstance();
    }

    public void init(ServiceContainer container, Map<String, String> params) {
        this.container = container;

    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String def) {
        return properties.getProperty(key, def);
    }

    public void stop() {
        
    }

    public void onStart() {
        ServiceManager.getInstance().regisiter(this);
    }

    public ServiceContainer getContainer() {
        return container;
    }
    
    public void process(Intent intent) throws Exception{
        
    }

    public abstract String getServiceName();

    @Override
    public String toString() {
        return this.getClass() + "[" + getServiceName() + "]";
    }
}

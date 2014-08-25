/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import mano.ContextClassLoader;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ServiceManager implements ServiceContainer {

    private HashMap<String, Service> services = new HashMap<>();
    private ContextClassLoader classLoader;
    @Override
    public Service getService(String serviceName) {
        if (serviceName != null && services.containsKey(serviceName)) {
            return services.get(serviceName);
        }
        return null;
    }

    private static ServiceManager instance;

    static {
        instance = new ServiceManager();
    }

    public static ServiceManager getInstance() {
        return instance;
    }
    
    public void setLoader(ContextClassLoader loader){
        classLoader=loader;
    }
    public ContextClassLoader getLoader(){
        return classLoader;
    }

    public void regisiter(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("service is required");
        } else if (instance.services.containsKey(service.getServiceName())) {
            throw new IllegalArgumentException("service name was already regsition.name:" + service.getServiceName());
        }
        instance.services.put(service.getServiceName(), service);
    }

    public static Intent send(String serviceName, String action) {
        Intent intent = new Intent(serviceName, action);
        return intent;
    }

}

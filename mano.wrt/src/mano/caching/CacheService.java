/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.caching;

import java.util.Map;
import mano.Service;
import mano.ServiceContainer;
import mano.ServiceProvider;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class CacheService extends Service implements ServiceProvider {

    HashCacheProvider provider;

    @Override
    public void init(ServiceContainer container, Map<String, String> params) {
        super.init(container, params);
        provider = new HashCacheProvider();
    }

    @Override
    public String getServiceName() {
        return "cache.service";
    }

    @Override
    public void run() {

    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        if (CacheProvider.class.getName().equals(serviceType.getName())) {
            return (T) provider;
        }
        return null;
    }

}

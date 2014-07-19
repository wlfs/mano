/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.caching.CacheProvider;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpSession {

    private CacheProvider provider;
    private static final String COOKIE_KEY = "--MANO$SID--";

    public String getSessionId() {
        return "--MANO$SID--";
    }

}

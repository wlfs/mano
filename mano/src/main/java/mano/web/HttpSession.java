/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.util.HashMap;
import java.util.UUID;
import mano.caching.CacheProvider;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpSession {

    private CacheProvider provider;
    private String sid;
    private boolean isnew;
    public static final String COOKIE_KEY = "MANOSESSIONID";

    protected HttpSession(CacheProvider provider) {
        this(Integer.toHexString(UUID.randomUUID().hashCode()), provider);
        isnew = true;
        this.provider.set(sid, new HashMap<>(), 1000 * 60 * 20, true, null);
    }

    protected HttpSession(String sessionId, CacheProvider provider) {
        this.sid = sessionId;
        this.provider = provider;
    }

    public static HttpSession getSession(String sessionId, CacheProvider provider) {
        if (sessionId == null || "".equals(sessionId) || !provider.contains(sessionId)) {
            return new HttpSession(provider);
        }
        return new HttpSession(sessionId, provider);
    }

    public final String getSessionId() {
        return sid;
    }

    public final boolean isNewSession() {
        return isnew;
    }

    public void set(String name, Object val) {
        this.provider.set(sid, name, val);
    }

    public Object get(String name) {
        return this.provider.get(sid, name);
    }

    public void remove(String name) {
        this.provider.remove(sid, name);
    }
}

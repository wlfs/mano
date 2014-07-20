/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mano.DateTime;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpCookie implements HttpResponseCookie {

    private Map<String, CookieEntry> entries = new HashMap<>();

    @Override
    public String set(String key, Object value, int expires, String path, String domain, boolean httpOnly, boolean secure) {
        DateTime dt = null;
        if (expires != 0) {
            dt = DateTime.now();
            dt.AddSeconds(expires);
        }
        return this.set(key, value, dt, path, domain, httpOnly, secure);
    }

    @Override
    public String set(String key, Object value, DateTime expires, String path, String domain, boolean httpOnly, boolean secure) {
        CookieEntry entry;
        if (entries.containsKey(key)) {
            entry = entries.get(key);
        } else {
            entry = new CookieEntry();
            entry.key = key;
            entries.put(key, entry);
        }
        entry.value = value == null ? "" : value.toString();
        entry.domain = domain;
        entry.expires = expires;
        entry.httponly = httpOnly;
        entry.secure = secure;
        return entry.value;
    }

    @Override
    public String set(String key, Object value, int expires) {
        return this.set(key, value, expires, null, null, false, false);
    }

    @Override
    public String set(String key, Object value, DateTime expires) {
        return this.set(key, value, expires, null, null, false, false);
    }

    @Override
    public String set(String key, Object value) {
        return this.set(key, value, 0, null, null, false, false);
    }

    @Override
    public String get(String key) {
        if (entries.containsKey(key)) {
            return entries.get(key).getValue();
        }
        return null;
    }

    @Override
    public Iterable<CookieEntry> iterator() {
        return entries.values();
    }

    public class CookieEntry implements Entry<String, String> {

        private String key;
        private String value;
        private DateTime expires;
        private String domain;
        private String path;
        private boolean secure;
        private boolean httponly;

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            this.value = value;
            return value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            sb.append("=");
            sb.append(value);

            if (expires != null) {
                sb.append(";expires=");
                sb.append(expires.toGMTString());

            }
            if (domain != null && !"".equals(domain)) {
                sb.append(";domain="); //TODO: 临时cookie不能有些域？
                sb.append(domain);
            }
            if (path != null && !"".equals(path)) {
                sb.append(";path=");
                sb.append(path);
            }
            if (secure) {
                sb.append(";secure");
            }
            if (httponly) {
                sb.append(";httponly");
            }
            return sb.toString();
        }

    }

}

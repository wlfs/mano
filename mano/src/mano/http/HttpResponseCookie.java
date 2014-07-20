/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.DateTime;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface HttpResponseCookie extends HttpRequestCookie {
    String set(String key, Object value, DateTime expires, String path, String domain, boolean httpOnly, boolean secure);
    String set(String key, Object value, int expires, String path, String domain, boolean httpOnly, boolean secure);
    String set(String key, Object value, int expires);
    String set(String key, Object value, DateTime expires);
    String set(String key, Object value);
    
}

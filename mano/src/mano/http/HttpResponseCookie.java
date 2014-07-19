/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface HttpResponseCookie extends HttpRequestCookie {

    String set(String key, Object value, String expires, String path, String domain, boolean httpOnly, boolean secure);
}

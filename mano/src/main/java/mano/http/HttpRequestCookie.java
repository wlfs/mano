/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.http;

/**
 * 提供一组用于读取 HTTP Cookie 的方法。
 * @author jun <jun@diosay.com>
 */
public interface HttpRequestCookie {
    /**
     * 从 Cookie 集合中返回具有指定名称的 Cookie。
     * @param key 要从集合中检索的 Cookie 的名称。 
     * @return 按 name 指定的 HttpCookie。
     */
    String get(String key);
    
    /**
     * 返回集合的迭代器。 
     * @return 
     */
    Iterable<HttpCookieCollection.CookieEntry> iterator();
}

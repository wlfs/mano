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
 * 提供一组用于写入 HTTP Cookie 的方法。
 * @author jun <jun@diosay.com>
 */
public interface HttpResponseCookie extends HttpRequestCookie {
    /**
     * 设置一个 Cookie 到集合中，如果 key 已经存在集合中则会替换原有项。
     * @param key 名称。 
     * @param value 值。 
     * @param expires 过期时间。 
     * @param path 当前 Cookie 一起传输的虚拟路径。
     * @param domain 将此 Cookie 与其关联的域。
     * @param httpOnly 指定 Cookie 是否可通过客户端脚本访问。
     * @param secure 指示是否使用安全套接字层 (SSL)（即仅通过 HTTPS）传输 Cookie。
     * @return 待传输的Cookie 值。
     */
    String set(String key, Object value, DateTime expires, String path, String domain, boolean httpOnly, boolean secure);
    /**
     * 设置一个 Cookie 到集合中，如果 key 已经存在集合中则会替换原有项。
     * @param key 名称。 
     * @param value 值。 
     * @param expires 过期时间。 
     * @param path 当前 Cookie 一起传输的虚拟路径。
     * @param domain 将此 Cookie 与其关联的域。
     * @param httpOnly 指定 Cookie 是否可通过客户端脚本访问。
     * @param secure 指示是否使用安全套接字层 (SSL)（即仅通过 HTTPS）传输 Cookie。
     * @return 待传输的Cookie 值。
     */
    String set(String key, Object value, int expires, String path, String domain, boolean httpOnly, boolean secure);
    /**
     * 设置一个 Cookie 到集合中，如果 key 已经存在集合中则会替换原有项。
     * @param key 名称。 
     * @param value 值。 
     * @param expires 过期时间。 
     * @return 待传输的Cookie 值。
     */
    String set(String key, Object value, int expires);
    /**
     * 设置一个 Cookie 到集合中，如果 key 已经存在集合中则会替换原有项。
     * @param key 名称。 
     * @param value 值。 
     * @param expires 过期时间。 
     * @return 待传输的Cookie 值。
     */
    String set(String key, Object value, DateTime expires);
    
    /**
     * 设置一个 Cookie 到集合中，如果 key 已经存在集合中则会替换原有项。
     * @param key 名称。 
     * @param value 值。 
     * @return 待传输的Cookie 值。
     */
    String set(String key, Object value);
    
}

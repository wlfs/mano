/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.web.HttpSession;
import mano.web.WebApplication;

/**
 * 封装有关个别 HTTP 请求的所有 HTTP 特定的信息。
 * @author jun <jun@diosay.com>
 */
public interface HttpContext {
    
    /**
     * 获取当前 HTTP 请求所关联的 WebApplication 对象。
     * @return 
     */
    WebApplication getApplication();
    
    /**
     * 获取一个值，以指示当前 HTTP 请求是否处理完成。
     * @return 
     */
    boolean isCompleted();
    
    /**
     * 获取当前 HTTP 请求所关联的 HttpRequest 对象。
     * @return 
     */
    HttpRequest getRequest();

    /**
     * 获取当前 HTTP 请求所关联的 HttpResponse 对象。
     * @return 
     */
    HttpResponse getResponse();
    
    /**
     * 获取当前 HTTP 请求所关联的 HttpServer 对象。
     * @return 
     */
    HttpServer getServer();
    
    /**
     * 获取当前 HTTP 请求所关联的 HttpSession 对象。
     * @return 
     */
    HttpSession getSession();
}

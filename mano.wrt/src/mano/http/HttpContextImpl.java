/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.web.WebApplication;
import mano.web.HttpSession;

/**
 * HttpContext的实现
 *
 * @author jun(jun@diosay.com)
 */
class HttpContextImpl implements HttpContext {

    HttpRequestImpl request;
    HttpResponseImpl response;
    HttpSession session;
    boolean completed;
    HttpServer server;
    WebApplication application;

    HttpContextImpl(HttpRequestImpl req, HttpResponseImpl rsp) {
        request = req;
        response = rsp;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public WebApplication getApplication() {
        return application;
    }
    
    @Override
    public HttpServer getServer() {
        return server;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }
    
    @Override
    public boolean isCompleted() {
        return completed;
    }
}

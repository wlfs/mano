/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.ContextClassLoader;
import mano.http.HttpContext;
import mano.http.HttpPostFile;
import mano.http.HttpRequest;
import mano.http.HttpResponse;
import mano.util.json.JsonConvert;
import mano.util.json.JsonConverter;
import mano.util.logging.CansoleLogProvider;
import mano.util.logging.Logger;

/**
 *
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Controller {

    
    private HttpResponse response;
    private HttpRequest request;
    private HttpContext context;
    private RequestService service;
    private JsonConverter jsonConverter;

    private final void setService(RequestService rs) {
        service = rs;
        context = rs.getContext();
    }
    
    private RequestService getService(){
        if(this.service==null){
            throw new mano.InvalidOperationException("未初始化服务。");
        }
        return this.service;
    }

    public void set(String name, Object value) {
        getService().set(name, value);
    }

    public Object get(String name) {
        return getService().get(name);
    }

    public String query(String name) {
        return getContext().getRequest().query().get(name);
    }

    public String form(String name) {
        return getContext().getRequest().form().get(name);
    }

    public HttpPostFile file(String name) {
        return getContext().getRequest().files().get(name);
    }

    public void session(String name, Object value) {
        getContext().getSession().set(name, value);
    }

    public Object session(String name) {
        return getContext().getSession().get(name);
    }

    public void cookie(String name, Object value) {
        getContext().getResponse().getCookie().set(name, value);
    }

    public String cookie(String name) {
        return getContext().getRequest().getCookie().get(name);
    }

    protected void view() {
        getService().setResult(new ViewResult());
    }

    protected void view(String action) {
        getService().setAction(action);
        getService().setResult(new ViewResult());
    }

    protected void view(String action, String controller) {
        getService().setAction(action);
        getService().setController(controller);
        getService().setResult(new ViewResult());
    }

    protected void template(String path) {
        getService().setPath(path);
        //throw new UnsupportedOperationException();
        getService().setResult(new ViewResult());
    }

    protected void text(String content) {
        this.text(content, "text/plain;charset=utf-8");
    }

    protected void text(String content, String contentType) {
        getContext().getResponse().setContentType(contentType);
        getContext().getResponse().write(content);
    }
    

    protected void json(Object src) {
        if (jsonConverter == null) {
            jsonConverter = JsonConvert.getConverter(getLoader());
        }
        getContext().getResponse().setContentType("application/json;charset=utf-8");
        getContext().getResponse().write(jsonConverter.serialize(src));
    }
    
    public HttpContext getContext(){
        return getService().getContext();
    }
    
    public WebApplication getApplication(){
        return getContext().getApplication();
    }

    public Logger getLogger() {
        return new Logger(new CansoleLogProvider());
    }
    
    public ContextClassLoader getLoader() {
        return getApplication().getLoader();
    }

}

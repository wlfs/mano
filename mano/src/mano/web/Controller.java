/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.http.HttpContext;
import mano.http.HttpPostFile;
import mano.http.HttpRequest;
import mano.http.HttpResponse;
import mano.util.json.JsonConvert;

/**
 *
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Controller {

    protected HttpResponse response;
    protected HttpRequest request;
    protected HttpContext context;
    private RequestService service;
    /*public Controller(HttpContext context){
     if(context==null){
     throw new NullPointerException();
     }
     this.context=context;
     this.request=context.getRequest();
     this.response=context.getResponse();
     }*/

    private final void setService(RequestService rs) {
        service = rs;
        context = rs.getContext();
    }
    
    public void set(String name,Object value){
        service.set(name, value);
    }
    
    public Object get(String name){
        return service.get(name);
    }
    
    public String query(String name){
        return service.getContext().getRequest().query().get(name);
    }
    
    public String form(String name){
        return service.getContext().getRequest().form().get(name);
    }
    
    public HttpPostFile file(String name){
        return service.getContext().getRequest().files().get(name);
    }
    
    public void session(String name,Object value){
        service.getContext().getSession().set(name, value);
    }
    
    public Object session(String name){
        return service.getContext().getSession().get(name);
    }
    
    public void cookie(String name,Object value){
        service.getContext().getResponse().getCookie().set(name, value);
    }
    
    public String cookie(String name){
        return service.getContext().getRequest().getCookie().get(name);
    }

    protected void view() {
        service.setResult(new ViewResult());
    }

    protected void view(String action) {
        service.setAction(action);
        service.setResult(new ViewResult());
    }

    protected void view(String action, String controller) {
        service.setAction(action);
        service.setController(controller);
        service.setResult(new ViewResult());
    }

    protected void template(String path) {
        service.setResult(new ViewResult());
    }

    protected void text(String content) {
        this.text(content, "text/plain;charset=utf-8");
    }

    protected void text(String content, String contentType) {
        service.getContext().getResponse().setContentType(contentType);
        service.getContext().getResponse().write(content);
    }

    protected void json(Object src) {
        service.getContext().getResponse().setContentType("application/json;charset=utf-8");
        service.getContext().getResponse().write(JsonConvert.serialize(src));
    }

}

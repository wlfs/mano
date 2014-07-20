/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.http.HttpContext;
import mano.http.HttpRequest;
import mano.http.HttpResponse;

/**
 * u.diosay.com=host ok w.diosay.com=host na
 *
 * @author jun <jun@diosay.com>
 */
@Module("{host=token}/home/{lang}")
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
    
    private final void setService(RequestService rs){
        service=rs;
        context=rs.getContext();
    }

    protected void view() {
        service.setResult(new ViewResult());
    }

    protected void view(String action) {
        service.setAction(action);
        service.setResult(new ViewResult());
    }

    public void view(String action, String controller) {
        service.setAction(action);
        service.setController(controller);
        service.setResult(new ViewResult());
    }

    public void template(String path) {
        service.setResult(new ViewResult());
    }

}

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
 * u.diosay.com=host ok
 * w.diosay.com=host na
 * @author jun <jun@diosay.com>
 */
@Module("{host=token}/home/{lang}")
public abstract class Controller {
    protected final HttpResponse response;
    protected final HttpRequest request;
    protected final HttpContext context;
    RouteService service;
    public Controller(HttpContext context){
        if(context==null){
            throw new NullPointerException();
        }
        this.context=context;
        this.request=context.getRequest();
        this.response=context.getResponse();
    }
    
    @Action(value="",method=9)
    void view(String path){
        
        service.set("", path);
        service.setResult(null);
        
    }
    //actionMapping("/index/{name?}/{value?}",POST)
    void view(String action,String controller,String app){
        //host/app/controller/action/args?query
        //return newindex("index")
        
    }
    
    class TestView implements View{
        
        @Override
        public void execute(RouteService service) {
            
            service.getContext().getResponse().write("<html>", "");
            if(service.get("")==null){
                
            }
            service.getContext().getResponse().write("</html>", "");
        }
        
    }
    
}

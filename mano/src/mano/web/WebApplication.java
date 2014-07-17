/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import mano.Activator;
import mano.InvalidOperationException;
import mano.http.HttpContext;
import mano.http.HttpModule;
import mano.http.HttpStatus;
import mano.util.Logger;
import mano.util.Utility;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplication {

    private Set<HttpModule> _modules;
    private Activator loader;
    private String basedir;
    private String viewdir;
    private Logger logger;

    public Activator getLoader() {
        return loader;
    }

    public Logger getLogger() {
        return logger;
    }

    public final void init(WebApplicationStartupInfo info) {

        loader = new Activator(info.service.getLoader());
        logger = info.service.getLogger();
        _modules = new LinkedHashSet<>();
        basedir = info.path;

        if (basedir.startsWith("./") || basedir.startsWith(".\\")) {
            basedir = Utility.combinePath(info.serverPath, basedir.substring(1)).toString();
        } else if (basedir.startsWith("/") || basedir.startsWith("\\")) {
            basedir = Utility.combinePath(info.serverPath, basedir).toString();
        }

        for (mano.http.HttpModuleSettings settings : info.modules.values()) {
            try {
                HttpModule mod = (HttpModule) loader.newInstance(settings.type);
                if (mod != null) {
                    mod.init(this, settings.params);
                    _modules.add(mod);
                }
            } catch (InstantiationException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        //loader.
        //host base
        //config
        //path
        //root
        //index
        //mappings
        //post limits
        //auth
        //logger
        //loader
        //items
        //session state
    }

    public final void destory() {

    }

    /**
     * 任何实现HTTP Service 都须要调用该方法。 基础代码，除非你清楚的知道你要做什么，否则不建议在用户代码中调用该方法。
     *
     * @param context
     */
    public void init(HttpContext context) {
        //触发begin事件
        //context.setApplication(this);

        //创建session
        //获取session
        //获取handlers
        //循环调用
        System.out.println(context.getRequest().rawUrl());
        boolean processed = false;
        for (HttpModule module : _modules) {
            if (module.handle(context)) {
                processed = true;
                break;
            }
        }
        if (!processed) {
            try {
                context.getResponse().setHeader("Connection", "close");
                context.getResponse().status(HttpStatus.NotFound);
            } catch (InvalidOperationException ignored) {
                //
            }
            context.getResponse().write("<html><head><title>%d Error</title></head><body>%s<body></html>", 404, "Not Found");
            context.getResponse().end();
        }
        
        if(!context.isCompleted()){
            context.getResponse().end();
        }
        
        /*
         this.onBeginRequest(context);
         if (context.handler() == null) {
         HttpModule handler = resolveHandler(context);
         if (handler == null) {
         //404
         }
         context.handler(handler);
         }
        
         this.postHandlerExecute(context);
         */
    }

    public String getBasedir() {
        return basedir;
    }


    /*
     class HttpModuleSettings {
     public String path;
     public String verb;
     public String mime;
     public String type;
     public String args;
        
     }

     protected HttpModule resolveHandler(HttpContext context) {
     String path = context.request().url().getPath();//+context.request().url().getQuery();
        
     //path.html
     return new HttpModule() {

     @Override
     public void init(Map<String, String> params) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public boolean handle(HttpContext context) {
     context.response().write("hello world");
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public void dispose() {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }


     };

     }

     protected final void postHandlerExecute(HttpContext context) {
     new Thread(new Runnable() {
     @Override
     public void run() {
     //http://www.cnblogs.com/tangself/archive/2011/03/28/1998007.html
     //java.util.Timer timer=new java.util.Timer(); http://www.cnblogs.com/jinspire/archive/2012/02/10/2345256.html

     context.handler().handle(context);
     if (!context.isCompleted()) {
     context.response().end();
     //throw new InterruptedException();
     }
     }
     }).start();
     //触发end事件
     }
    
     protected void acquireRequestState(){
     //触发session事件
     }
    
     //只是一个开始事件
     protected void onBeginRequest(HttpContext context){
     if(true){ //err
     context.handler(null);//errhandler
     }
     }
    
     //只是一个结束事件
     protected void onEndRequest(HttpContext context){
        
     }
     */
}

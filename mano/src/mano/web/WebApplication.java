/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import mano.Activator;
import mano.ContextClassLoader;
import mano.InvalidOperationException;
import mano.http.HttpContext;
import mano.http.HttpException;
import mano.http.HttpModule;
import mano.http.HttpStatus;
import mano.service.ServiceProvider;
import mano.util.Utility;
import mano.util.logging.LogProvider;
import mano.util.logging.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplication {

    private Set<HttpModule> modules;
    private ContextClassLoader loader;
    private LogProvider logger;
    private WebApplicationStartupInfo startupInfo;

    public ContextClassLoader getLoader() {
        return loader;
    }

    public Logger getLogger() {
        return loader.getLogger();
    }

    private void init(WebApplicationStartupInfo info, ContextClassLoader activator) {
        startupInfo = info;
        loader = activator;

        modules = new LinkedHashSet<>();
        for (mano.http.HttpModuleSettings settings : info.modules.values()) {
            try {
                HttpModule mod = (HttpModule) loader.newInstance(settings.type);
                if (mod != null) {
                    mod.init(this, settings.params);
                    modules.add(mod);
                }
            } catch (InstantiationException | ClassNotFoundException ex) {
                loader.getLogger().error("WebApplication.init(modules)", ex);
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

    /**
     * 获取用户配置参数。
     *
     * @param name 配置名称。
     * @return 值。
     */
    public final String getSettingValue(String name) {
        if (this.startupInfo.settings.containsKey(name)) {
            return this.startupInfo.settings.get(name);
        }
        return null;
    }

    /**
     * 获取应用程序根目录。
     *
     * @return
     */
    public final String getApplicationPath() {
        return this.startupInfo.getServerInstance().getBaseDirectory();
    }

    public final void destory() {

    }
    private HashMap<String,Object> items=new HashMap<>();
    public Object get(String name){
        if(items.containsKey(name)){
            return items.get(name);
        }
        return null;
    }
    public void set(String name,Object value){
        items.put(name, value);
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
        //System.out.println(context.getRequest().rawUrl());
        boolean processed = false;
        for (HttpModule module : modules) {
            if (module.handle(context)) {
                processed = true;
                break;
            }
        }

        if (!processed) {
            this.onError(context, new HttpException(HttpStatus.NotFound, "404 Not Found"));
        }
    }

    protected void onError(HttpContext context, Throwable t) {
        HttpException ex;
        if (t instanceof HttpException) {
            ex = (HttpException) t;
        } else {
            ex = new HttpException(HttpStatus.InternalServerError, t);
        }

        try {
            context.getResponse().setHeader("Connection", "close");
            context.getResponse().status(ex.getHttpCode());
        } catch (Exception e) {
            //ignored
        }

        try {
            context.getResponse().write("<html><head><title>%d Error</title></head><body>%s<body></html>", ex.getHttpCode(), ex.getMessage());
        } catch (Exception e) {
            //ignored
        }
    }
}

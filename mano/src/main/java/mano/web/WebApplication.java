/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import mano.ContextClassLoader;
import mano.PropertyContext;
import mano.http.HttpContext;
import mano.http.HttpException;
import mano.http.HttpModule;
import mano.http.HttpStatus;
import mano.util.Utility;
import mano.util.logging.Logger;

/**
 * 定义 Web 应用程序中的所有应用程序对象通用的方法、属性和事件。
 * @author jun <jun@diosay.com>
 */
public class WebApplication extends PropertyContext {

    
    private Set<HttpModule> modules;
    private ContextClassLoader loader;
    private WebApplicationStartupInfo startupInfo;
    private final HashMap<String, Object> items = new HashMap<>();
    
    public ContextClassLoader getLoader() {
        return loader;
    }

    public Logger getLogger() {
        return loader.getLogger();
    }

    /**
     * 初始化应用程序。
     * @param info
     * @param l 
     */
    final void init(WebApplicationStartupInfo info, ContextClassLoader l) {
        startupInfo = info;
        loader = l;
        this.setProperties(new Properties());
        modules = new LinkedHashSet<>();
        for (mano.http.HttpModuleSettings settings : info.modules.values()) {
            try {
                HttpModule mod = (HttpModule) loader.newInstance(settings.type);
                if (mod != null) {
                    mod.init(this, settings.settings);
                    modules.add(mod);
                }
            } catch (Throwable ex) {
                getLogger().warn("Failed to initialize the HTTP module:", ex);
            }
        }
        this.onInit();
    }

    final void destory() {
        onDestory();
        startupInfo.app = null;
        if (modules != null) {
            for (HttpModule module : modules) {
                try {
                    module.dispose();
                } catch (Exception ingored) {
                }
            }
            modules.clear();
        }
        items.clear();
        loader=null;
    }

    /**
     * 获取应用程序根目录。
     *
     * @return
     */
    public final String getApplicationPath() {
        return this.startupInfo.getServerInstance().getBaseDirectory();
    }

    /**
     * 获取一个用于在应用程序各 HttpContext 之间交互的对象。
     * @param name
     * @return 
     */
    public final Object get(String name) {
        if (items.containsKey(name)) {
            return items.get(name);
        }
        return null;
    }

    /**
     * 设置一个用于在应用程序各 HttpContext 之间交互的对象。
     * @param name
     * @param value 
     */
    public final void set(String name, Object value) {
        items.put(name, value);
    }

    /**
     * 任何实现HTTP Service 都须要调用该方法。 
     * <p>基础代码，除非你清楚的知道你要做什么，否则不建议在用户代码中调用该方法。
     *
     * @param context
     */
    public void processRequest(HttpContext context) {
        boolean processed = false;

        ArrayList<String> paths = new ArrayList<>();
        String path = context.getRequest().url().getPath();
        paths.add(Utility.combinePath(context.getServer().getVirtualPath(), path).toString());
        if (true) {
            for (String s : startupInfo.documents) {
                paths.add(Utility.combinePath(context.getServer().getVirtualPath(), path, s).toString());
            }
            paths.add(Utility.combinePath(context.getServer().getVirtualPath(), path, startupInfo.controller, startupInfo.action).toString());
            paths.add(Utility.combinePath(context.getServer().getVirtualPath(), path, startupInfo.action).toString());
        }
        for (String p : paths) {
            p = p.replace('\\', '/');
            if (!p.startsWith("/")) {
                p = "/" + p;
            }
            for (HttpModule module : modules) {
                if (module.handle(context, p)) {
                    processed = true;
                    break;
                }
            }
            if (processed) {
                break;
            }
        }

        if (!processed) {
            this.onError(context, new HttpException(HttpStatus.NotFound, "404 Not Found"));
        }

        if (!context.isCompleted()) {
            context.getResponse().end();
        }

    }
    protected void onInit(){}
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

    protected void onDestory() {

    }
}

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mano.http.HttpContext;
import mano.http.HttpModule;
import mano.util.Utility;
import java.net.URLDecoder;
import java.util.Properties;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class UrlRouteModule implements HttpModule {

    class JarScanner {

        private String[] jarFiles;

        public void scan(String path) {
            java.io.File dir = new java.io.File(path);
            dir.list(new FilenameFilter() {

                @Override
                public boolean accept(File tmp, String name) {
                    if (!name.toLowerCase().endsWith(".jar")) {
                        return false;
                    }

                    try {
                        scanJar(new URL("jar:file:/" + tmp.toString() + "/" + name + "!/"), name.substring(0, name.length() - 4));
                    } catch (MalformedURLException ex) {
                        if (java.lang.MANO_WEB_MACRO.DEBUG) {
                            app.getLogger().debug(null, ex);
                        }
                    }
                    return false;
                }
            });
        }

        public void scan(URL url) {
            String protocol = url.getProtocol().toLowerCase();
            if ("file".equals(protocol)) {
                try {
                    this.scanFile(new File(URLDecoder.decode(url.getFile(), "UTF-8")));
                } catch (UnsupportedEncodingException ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().debug(null, ex);
                    }
                }
            } else if ("jar".equals(protocol)) {
                scanJar(url, null);
            }
        }

        public void scanJar(URL url, String libname) {
            JarFile jar;
            try {
                jar = ((JarURLConnection) url.openConnection()).getJarFile();
            } catch (IOException ex) {
                app.getLogger().debug("URL:" + url.toString(), ex);
                return;
            }
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            String name;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                name = entry.getName();

                if (!entry.isDirectory() && name.endsWith(".class") && name.indexOf("$") < 1) {//
                    name = (name.substring(0, name.length() - 6)).replace('/', '.');
                    try {
                        onFoundClass(app.getLoader().loadClass(name));
                    } catch (Throwable ex) {
                        if (java.lang.MANO_WEB_MACRO.DEBUG) {
                            app.getLogger().debug(null, ex);
                        }
                    }
                }
            }
        }

        public void scanFile(File dir) {
            //获取此包的目录 建立一个File  
            //File dir = new File(path);
            //如果不存在或者 也不是目录就直接返回  
            if (!dir.exists()) {
                return;
            } else if (dir.isFile() && dir.getName().toLowerCase().endsWith(".jar")) {
                try {
                    if (File.separator.equals("\\")) {
                        scanJar(new URL("jar:file:/" + dir.toString() + "!/"), dir.getName().substring(0, dir.getName().length() - 4));
                    } else {
                        scanJar(new URL("jar:file://" + dir.toString() + "!/"), dir.getName().substring(0, dir.getName().length() - 4));
                    }
                } catch (MalformedURLException ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().debug(null, ex);
                    }
                }
            } else if (dir.isFile() && dir.getName().toLowerCase().endsWith(".class")) {
                //如果是java类文件 去掉后面的.class 只留下类名  
                String className = dir.getName().substring(0, dir.getName().length() - 6);
                try {
                    onFoundClass(app.getLoader().loadClass(className));
                } catch (Throwable ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().debug(null, ex);
                    }
                }
            } else if (dir.isDirectory()) {
                dir.listFiles((file) -> {
                    scanFile(file);
                    return false;
                });
            }
        }

        final Class<?> foundClass(String type) {
            try {
                return app.getLoader().loadClass(type);
            } catch (Throwable ex) {
                if (java.lang.MANO_WEB_MACRO.DEBUG) {
                    app.getLogger().debug(null, ex);
                }
            }
            return null;
        }

        final Pattern pattern = Pattern.compile("\\{\\s*(\\w+)\\s*\\}");

        public void onFoundClass(Class<?> clazz) {
            if (clazz == null) {
                return;
            }
            //.*/controller/action/(\w*)/(\w*).*
            //\{(\w+)(\?){0,1}\}
            //{?name}
            //default +1000
            UrlMapping mapping;
            String url = null;
            int verb = 0;
            boolean pojo = false;
            Annotation[][] ps;
            String pname;
            Map<Integer, String> map = new HashMap<>();
            String part;
            ArrayList<String> list = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            Matcher matcher = pattern.matcher(sb);
            Route route;

            //查找类，获取第1部分URL
            if (Controller.class.isAssignableFrom(clazz)) {
                mapping = clazz.getAnnotation(UrlMapping.class);
                if (mapping != null) {
                    url = mapping.value();
                }
                if (url == null || "".equals(url.trim())) {
                    url = clazz.getSimpleName().toLowerCase();
                    if (url.endsWith("controller")) {
                        url = url.substring(0, url.length() - 10);
                    }
                }
            } else {
                mapping = clazz.getAnnotation(UrlMapping.class);
                if (mapping == null) {
                    return;
                }
                try {
                    clazz.getMethod("setService", ActionContext.class);
                } catch (Throwable ex) {
                    return;
                }
                url = mapping.value();
                verb = mapping.verb();
                pojo = true;
                if (url == null || "".equals(url.trim())) {
                    url = clazz.getSimpleName().toLowerCase();
                    if (url.endsWith("controller")) {
                        url = url.substring(0, url.length() - 10);
                    }
                }
            }
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            if (!url.endsWith("/")) {
                url += "/";
            }

            //查找方法，获取第2部分URL 和签名参数
            for (Method method : clazz.getDeclaredMethods()) {
                map.clear();
                mapping = method.getAnnotation(UrlMapping.class);
                if (mapping == null) {
                    continue;
                }

                if (mapping.verb() > 0) {
                    verb = mapping.verb(); //重写父级定义
                }

                part = mapping.value();

                if (part == null || "".equals(part.trim())) {
                    part = method.getName();
                }
                if (part.startsWith("/")) {
                    part = part.substring(1);
                }

                list.clear();
                sb.setLength(0);
                sb.append(url);
                sb.append(part);
                matcher = pattern.matcher(sb);
                while (matcher.find()) {
                    String name = matcher.group(1);
                    list.add(name);
                    sb.replace(matcher.start(), matcher.end(), "(?<" + name + ">\\w+)");
                }
                //解决最后一个元素不能被替换的BUG
                matcher = pattern.matcher(sb);
                while (matcher.find()) {
                    String name = matcher.group(1);
                    list.add(name);//{1}{2}
                    sb.replace(matcher.start(), matcher.end(), "(?<" + name + ">\\w+)");
                }

                //http://blog.sina.com.cn/s/blog_72827fb10101pl9i.html
                //http://blog.sina.com.cn/s/blog_72827fb10101pl9j.html
                if (sb.charAt(sb.length() - 1) != '$') {
                    if (sb.charAt(sb.length() - 1) == '/') {
                        sb.append("{0,1}$");
                    } else {
                        sb.append("/{0,1}$");
                    }
                }
                if (sb.charAt(0) != '^') {
                    sb.insert(0, "^");
                }

                //参数映射集合
                ps = method.getParameterAnnotations();
                for (int i = 0; i < ps.length; i++) {
                    for (int j = 0; j < ps[i].length; j++) {
                        if (ps[i][j] instanceof PathParam) {
                            pname = ((PathParam) ps[i][j]).value();
                        } else {
                            pname = "";
                        }
                        if ("".equals(pname) || !list.contains(pname)) {
                            continue;
                        }
                        if (map.containsKey(i)) {
                            //map.put(i, map.get(i) + "," + pname);
                        } else {
                            map.put(i, pname);
                        }
                        break;
                    }
                }

                route = new Route();
                method.setAccessible(true);
                route.call = method;
                route.clazz = clazz;
                route.paramsMapping.putAll(map);
                route.patten = sb.toString();
                route.controller = clazz.getSimpleName().toLowerCase();
                route.action = method.getName().toLowerCase();
                RouteTable.add(route);
            }

            //PathMapping()
            //Routing(x,0);
            //clazz.getInterfaces()
            //System.out.println(clazz);
        }

    }
    ViewEngine viewEngine;

    class Route {

        Class<?> clazz;
        boolean isPOJO;
        String patten;
        Method call;
        String controller;
        String action;
        String setServiceMethod;
        int httpMethod;
        Map<Integer, String> paramsMapping = new HashMap<>();
        Class<?>[] filters;

        public Class<?>[] getActionFilters() {
            if (filters == null) {
                ArrayList<Class<?>> tmp = new ArrayList<>();
                FilterGroup group = call.getAnnotation(FilterGroup.class);
                if (group != null && group.value() != null && group.value().length > 0) {
                    for (Filter f : group.value()) {
                        if (!tmp.contains(f.value())) {
                            tmp.add(f.value());
                        }
                    }
                } else {
                    Filter[] tmps = call.getAnnotationsByType(Filter.class);
                    if (tmps != null) {
                        for (Filter f : tmps) {
                            if (!tmp.contains(f.value())) {
                                tmp.add(f.value());
                            }
                        }
                    }
                }
                filters = tmp.toArray(new Class<?>[0]);
            }
            return filters;
        }
    }

    Set<Route> RouteTable = new LinkedHashSet<>();
    WebApplication app;

    @Override
    public void init(WebApplication app, Properties params) {
        this.app = app;
        JarScanner js = new JarScanner();
        if (java.lang.MANO_WEB_MACRO.DEBUG) {
            app.getLogger().info("scanning controllers...");
        }
        for (URL url : app.getLoader().getURLs()) {
            try {
                js.scan(url);
            } catch (Throwable ex) {
                if (java.lang.MANO_WEB_MACRO.DEBUG) {
                    app.getLogger().debug("scanning jar:" + url, ex);
                }
            }
        }

        try {
            viewEngine = (ViewEngine) app.getLoader().newInstance(params.getProperty("view.engine"));
            viewEngine.setTempdir(Utility.combinePath(app.getApplicationPath(), "APP-DATA/tmp").toString());
            viewEngine.setViewdir(Utility.combinePath(app.getApplicationPath(), "views").toString());
        } catch (Throwable ex) {
            app.getLogger().error(ex);
        }
    }

    HashMap<Class<?>, ActionFilter> actionFilters = new HashMap<>();

    @Override
    public boolean handle(HttpContext context) {
        return this.handle(context, context.getRequest().url().getPath());
    }

    @Override
    public boolean handle(HttpContext context, String tryPath) {
        Pattern test = null;
        Matcher matcher;

        ActionContext rs = null;
        for (Route route : RouteTable) {//TODO: 测试未考虑效率
            try {
                test = Pattern.compile(route.patten, Pattern.CASE_INSENSITIVE);
            } catch (Throwable ex) {
                if (java.lang.MANO_WEB_MACRO.DEBUG) {
                    app.getLogger().debug("patten error:" + route.patten, ex);
                }
                continue;
            }
            matcher = test.matcher(tryPath);
            app.getLogger().debug("matching: patten:" + route.patten + " url:" + tryPath);
            if (matcher.matches()) {

                Object[] params = new Object[route.call.getParameterCount()];
                Class<?>[] types = route.call.getParameterTypes();
                try {
                    for (int i = 0; i < types.length; i++) {
                        params[i] = Utility.cast(types[i], matcher.group(route.paramsMapping.get(i)));
                    }
                } catch (Throwable ex) {
                    continue;
                }

                try {
                    rs = new ActionContext(context);
                    rs.setController(route.controller);
                    rs.setAction(route.action);

                    //初始化过滤程序
                    ActionFilter filter;
                    for (Class<?> clazz : route.getActionFilters()) {
                        if (actionFilters.containsKey(clazz)) {
                            filter = actionFilters.get(clazz);
                        } else {
                            filter = (ActionFilter) context.getApplication().getLoader().newInstance(clazz);
                            actionFilters.put(clazz, filter);
                        }
                        if (!filter.onActionExecuting(rs)) {
                            return true;
                        }
                    }

                    Object obj = context.getApplication().getLoader().newInstance(route.clazz);
                    Method m = Controller.class.getDeclaredMethod("setService", ActionContext.class);
                    m.setAccessible(true);
                    m.invoke(obj, rs);//

                    route.call.invoke(obj, params);

                    for (Class<?> clazz : route.getActionFilters()) {
                        if (actionFilters.containsKey(clazz)) {
                            filter = actionFilters.get(clazz);
                        } else {
                            filter = (ActionFilter) context.getApplication().getLoader().newInstance(clazz);
                            actionFilters.put(clazz, filter);
                        }
                        if (!filter.onActionExecuted(rs)) {
                            return true;
                        }
                    }

                    break;
                } catch (InvocationTargetException ex) {
                    if (ex.getTargetException() != null) {
                        context.getResponse().write(ex.getTargetException().getClass() + ":" + ex.getTargetException().getMessage());
                        app.getLogger().debug("call route handler", ex.getTargetException());
                    } else {
                        context.getResponse().write(ex.getClass() + ":" + ex.getMessage());
                        app.getLogger().debug("call route handler", ex);
                    }
                    return true;
                } catch (Throwable ex) {
                    context.getResponse().write(ex.getClass() + ":" + ex.getMessage());
                    app.getLogger().debug("call route handler", ex);
                    return true;
                }
            }

        }
        if (rs == null) {
            return false;
        }
        ActionResult result = rs.getResult();
        if (result == null) {
            return true;
        }
        if (result instanceof ViewResult) {
            ((ViewResult) result).init(viewEngine);
        }

        result.execute(rs);

        /*rs.setResult(new ViewResult().init(viewEngine));
         //rs.setResult(new ContentResult("你好世界，我是jun!"));
         //context.getResponse().write("hello");
         ActionResult result = rs.getResult();
         if (result != null) {
         result.execute(rs);
         //javax.tools.JavaCompiler compiler=javax.tools.ToolProvider.getSystemJavaCompiler();
         //http://blog.csdn.net/menxu_work/article/details/9187027

         }
         if (!context.isCompleted()) {
         context.getResponse().end();
         }*/
        return true;
    }

    @Override
    public void dispose() {
        viewEngine = null;
        app = null;
        RouteTable.clear();
    }

}

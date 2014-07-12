/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.http.HttpContext;
import mano.http.HttpModule;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class UrlRouteModule implements HttpModule {

    class JarScanner {

        private String[] jarFiles;

        public void scan() {
            URL url;
            try {
                url = new URL("jar:file:/E:/repositories/java/mano/mano.wrt/dist/mano.wrt.jar!/");
            } catch (MalformedURLException ex) {
                return;
            }
            JarFile jar;
            try {
                jar = ((JarURLConnection) url.openConnection()).getJarFile();
            } catch (IOException ex) {
                return;
            }
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            String name;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                name = entry.getName();
                if (!entry.isDirectory() && name.endsWith(".class") && name.indexOf("$") < 1) {//
                    try {
                        name = (name.substring(0, name.length() - 6)).replace('/', '.');
                        onFoundClass(Class.forName(name));
                    } catch (ClassNotFoundException ex) {
                        //ignored
                    }
                }
            }
        }

        public void onFoundClass(Class<?> clazz) {
            //clazz.getInterfaces()
            System.out.println(clazz);
        }

    }

    @Override
    public void init(Map<String, String> params) {
        //controllers scanning
        //UrlRouteModule.class.getPackage().
        JarScanner js = new JarScanner();
        js.scan();
    }

    @Override
    public boolean handle(HttpContext context) {
        return this.handle(context, context.getRequest().url().getPath());
    }

    @Override
    public boolean handle(HttpContext context, String tryPath) {
        RouteService rs = new RouteService(context);
        
        rs.set("title", "hello");
        
        rs.setResult(new ViewResult());
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
        }
        return true;
    }

    @Override
    public void dispose() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;
import mano.Activator;
import mano.ContextClassLoader;
import mano.http.HttpModuleSettings;
import mano.http.HttpServer;
import mano.service.Service;
import mano.service.ServiceProvider;
import mano.util.NameValueCollection;
import mano.util.Utility;
import mano.util.logging.LogProvider;
import mano.util.logging.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplicationStartupInfo {

    public Service service;
    public ContextClassLoader serviceLoader;
    public NameValueCollection<HttpModuleSettings> modules = new NameValueCollection<>();
    public String name;
    public String host;
    public String rootdir;
    public String type;
    public String path;
    public ArrayList<String> documents = new ArrayList<>();
    public ArrayList<String> dependency = new ArrayList<>();
    public ArrayList<String> dependencyExt = new ArrayList<>();
    public NameValueCollection<String> exports = new NameValueCollection<>();
    public NameValueCollection<String> settings = new NameValueCollection<>();
    public ArrayList<String> ignoreds = new ArrayList<>();
    public String serverPath;
    public WebApplication app;
    private HttpServer server;
    public String version = "ManoServer/1.1";
    private Pattern hostreg;
    public boolean disabledEntityBody;
    public long maxEntityBodySize;
    public long maxPostFileSize;
    public String controller;
    public String action;

    public boolean matchHost(String hostname) {
        if (hostreg == null) {
            hostreg = Pattern.compile("^" + host.replace("*", "[\\w\\-_\\.]+") + "$");
        }
        return hostreg.matcher(hostname).matches();
    }

    public synchronized WebApplication getInstance() {
        if (app != null) {
            return app;
        }
        if (service == null) {
            return null;
        }
        try {
            ArrayList<String> files = new ArrayList<>();
            files.add(getServerInstance().mapPath("WEB-INF/lib"));
            files.add(getServerInstance().mapPath("WEB-INF/classes"));
            for (String file : dependencyExt) {

                if (file == null || "".equals(file)) {
                    continue;
                }

                if (file.startsWith("~/") || file.startsWith("~\\")) {
                    files.add(Utility.combinePath(serverPath, file.substring(1)).toString());
                } else {
                    files.add(file);
                }
            }

            for (String file : dependency) {

                if (file == null || "".equals(file)) {
                    continue;
                }

                if (file.startsWith("~/") || file.startsWith("~\\")) {
                    files.add(Utility.combinePath(serverPath, file.substring(1)).toString());
                } else {
                    files.add(file);
                }
            }

            ContextClassLoader loader = new ContextClassLoader(((ServiceProvider) this.service).getService(Logger.class), new URL[0], serviceLoader);

            loader.register(files.toArray(new String[0]));
            this.exports.entrySet().iterator().forEachRemaining(item -> {
                try {
                    loader.registerExport(item.getKey(), item.getValue());
                } catch (ClassNotFoundException ex) {
                    loader.getLogger().warn(null, ex);
                }
            });
            //TODO: 重写日志组件

            /*try {
             loader.register(getServerInstance().mapPath("bin"));
             } catch (FileNotFoundException ex) {
             //ignored
             }
             try {
             loader.register(getServerInstance().mapPath("bin/lib"));
             } catch (FileNotFoundException ex) {
             //ignored
             }*/
            app = (WebApplication) loader.newInstance(this.type);
            if (app != null) {
                Method init = WebApplication.class.getDeclaredMethod("init", WebApplicationStartupInfo.class, ContextClassLoader.class);
                init.setAccessible(true);
                init.invoke(app, this, loader);
                return app;
            }
        } catch (InvocationTargetException ex) {
            Logger.getDefault().error("WebApplicationStartupInfo.getInstance", ex.getTargetException() == null ? ex : ex.getTargetException());
        } catch (Exception ex) {
            Logger.getDefault().error("WebApplicationStartupInfo.getInstance", ex);
        }
        return null;
    }

    public synchronized HttpServer getServerInstance() {
        if (server == null && service != null) {

            String _basedir = this.rootdir;

            if (_basedir.startsWith("~/") || _basedir.startsWith("~\\")) {
                _basedir = Utility.combinePath(this.serverPath, _basedir.substring(1)).toString();
            }

            final String realbasedir = _basedir;
            final String virtualPath = this.path;
            server = new HttpServer() {

                @Override
                public String getBaseDirectory() {
                    return realbasedir;
                }

                @Override
                public String getVirtualPath() {
                    return virtualPath;
                }

                @Override
                public String mapPath(String... vpaths) {
                    return Paths.get(realbasedir, vpaths).toString();
                }

                @Override
                public String getVersion() {
                    return version;
                }

            };
        }
        return server;
    }

}

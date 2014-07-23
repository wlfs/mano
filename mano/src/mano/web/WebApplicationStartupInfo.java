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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;
import mano.Activator;
import mano.Service;
import mano.ServiceProvider;
import mano.http.HttpModuleSettings;
import mano.http.HttpServer;
import mano.util.NameValueCollection;
import mano.util.Utility;
import mano.util.logging.ILogger;
import mano.util.logging.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplicationStartupInfo {

    public Service service;
    public NameValueCollection<HttpModuleSettings> modules;
    public String name;
    public String host;

    public String type;
    public String path;
    public ArrayList<String> documents = new ArrayList<>();
    public ArrayList<String> dependency = new ArrayList<>();
    public NameValueCollection<String> settings;
    public String serverPath;
    private WebApplication app;
    private HttpServer server;
    public String version = "ManoServer/1.1";
    private Pattern hostreg;

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

            files.add(getServerInstance().mapPath("bin"));
            files.add(getServerInstance().mapPath("bin/lib"));
            files.add(getServerInstance().mapPath("bin/ext"));

            for (String file : dependency) {

                if (file == null || "".equals(file)) {
                    continue;
                }

                if (file.startsWith("~/")) {
                    files.add(Utility.combinePath(serverPath, file.substring(1)).toString());
                } else if (file.startsWith("/")) {
                    files.add(getServerInstance().mapPath(file));
                } else {
                    files.add(file);
                }
            }

            Activator loader = new Activator(((ServiceProvider) service).getService(Activator.class));
            loader.loadAll(files.toArray(new String[0]));
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
                Method init = WebApplication.class.getDeclaredMethod("init", WebApplicationStartupInfo.class, Activator.class);
                init.setAccessible(true);
                init.invoke(app, this, loader);
                return app;
            }
        } catch (InvocationTargetException ex) {
            Logger.error("WebApplicationStartupInfo.getInstance", ex.getTargetException() == null ? ex : ex.getTargetException());
        } catch (Exception ex) {
            Logger.error("WebApplicationStartupInfo.getInstance", ex);
        }
        return null;
    }

    public synchronized HttpServer getServerInstance() {
        if (server == null && service != null) {

            String _basedir = this.path;

            if (_basedir.startsWith("./") || _basedir.startsWith(".\\")) {
                _basedir = Utility.combinePath(this.serverPath, _basedir.substring(1)).toString();
            } else if (_basedir.startsWith("/") || _basedir.startsWith("\\")) {
                _basedir = Utility.combinePath(this.serverPath, _basedir).toString();
            }

            final String basedir = _basedir;
            final String virtualPath = "/";
            server = new HttpServer() {

                @Override
                public String getBaseDirectory() {
                    return basedir;
                }

                @Override
                public String getVirtualPath() {
                    return virtualPath;
                }

                @Override
                public String mapPath(String vpath) {
                    return Paths.get(basedir, virtualPath, vpath).toString();
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

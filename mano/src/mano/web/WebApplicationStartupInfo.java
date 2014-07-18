/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import mano.Activator;
import mano.Service;
import mano.http.HttpModuleSettings;
import mano.http.HttpServer;
import mano.util.NameValueCollection;
import mano.util.Utility;

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
    public ArrayList<String> documents;
    public NameValueCollection<String> settings;
    public String serverPath;
    private WebApplication app;
    private HttpServer server;
    public String version = "ManoServer/1.1";

    public synchronized WebApplication getInstance() {
        if (app != null) {
            return app;
        }
        if (service == null) {
            return null;
        }
        try {

            Activator loader = new Activator(service.getLoader());
            try {
                loader.register(getServerInstance().mapPath("bin"));
            } catch (FileNotFoundException ex) {
                //ignored
            }
            try {
                loader.register(getServerInstance().mapPath("bin/lib"));
            } catch (FileNotFoundException ex) {
                //ignored
            }

            app = (WebApplication) service.getLoader().newInstance(this.type);
            if (app != null) {
                Method init = WebApplication.class.getDeclaredMethod("init", WebApplicationStartupInfo.class,Activator.class);
                init.setAccessible(true);
                init.invoke(app, this,loader);
                return app;
            }
        } catch (Exception ex) {
            service.getLogger().error("WebApplicationStartupInfo.getInstance", ex);
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

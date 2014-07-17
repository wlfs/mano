/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.Service;
import mano.util.NameValueCollection;
import mano.http.HttpModuleSettings;
import java.util.ArrayList;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplicationStartupInfo{
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
        public synchronized WebApplication getInstance(){
            if(app!=null){
                return app;
            }
            if(service==null){
                return null;
            }
            try {
                WebApplication app= (WebApplication)service.getLoader().newInstance(this.type);
                if(app!=null){
                    app.init(this);
                    return app;
                }
            } catch (Exception ex) {
                service.getLogger().error("", ex);
            }
            return null;
        }
        
    }

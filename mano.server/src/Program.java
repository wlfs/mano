/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

import java.io.FileNotFoundException;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import mano.Activator;
import mano.Service;
import mano.ServiceContainer;
import mano.ServiceProvider;
import mano.logging.Log4jLogger;
import mano.net.Task;
import mano.otpl.python.PyViewEngine;
import mano.util.CachedObjectFactory;
import mano.util.Logger;
import mano.util.NameValueCollection;
import mano.util.ObjectFactory;
import mano.util.ThreadPool;
import mano.util.Utility;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import org.python.core.*;
import org.python.util.PythonInterpreter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author jun
 */
public class Program implements ServiceContainer, ServiceProvider {

    @Override
    public Service getService(String serviceName) {
        if (serviceName != null && services.containsKey(serviceName)) {
            return services.get(serviceName);
        }
        return null;
    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        if (serviceType == null) {
            return null;
        }
        if (Logger.class.getName().equals(serviceType.getName())) {
            return (T) logger;
        } else if (Activator.class.getName().equals(serviceType.getName())) {
            return (T) loader;
        }
        return null;
    }

    public static class test implements Runnable {

        Program pp;
        UUID id;

        public test(Program p) {
            pp = p;
            id = UUID.randomUUID();
        }
        public int num = 0;
        public String kk = "";
        /*@Override
         protected void finalize(){
         System.out.println("finalize "+id);
         //pp.factory.put(this);
            
         }*/
        int x = 0;

        public void call() {
            System.out.println("this is call result");
        }

        @Override
        public void run() {
            System.out.println("run:" + (x > 0 ? "++++++++++++++" : ""));
            x++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
            }
            ThreadPool.execute(pp.factory.get());
            System.gc();
        }
    }

    Logger logger;
    Activator loader;
    NameValueCollection<Service> services;
    CachedObjectFactory<test> factory;
    String bootstrapPath;

    private void init() throws FileNotFoundException {
        logger = new Log4jLogger();
        loader = new Activator();
        bootstrapPath = System.getProperty("user.dir");
        loader.register(Utility.combinePath(bootstrapPath, "dist").toString());
        loader.register(Utility.combinePath(bootstrapPath, "dist/lib").toString());

        /*Program me=this;
         factory = new CachedObjectFactory<>(new ObjectFactory<test>(){

         @Override
         public test create() {
         return new test(me);
         }
            
         });
         ThreadPool.execute(factory.get());
         ThreadPool.execute(factory.get());
         */
    }

    void configServices() throws XmlException, InstantiationException, ClassNotFoundException {
        services = new NameValueCollection<>();
        String configPath = Utility.combinePath(bootstrapPath, "server/config.xml").toString();
        XmlHelper helper = XmlHelper.load(configPath);
        NodeList nodes = helper.selectNodes("/configuration/services/service");
        NameValueCollection<String> params;
        for (int i = 0; i < nodes.getLength(); i++) {
            NamedNodeMap attrs = nodes.item(i).getAttributes();

            String name = attrs.getNamedItem("name").getNodeValue();
            String type = attrs.getNamedItem("type").getNodeValue();

            
            params = new NameValueCollection<>();
            params.put("path:bootstrap", this.bootstrapPath);
            params.put("path:config", configPath);
            params.put("service:name", name);
            NodeList conns = helper.selectNodes(nodes.item(i), "params/param");
            
            for (int j = 0; j < conns.getLength(); j++) {
                attrs = conns.item(j).getAttributes();
                params.put(attrs.getNamedItem("name").getNodeValue(), conns.item(j).getTextContent());
            }
            Service service = (Service) loader.newInstance(type);
            service.init(this, params);//try 
            services.put(name, service);
        }
    }

    private void loop() {
        while (true) {
            try {
                Thread.sleep(1000 * 1000);
            } catch (InterruptedException e) {
                this.logger.error("", e);
            }
        }
    }

    /**
     * @param args the command line arguments:
     * <p>
     *
     * </p>
     */
    public static void main(String[] args) {

        Program server = new Program();

        try {
            server.init();

            server.configServices();

            server.services.values().stream().forEach((service) -> {
                ThreadPool.execute(service);
            });

            server.loop();

        } catch (Exception ex) {
            server.logger.error("", ex);
        }
    }

}

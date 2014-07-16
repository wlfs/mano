/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import mano.Activator;
import mano.Service;
import mano.logging.Log4jLogger;
import mano.net.Task;
import mano.otpl.python.PyViewEngine;
import mano.util.CachedObjectFactory;
import mano.util.Logger;
import mano.util.NameValueCollection;
import mano.util.ObjectFactory;
import mano.util.ThreadPool;
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
public class Program {

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

    void init() {
        logger = new Log4jLogger();
        loader = new Activator();
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
        String configPath = this.bootstrapPath + "\\config.xml";
        XmlHelper helper = XmlHelper.load(configPath);
        NodeList nodes = helper.selectNodes("/configuration/services/service");

        for (int i = 0; i < nodes.getLength(); i++) {
            NamedNodeMap attrs = nodes.item(i).getAttributes();

            String name = attrs.getNamedItem("name").getNodeValue();
            String type = attrs.getNamedItem("type").getNodeValue();

            Service service = (Service) loader.newInstance(type);
            service.param("path:bootstrap", this.bootstrapPath);
            service.param("path:config", configPath);
            NodeList conns = helper.selectNodes(nodes.item(i), "params/param");
            for (int j = 0; j < conns.getLength(); j++) {
                attrs = conns.item(j).getAttributes();
                service.param(attrs.getNamedItem("name").getNodeValue(), conns.item(j).getTextContent());
            }

            service.init(name, loader, logger);
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

    public static class TplObject {

        public static final long TYPE_UNKONW = 0;
        public static final long TYPE_NULL = 1;
        public static final long TYPE_SHORT = 2;
        public static final long TYPE_INTEGER = 4;
        public static final long TYPE_LONG = 8;
        public static final long TYPE_FLOAT = 16;
        public static final long TYPE_DOUBLE = 32;
        public static final long TYPE_BOOL = 64;
        public static final long TYPE_STRING = 128;
        public static final long TYPE_ENUM = 256;
        public static final long TYPE_DATETIME = 512;
        public static final long TYPE_SET = 1024;
        public static final long TYPE_MAP = 2048;
        public static final long TYPE_INSTANCE = 4096;
        public static final long TYPE_CALLABLE = 4096;

        public TplObject(Object value) {
            Class<?> clazz = value.getClass();
            clazz.isPrimitive();
        }

        public void set(Object value) {

        }

        public Object get() {
            return null;
        }

        public boolean eq(Object value) {
            return true;
        }

        public String getType() {
            return null;
        }

        public static TplObject parse(String expr, Object domain) {
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /*Stack<Object> stack = new Stack<>();
        String[] cmds = new String[4];
        cmds[0] = "ldnum 15";
        cmds[1] = "ldnum 5";
        cmds[2] = "plus";
        cmds[3] = "puts";//1 lodstr,30,vvvvvvv

        for (String cmd : cmds) {
            String[] arr = cmd.split(" ");
            switch (arr[0]) {
                case "ldnum":
                    stack.push(arr[1]);
                    break;
                case "plus":
                    Object a = stack.pop();
                    Object b = stack.pop();
                    stack.push((Integer.parseInt(a.toString())) + (Integer.parseInt(b.toString())));
                    break;
                case "puts":
                    System.out.println(stack.pop());
                    break;
            }
        }*/

        /*PythonInterpreter pi = new PythonInterpreter();

         pi.setOut(System.out);
        
         Program.test t=new Program.test(null);
         t.num=500;
         t.kk="world";
         pi.set("test", t);
        
         pi.exec("print test.call()");
         
        if (true) {
            return;
        }*/
        //PyViewEngine pp = new PyViewEngine();
        Program server = new Program();
        server.bootstrapPath = "E:\\repositories\\java\\mano\\mano.server\\server";
        server.init();
        try {
            server.loader.register("E:\\repositories\\java\\mano\\mano\\dist");
            server.loader.register("E:\\repositories\\java\\mano\\mano.wrt\\dist");
            server.loader.register("E:\\repositories\\java\\mano\\mano.otpl\\dist");
            server.configServices();

            server.services.values().stream().forEach((svc) -> {
                new Thread(svc).start();
            });

            server.loop();

        } catch (Exception ex) {
            server.logger.error("", ex);
        }
    }

}

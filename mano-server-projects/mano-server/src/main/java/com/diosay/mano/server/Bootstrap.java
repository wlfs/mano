package com.diosay.mano.server;

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
import java.io.FileNotFoundException;
import java.util.Map;
import mano.ContextClassLoader;
import mano.service.Service;
import mano.service.ServiceContainer;
import mano.service.ServiceProvider;
import mano.util.NameValueCollection;
import mano.util.ThreadPool;
import mano.util.Utility;
import mano.util.logging.CansoleLogProvider;
import mano.util.logging.LogProvider;
import mano.util.logging.Logger;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jun
 */
public class Bootstrap implements ServiceContainer, ServiceProvider {

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
        } else if (ContextClassLoader.class.getName().equals(serviceType.getName()) || ClassLoader.class.getName().equals(serviceType.getName())) {
            return (T) loader;
        }
        return null;
    }

    Logger logger;
    ContextClassLoader loader;
    NameValueCollection<Service> services;
    String bootstrapPath;

    private void init() throws FileNotFoundException {
        bootstrapPath = Utility.combinePath(System.getProperty("user.dir")).getParent().toString();
        loader = new ContextClassLoader(new Logger(new CansoleLogProvider()));
        loader.register(Utility.combinePath(bootstrapPath, "lib").toString());
        
        
    }

//    private void loadParams(XmlHelper helper, Node node, Map<String, String> result) throws XmlException {
//        NodeList nodes = helper.selectNodes(node, "params/param");
//        NamedNodeMap attrs;
//        if (nodes.getLength() <= 0) {
//            return;
//        }
//        for (int i = 0; i < nodes.getLength(); i++) {
//            attrs = nodes.item(i).getAttributes();
//            
//        }
//    }
    /**
     * 载入配置文件
     *
     * @throws XmlException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private void loadConfig() throws XmlException, InstantiationException, ClassNotFoundException {
        String configPath = Utility.combinePath(bootstrapPath, "conf/server.xml").toString();
        XmlHelper helper = XmlHelper.load(configPath);
        NamedNodeMap attrs;
        NodeList nodes;
        Node node, root;
        String s;
        NameValueCollection<String> params = new NameValueCollection<>();
        root = helper.selectNode("/configuration/server");
        if (root == null) {
            return;
        }

        //服务依赖
        nodes = helper.selectNodes(root, "dependency/path");
        //ArrayList<String> list=new ArrayList();
        String[] arr;
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();

                try {
                    s = attrs.getNamedItem("value").getNodeValue().trim();
                    if (s.startsWith("~/") || s.startsWith("~\\")) {
                        s = Utility.combinePath(bootstrapPath, s.substring(2)).toString();
                    } else if (s.startsWith("/") || s.startsWith("\\")) {
                        s = Utility.combinePath(bootstrapPath, s.substring(1)).toString();
                    }
                    loader.register(s);
                } catch (Exception ex) {
                    Logger.getDefault().warn(null, ex);
                }
            }
        }

        //获取依赖导出
        nodes = helper.selectNodes(root, "dependency/export");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();
                try {
                    loader.registerExport(attrs.getNamedItem("name").getNodeValue().trim(), attrs.getNamedItem("class").getNodeValue().trim());
                } catch (Exception ex) {
                    Logger.getDefault().warn(null, ex);
                }
            }
        }

        //重置日志记录器
        node = helper.selectNode(root, "logger");
        if (node != null) {
            LogProvider provider = null;
            attrs = node.getAttributes();
            try {
                s = attrs.getNamedItem("provider").getNodeValue().trim();
                arr = Utility.split(s, ":", true);
                if (arr[0].equals("class")) {
                    provider = (LogProvider) loader.newInstance(arr[1]);
                }
            } catch (Exception ignored) {
            }

            if (provider != null) {
                params.clear();
                nodes = helper.selectNodes(node, "params/param");
                for (int i = 0; i < nodes.getLength(); i++) {
                    attrs = nodes.item(i).getAttributes();
                    try {
                        params.put(attrs.getNamedItem("name").getNodeValue().trim(), nodes.item(i).getTextContent().trim());
                    } catch (Exception ignored) {
                    }
                }
                provider.init(params);
                loader.setLogger(new Logger(provider));
            }
        }

        //实例化服务
        services = new NameValueCollection<>();
        NodeList nodes2;
        nodes = helper.selectNodes(root, "services/service");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();

                String name = attrs.getNamedItem("name").getNodeValue();
                String type = attrs.getNamedItem("class").getNodeValue();

                params.clear();
                params.put("path:bootstrap", this.bootstrapPath);
                params.put("path:config", configPath);
                params.put("service:name", name);
                nodes2 = helper.selectNodes(nodes.item(i), "params/param");
                if (nodes2 != null) {
                    for (int j = 0; j < nodes2.getLength(); j++) {
                        attrs = nodes2.item(j).getAttributes();
                        try {
                            params.put(attrs.getNamedItem("name").getNodeValue().trim(), nodes2.item(j).getTextContent().trim());
                        } catch (Exception ignored) {
                        }
                    }
                }

                try {
                    Service service = (Service) loader.newInstance(type);
                    service.init(this, params);
                    services.put(name, service);
                } catch (Exception ignored) {
                    loader.getLogger().error(null, ignored);
                }
            }
        }
    }

    private void loop() {
        while (true) {
            try {
                Thread.sleep(1000 * 1000);
            } catch (InterruptedException e) {
                Logger.getDefault().warn("", e);
            }
        }
    }
    
    public void start(String...args){
        Logger.getDefault().info("Starting server.");
        try {
            init();

            loadConfig();

            if (services.isEmpty()) {
                Logger.getDefault().fatal("No service running.");
                System.exit(0);
            } else {
                services.values().stream().forEach((service) -> {
                    ThreadPool.execute(service);
                });
                Logger.getDefault().info("server is started.");
                loop();
            }

        } catch (Exception ex) {
            Logger.getDefault().fatal("", ex);
            System.exit(0);
        }
    }
    
    public void stop(){
        Logger.getDefault().info("server has stopped.");
        System.exit(0);
    }

    public static void main(String[] args) {
        Bootstrap server = new Bootstrap();
        server.start(args);
    }

}

package com.diosay.mano.server;

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import mano.ContextClassLoader;
import mano.Mano;
import mano.service.Intent;
import mano.service.Service;
import mano.service.ServiceManager;
import mano.service.ServiceProvider;
import mano.util.NameValueCollection;
import mano.util.ThreadPool;
import mano.util.Utility;
import mano.util.logging.LogProvider;
import mano.util.logging.LogService;
import mano.util.logging.Logger;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 实现 Mano 服务容器的启动程序。
 *
 * @author jun
 */
public class Bootstrap extends ContextClassLoader implements ServiceProvider {

    @java.lang.Deprecated
    Logger logger;
    @java.lang.Deprecated
    ContextClassLoader loader;

    NameValueCollection<Service> services;
    @java.lang.Deprecated
    String bootstrapPath;

    public Bootstrap() {
        super(Logger.getLog());
        ServiceManager.getInstance().setLoader(this);
        ServiceManager.getInstance().regisiter(new LogService());
    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        if (serviceType == null) {
            return null;
        }
        if (Logger.class.getName().equals(serviceType.getName())) {
            return (T) this.getLogger();
        } else if (ContextClassLoader.class.getName().equals(serviceType.getName()) || ClassLoader.class.getName().equals(serviceType.getName())) {
            return (T) this;
        }
        return null;
    }

    /**
     * 打印错误
     *
     * @param message
     * @param ex
     */
    private void error(String message, Throwable ex) {
        System.out.println(message);
        if (ex != null) {
            ex.printStackTrace(System.out);
        }
    }

    @java.lang.Deprecated
    private void init() throws FileNotFoundException {
        bootstrapPath = Utility.combinePath(System.getProperty("user.dir")).getParent().toString();
        loader = this;
        loader.register(Utility.combinePath(bootstrapPath, "lib").toString());
    }

    /**
     * 载入配置文件
     *
     * @throws XmlException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private void configure(String configPath, String serverDir) throws Exception {
        Mano.setProperty("user.dir", System.getProperty("user.dir"));

        File cfile;
        if (configPath == null) {
            cfile = Utility.combinePath(Utility.combinePath(Mano.getProperty("user.dir")).getParent().toString(), "conf/server.xml").toFile();
        } else {
            cfile = new File(configPath);
        }

        if (!cfile.exists() || !cfile.isFile()) {
            throw new FileNotFoundException("Configuration file not found.");
        }

        XmlHelper helper = XmlHelper.load(cfile.toString());

        NamedNodeMap attrs;
        NodeList nodes;
        Node node, root;
        String s;
        NameValueCollection<String> params = new NameValueCollection<>();
        root = helper.selectNode("/configuration/server");
        if (root == null) {
            throw new NoSuchElementException("Miss [server] Node.");
        }
        Mano.getProperties().setProperty("server.config", cfile.toString());

        //加载属性
        nodes = helper.selectNodes(root, "settings/property");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();
                Mano.getProperties().setProperty(attrs.getNamedItem("name").getNodeValue(), nodes.item(i).getTextContent());
            }
        }

        //预处理配置值。
        if (serverDir != null) {
            Mano.setProperty("server.dir", serverDir);
        } else {
            if (Mano.getProperties().containsKey("server.dir")) {
                Mano.setProperty("server.dir", Utility.getAndReplaceMarkup("server.dir", Mano.getProperties(), System.getProperties()));
            } else {
                Mano.setProperty("server.dir", Utility.combinePath(Mano.getProperty("user.dir")).getParent().toString());
            }
        }

        //加载依赖
        register(Utility.combinePath(Mano.getProperty("server.dir"), "lib").toString());

        nodes = helper.selectNodes(root, "dependency/path");
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
                    this.register(s);
                } catch (Throwable ex) {
                    error(null, ex);
                }
            }
        }

        //导出依赖
        nodes = helper.selectNodes(root, "dependency/export");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();
                try {
                    this.registerExport(attrs.getNamedItem("name").getNodeValue().trim(), attrs.getNamedItem("class").getNodeValue().trim());
                } catch (Throwable ex) {
                    error(null, ex);
                }
            }
        }

        //重置日志记录器
        node = helper.selectNode(root, "logger");
        if (node != null) {

            attrs = node.getAttributes();
            try {
                s = attrs.getNamedItem("name").getNodeValue().trim();
                if (s == null || "".equals(s.trim())) {
                    throw new NoSuchElementException("Miss Logger [name] Attribute.");
                } else {
                    this.setLogger(Logger.getLog());
                }

                nodes = helper.selectNodes(node, "handler");
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        try {
                            s = nodes.item(i).getAttributes().getNamedItem("class").getNodeValue();
                            Intent bag = Intent.create("mano.service.logging", "addhandler");
                            bag.set("handler", this.newInstance(s));
                            bag.submit();
                        } catch (Throwable ex) {
                            this.error(null, ex);
                        }
                    }
                }

            } catch (Exception ignored) {
            }
        }

        //加载服务
        services = new NameValueCollection<>();
        NodeList nodes2;
        nodes = helper.selectNodes(root, "services/service");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();

                String name = attrs.getNamedItem("name").getNodeValue();
                String type = attrs.getNamedItem("class").getNodeValue();
                try {
                    Service service = (Service) this.newInstance(type);
                    service.getProperties().setProperty("service_name", name);
                    nodes2 = helper.selectNodes(nodes.item(i), "property");
                    if (nodes2 != null) {
                        for (int j = 0; j < nodes2.getLength(); j++) {
                            attrs = nodes2.item(j).getAttributes();
                            try {
                                service.getProperties().setProperty(attrs.getNamedItem("name").getNodeValue().trim(), nodes2.item(j).getTextContent().trim());
                            } catch (Exception ignored) {
                                this.error(null, ignored);
                            }
                        }
                    }
                    services.put(name, service);
                } catch (Exception ignored) {
                    this.error(null, ignored);
                }
            }
        }
    }

    private void loop() {
        while (true) {
            try {
                Thread.sleep(1000 * 1000);
            } catch (InterruptedException e) {
                this.error(null, e);
                break;
            }
        }
    }

    public void start(String configFile, String serverDir) {

        this.getLogger().info("Starting server.");
        try {

            configure(configFile, serverDir);

            if (services.isEmpty()) {
                this.getLogger().fatal("No service running.");
                System.exit(0);
            } else {
                services.values().stream().forEach((service) -> {
                    ThreadPool.execute(service);
                });
                this.getLogger().info("server is started.");

            }

        } catch (Exception ex) {
            this.getLogger().fatal(ex);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex1) {
            }
            System.exit(0);
        }
    }

    public void stop() {
        this.getLogger().info("server has stopped.");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex1) {
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        Bootstrap server = new Bootstrap();
//        Mano.setProperty("manoserver.testing.test_webapp.config_file", "E:\\repositories\\java\\mano\\test-webapp-projects\\test-webapp\\src\\main\\webapp");
//        Mano.setProperty("manoserver.testing.test_webapp.ext_dependency", "E:\\repositories\\java\\mano\\test-webapp-projects\\test-webapp\\target\\build\\lib");
//        server.start("E:\\repositories\\java\\mano\\mano-server-projects\\mano-server\\src\\resources\\conf\\server.xml", "E:\\repositories\\java\\mano\\mano-server-projects\\mano-server\\target\\build");
        server.start(null, null);
        server.loop();
    }

}

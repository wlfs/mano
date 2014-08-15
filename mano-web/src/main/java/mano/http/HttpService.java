/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import mano.ContextClassLoader;
import mano.caching.CacheProvider;
import mano.http.HttpConnection.FlushHandler;
import mano.http.HttpConnection.ReceivedCompletionHandler;
import mano.http.HttpConnection.ResolveRequestHeadersHandler;
import mano.http.HttpConnection.ResolveRequestLineHandler;
import mano.http.HttpConnection.SentCompletionHandler;
import mano.http.HttpRequestImpl.LoadExactDataHandler;
import mano.io.BufferPool;
import mano.io.ByteBufferPool;
import mano.service.Service;
import mano.service.ServiceContainer;
import mano.service.ServiceProvider;
import mano.util.CachedObjectPool;
import mano.util.NameValueCollection;
import mano.util.Pool;
import mano.util.Utility;
import mano.util.logging.CansoleLogProvider;
import mano.util.logging.Logger;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import mano.web.HttpSession;
import mano.web.WebApplication;
import mano.web.WebApplicationStartupInfo;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * HTTP 服务的标准实现。
 *
 * @author jun
 */
public class HttpService extends Service implements ServiceProvider {

    private ArrayList<String> documents;
    private NameValueCollection<HttpModuleSettings> modules;
    private NameValueCollection<WebApplicationStartupInfo> appInfos;
    private NameValueCollection<ConnectionInfo> infos = new NameValueCollection<>();
    private BufferPool _workBufferPool; //工作缓冲区池,4k
    private BufferPool _tempBufferPool; //临时缓冲区池,64k
    private ByteBufferPool _ioBufferPool; //io缓冲区池,512b
    //private Pool<HttpTask> _factory;
    private String bootstrapPath;
    private String configPath;
    private ContextClassLoader loader;
    private Logger logger = new Logger(new CansoleLogProvider());
    private String name;
    Pool<ReceivedCompletionHandler> receivedCompletionHandlerPool;
    Pool<SentCompletionHandler> sentCompletionHandlerPool;
    Pool<ResolveRequestLineHandler> resolveRequestLineHandlerPool;
    Pool<ResolveRequestHeadersHandler> resolveRequestHeadersHandlerHandlerPool;
    Pool<HttpConnection> connectionPool;
    Pool<FlushHandler> flushHandlerPool;
    Pool<LoadExactDataHandler> loadExactDataHandlerPool;

    public HttpService() {
        //_factory = new Pool<>(() -> new HttpTask(this));
        receivedCompletionHandlerPool = new CachedObjectPool<>(() -> {
            return new ReceivedCompletionHandler();
        }, 4, 128);
        sentCompletionHandlerPool = new CachedObjectPool<>(() -> {
            return new SentCompletionHandler();
        }, 4, 128);
        resolveRequestLineHandlerPool = new CachedObjectPool<>(() -> {
            return new ResolveRequestLineHandler();
        }, 4, 128);
        resolveRequestHeadersHandlerHandlerPool = new CachedObjectPool<>(() -> {
            return new ResolveRequestHeadersHandler();
        }, 4, 128);
        connectionPool = new CachedObjectPool<>(() -> {
            return new HttpConnection();
        }, 4, 128);
        flushHandlerPool = new CachedObjectPool<>(() -> {
            return new FlushHandler();
        }, 4, 128);
        loadExactDataHandlerPool = new CachedObjectPool<>(() -> {
            return new LoadExactDataHandler();
        }, 4, 128);
    }

    BufferPool workBufferPool() {
        return _workBufferPool;
    }

    BufferPool tempBufferPool() {
        return _tempBufferPool;
    }

    ByteBufferPool ioBufferPool() {
        return _ioBufferPool;
    }

    /*Task newTask() {
     return _factory.get();
     }*/
    public Logger getLogger() {
        return this.logger;
    }

    public ContextClassLoader getLoader() {
        return this.loader;
    }

    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(ServiceContainer container, Map<String, String> params) {
        super.init(container, null);
        ServiceProvider provider = (ServiceProvider) container;

        loader = provider.getService(ContextClassLoader.class);
        logger = loader.getLogger();

        if (params != null) {
            params.entrySet().stream().forEach(item -> {
                parseParam(item.getKey(), item.getValue());
            });
        }

        try {
            this.loadConfigs();
        } catch (XmlException ex) {
            logger.error("", ex);
        }
    }

    private void parseParam(String name, Object value) {
        String[] arr = Utility.split(name, ":", true);
        if (arr.length > 2) {
            if ("connection".equalsIgnoreCase(arr[0])) {
                ConnectionInfo info;
                if (infos.containsKey(arr[1])) {
                    info = infos.get(arr[1]);
                } else {
                    info = new ConnectionInfo();
                    infos.put(arr[1], info);
                }

                if ("address".equalsIgnoreCase(arr[2])) {
                    String addr = value == null ? "" : value.toString().trim();
                    int index = addr.lastIndexOf(":");
                    if (index < 0) {
                        return;
                    }
                    info.address = new InetSocketAddress(addr.substring(0, index), Integer.parseInt(addr.substring(index + 1)));
                } else if ("disabled".equalsIgnoreCase(arr[2])) {
                    info.disabled = "true".equalsIgnoreCase(value == null ? "" : value.toString().trim());
                }
            }
        } else if (arr.length == 2) {
            if ("path".equalsIgnoreCase(arr[0])) {
                if ("bootstrap".equalsIgnoreCase(arr[1])) {
                    this.bootstrapPath = value.toString();
                } else if ("config".equalsIgnoreCase(arr[1])) {
                    this.configPath = value.toString();
                }
            } else if ("service".equalsIgnoreCase(arr[0])) {
                if ("name".equalsIgnoreCase(arr[1])) {
                    this.name = value.toString();
                }
            }
        }
    }

    private long parseSize(String size) {
        long result;
        if (size.endsWith("M") || size.endsWith("m")) {
            result = Long.parseUnsignedLong(size.substring(0, size.length() - 1)) * (1024 * 1024);
        } else if (size.endsWith("K") || size.endsWith("k")) {
            result = Long.parseUnsignedLong(size.substring(0, size.length() - 1)) * 1024;
        } else if (size.endsWith("B") || size.endsWith("b")) {
            result = Long.parseUnsignedLong(size.substring(0, size.length() - 1));
        } else {
            result = Long.parseUnsignedLong(size);
        }
        return result;
    }
    int maxConnections;
    WebApplicationStartupInfo machine;

    private void loadConfigs() throws XmlException {

        XmlHelper helper = XmlHelper.load(this.configPath);
        Node node, attr, root = helper.selectNode("/configuration/http.service");
        String s;
        NamedNodeMap attrs = root.getAttributes();

        int bufferBucketSize = Integer.parseUnsignedInt(attrs.getNamedItem("bufferBucketSize").getNodeValue().trim());
        if (bufferBucketSize <= 0) {
            bufferBucketSize = 64;
        }

        _workBufferPool = new BufferPool((int) parseSize(attrs.getNamedItem("minBufferSize").getNodeValue().trim()), bufferBucketSize);
        _tempBufferPool = new BufferPool((int) parseSize(attrs.getNamedItem("maxBufferSize").getNodeValue().trim()), bufferBucketSize);
        _ioBufferPool = new ByteBufferPool((int) parseSize(attrs.getNamedItem("transferBufferSize").getNodeValue().trim()), bufferBucketSize);
        maxConnections = Integer.parseUnsignedInt(attrs.getNamedItem("maxConnections").getNodeValue().trim());

        machine = new WebApplicationStartupInfo();
        this.parseApplication(machine, helper, helper.selectNode(root, "machine"));

        //applications
        appInfos = new NameValueCollection<>();

        File appdefs = new File(Utility.combinePath(this.bootstrapPath, "/conf/apps").toUri());
        if (appdefs.exists() && appdefs.isDirectory()) {
            appdefs.listFiles((File child) -> {
                if (child.getName().toLowerCase().endsWith(".xml")) {
                    try {
                        loadApp(child.toString());
                    } catch (Exception ex) {
                        getLogger().error("", ex);
                    }
                }
                return false;
            });
        }

        /*NodeList nodes = helper.selectNodes(root, "web.applications/application");
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            WebApplicationStartupInfo info = new WebApplicationStartupInfo();

            info.service = this;
            info.modules.putAll(machine.modules);
            info.settings.putAll(machine.settings);
            info.exports.putAll(machine.exports);
            info.documents.addAll(machine.documents);
            info.ignoreds.addAll(machine.ignoreds);
            info.action = machine.action;
            info.controller = machine.controller;
            info.disabledEntityBody = machine.disabledEntityBody;
            info.maxEntityBodySize = machine.maxEntityBodySize;

            info.serverPath = this.bootstrapPath;
            this.parseApplication(info, helper, nodes.item(i));

            appInfos.put(info.host, info);
        }*/
        //include
    }

    private void loadApp(String filename) throws XmlException {
        XmlHelper helper = XmlHelper.load(filename);
        Node root = helper.selectNode("/application");

        if (root == null) {
            return;
        }
        NamedNodeMap attrs = root.getAttributes();
        if (attrs == null) {
            return;
        }
        Node attr = attrs.getNamedItem("path");
        if (attr == null) {
            throw new XmlException("miss attribute [path]");
        }
        
        WebApplicationStartupInfo info = new WebApplicationStartupInfo();
        info.rootdir = attr.getNodeValue();
        info.service = this;
        info.serviceLoader=this.getLoader();
        info.modules.putAll(machine.modules);
        info.settings.putAll(machine.settings);
        info.exports.putAll(machine.exports);
        info.documents.addAll(machine.documents);
        info.ignoreds.addAll(machine.ignoreds);
        info.action = machine.action;
        info.controller = machine.controller;
        info.disabledEntityBody = machine.disabledEntityBody;
        info.maxEntityBodySize = machine.maxEntityBodySize;
        info.serverPath = this.bootstrapPath;
        
        NodeList nodes = helper.selectNodes(root, "dependency");
        if (nodes != null) {
            Node node;
            String s;
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                attr = node.getAttributes().getNamedItem("path");
                if (attr != null) {
                    s = attr.getNodeValue();
                    if (s != null && !"".equals(s) && !info.dependencyExt.contains(s)) {
                        info.dependencyExt.add(s);

                    }
                }
            }
        }
        File file = new File(info.rootdir+"/WEB-INF/mano.web.xml");
        if (!file.exists() || !file.getName().toLowerCase().endsWith(".xml")) {
            throw new XmlException("Nonreadable file:"+file);
        }
        helper = XmlHelper.load(file.toString());
        root = helper.selectNode("/application");
        if (root == null) {
            throw new XmlException("无效的应用配置文件");
        }
        parseApplication(info, helper, root);
        appInfos.put(info.host, info);
    }

    private void parseApplication(WebApplicationStartupInfo info, XmlHelper helper, Node root) throws XmlException {
        NamedNodeMap attrs;
        String s;
        NodeList nodes;
        Node attr;
        //base
        attrs = root.getAttributes();
        attr = attrs.getNamedItem("name");
        if (attr != null) {
            info.name = attr.getNodeValue();
        }
        attr = attrs.getNamedItem("host");
        if (attr != null) {
            info.host = attr.getNodeValue();
        }
        attr = attrs.getNamedItem("class");
        if (attr != null) {
            info.type = attr.getNodeValue();
        }
        attr = attrs.getNamedItem("vpath");
        if (attr != null) {
            info.path = attr.getNodeValue();
        }

        //request
        Node node = helper.selectNode(root, "request");
        if (node != null) {
            attrs = node.getAttributes();
            attr = attrs.getNamedItem("action");
            if (attr != null) {
                info.action = attr.getNodeValue();
            }
            attr = attrs.getNamedItem("controller");
            if (attr != null) {
                info.controller = attr.getNodeValue();
            }
            attr = attrs.getNamedItem("maxEntityBodySize");
            if (attr != null) {
                info.maxEntityBodySize = this.parseSize(attr.getNodeValue().trim());
            }
            attr = attrs.getNamedItem("maxEntityBodySize");
            if (attr != null) {
                info.disabledEntityBody = Utility.cast(Boolean.class, attr.getNodeValue().trim());
            }

            //文档
            nodes = helper.selectNodes(node, "document/add");
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    try {
                        attrs = nodes.item(i).getAttributes();
                        attr = attrs.getNamedItem("value");
                        s = attr == null ? "" : attr.getNodeValue().trim().toLowerCase();
                        if ("".equals(s) || info.documents.contains(s)) {
                            continue;
                        }
                        info.documents.add(s);
                    } catch (Exception ignored) {
                    }
                }
            }

            //忽略
            nodes = helper.selectNodes(node, "ignored/add");
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    try {
                        attrs = nodes.item(i).getAttributes();
                        attr = attrs.getNamedItem("value");
                        s = attr == null ? "" : attr.getNodeValue().trim().toLowerCase();
                        if ("".equals(s) || info.ignoreds.contains(s)) {
                            continue;
                        }
                        info.ignoreds.add(s);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        //依赖
        nodes = helper.selectNodes(root, "dependency/path");
        for (int i = 0; i < nodes.getLength(); i++) {
            attrs = nodes.item(i).getAttributes();
            try {
                s = attrs.getNamedItem("value").getNodeValue().trim();
            } catch (Exception ignored) {
                s = "";
            }
            if (!"".equals(s) && !info.dependency.contains(s)) {
                info.dependency.add(s);
            }
        }
        nodes = helper.selectNodes(root, "dependency/export");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();
                try {
                    info.exports.put(attrs.getNamedItem("name").getNodeValue().trim(), attrs.getNamedItem("class").getNodeValue().trim());
                } catch (Exception ex) {
                    Logger.getDefault().warn(null, ex);
                }
            }
        }

        //模块
        HttpModuleSettings module;
        nodes = helper.selectNodes(root, "modules/add");
        for (int i = 0; i < nodes.getLength(); i++) {
            attrs = nodes.item(i).getAttributes();
            attr = attrs.getNamedItem("name");
            s = (attr == null) ? "" : attr.getNodeValue().trim();
            if ("".equals(s) || info.modules.containsKey(s)) {
                logger.warn("module exists:%s", s);
                continue;
            }

            module = new HttpModuleSettings();
            module.name = s;
            module.type = attrs.getNamedItem("class").getNodeValue();
            module.params = new NameValueCollection<>();
            NodeList params = helper.selectNodes(nodes.item(i), "params/param");
            for (int j = 0; j < params.getLength(); j++) {
                attrs = params.item(j).getAttributes();
                module.params.put(attrs.getNamedItem("name").getNodeValue(), params.item(j).getTextContent());
            }
            info.modules.put(s, module);
        }

        //配置
        nodes = helper.selectNodes(root, "settings/add");
        for (int i = 0; i < nodes.getLength(); i++) {
            attrs = nodes.item(i).getAttributes();
            info.settings.put(attrs.getNamedItem("key").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
        }
    }

    boolean createContext(HttpRequestImpl req) {
        WebApplicationStartupInfo info = null;
        String host = req.headers().get("Host").value();
        for (WebApplicationStartupInfo i : appInfos.values()) {
            if (i.matchHost(host)) {
                info = i;
                break;
            }
        }
        //info = appInfos.get(context.getRequest().headers().get("Host").value());
        if (info == null) {
            info = appInfos.get("*"); //默认
        }
        if (info != null) {

            WebApplication app = info.getInstance();
            if (app != null) {

                HttpContextImpl context = new HttpContextImpl(req, new HttpResponseImpl(req.connection));
                context.server = info.getServerInstance();
                context.application = app;
                req.connection.context = context;
                //session
                Service svc = this.getContainer().getService("cache.service");
                if (svc != null && svc instanceof ServiceProvider) {
                    CacheProvider provider = ((ServiceProvider) svc).getService(CacheProvider.class);//TODO: 指定实例服务
                    if (provider != null) {
                        String sid = context.getRequest().getCookie().get(HttpSession.COOKIE_KEY);
                        context.session = HttpSession.getSession(sid, provider);
                    }
                }

                app.init(context);
                return true;
            }
        }

        return false;
    }

    final ArrayList<AsynchronousServerSocketChannel> listeners = new ArrayList<>();
    final ArrayList<AsynchronousSocketChannel> connections = new ArrayList<>();
    final AtomicInteger cos = new AtomicInteger();

    void onConnected(AsynchronousSocketChannel chan) {
        connections.add(chan);
        cos.addAndGet(1);
        HttpConnection conn = new HttpConnection();
        conn.service = this;
        //conn.buffer = this.workBufferPool().get();
        conn.open(chan);
        logger.info("current connections count:%s", cos.get());
    }

    void onClosed(HttpConnection conn) {
        synchronized (cos) {
            connections.remove(conn.channel);
            cos.addAndGet(-1);
            cos.notify();
        }
        this.connectionPool.put(conn);
        logger.info("current connections count:%s", cos.get());
    }

    //启动服务
    @Override
    public void run() {

        try {
            AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool());
            AsynchronousServerSocketChannel chan;
            for (ConnectionInfo info : infos.values()) {
                if (info.disabled) {
                    continue;
                }
                chan = AsynchronousServerSocketChannel.open(group);
                chan.bind(info.address, 1024);
                chan.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                listeners.add(chan);

                chan.accept(chan, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {

                    @Override
                    public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel server) {
                        onConnected(result);
                        synchronized (cos) {
                            while (cos.get() >= 100) {
                                try {
                                    //System.out.println("xxxxxxxxxxxx");
                                    cos.wait(1000 * 5);
                                } catch (InterruptedException ex) {
                                    failed(ex, server);
                                }
                            }
                            //System.out.println("yyyyyyyyyyyyyyyyyy");
                            server.accept(server, this);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, AsynchronousServerSocketChannel server) {
                        logger.fatal(null, exc);
                        listeners.remove(server);
                        try {
                            server.close();
                        } catch (IOException ex) {
                            //ignored
                        }
                    }

                });

                //conn = new AioConnection(info.address, Executors.newCachedThreadPool());
                //conn.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                //conn.bind(100);
                //conn.accept(_factory.get());
                logger.info("listening for:%s", info.address.toString());
            }
        } catch (IOException e) {
            logger.error("mano.http.HttpService.run", e);
        }
    }

    @Override
    public String getServiceName() {
        return this.name;
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

    //=========================================================
    private class ConnectionInfo {

        public InetSocketAddress address;
        public boolean disabled = false;
    }
}

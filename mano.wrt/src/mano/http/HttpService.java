/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import mano.Activator;
import mano.ContextClassLoader;
import mano.caching.CacheProvider;
import mano.io.BufferPool;
import mano.io.ByteBufferPool;
import mano.net.AioConnection;
import mano.net.Connection;
import mano.net.Task;
import mano.service.Service;
import mano.service.ServiceContainer;
import mano.service.ServiceProvider;
import mano.util.NameValueCollection;
import mano.util.Pool;
import mano.util.Utility;
import mano.util.logging.CansoleLogProvider;
import mano.util.logging.LogProvider;
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
    private Pool<HttpTask> _factory;
    private String bootstrapPath;
    private String configPath;
    private ContextClassLoader loader;
    private Logger logger = new Logger(new CansoleLogProvider());
    private String name;

    public HttpService() {
        _factory = new Pool<>(() -> new HttpTask(this));
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

    Task newTask() {
        return _factory.get();
    }

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
        NodeList nodes = helper.selectNodes(root, "web.applications/application");
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
        }
        //include
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
        attr = attrs.getNamedItem("rootdir");
        if (attr != null) {
            info.rootdir = attr.getNodeValue();
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

    boolean handle(HttpContextImpl context) {
        WebApplicationStartupInfo info = null;
        String host = context.getRequest().headers().get("Host").value();
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

                context._server = info.getServerInstance();
                context._application = app;

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

    void context(Connection conn) {
        HttpContextImpl context = new HttpContextImpl(this, conn);
        context.init();
    }

    //启动服务
    @Override
    public void run() {
        try {
            AioConnection conn;
            for (ConnectionInfo info : infos.values()) {
                if (info.disabled) {
                    continue;
                }
                conn = new AioConnection(info.address, Executors.newCachedThreadPool());
                conn.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                conn.bind(100);
                conn.accept(_factory.get());

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

    private class HttpTask extends Task {

        HttpService service;

        public HttpTask(HttpService svc) {
            service = svc;
        }

        @Override
        public void dispose() {

            if (!this.cancelDisposing) {
                return;
            }
            super.dispose();/*return;*/

            //service._factory.put(this);

        }

        @Override
        protected void onAccepted() {

            try {
                accept().setOption(StandardSocketOptions.TCP_NODELAY, true);
                accept().setOption(StandardSocketOptions.SO_REUSEADDR, true);
                accept().setOption(StandardSocketOptions.SO_KEEPALIVE, false);
            } catch (IOException e) {
                logger.error(HttpTask.class.getName(), e);
            }
            try {
                logger.info("connected:" + this.accept().getRemoteAddress());
            } catch (IOException ignored) {
            }

            //TODO: 服务器异常检测
            this.cancelDisposing = true;//重复使用当前对象
            Connection conn = accept();
            this.connect().accept(this);

            service.context(conn);
        }

        private HttpContextImpl getContext() {
            if (connect() == null || connect().isAcceptable()) {
                return null;
            }
            HttpContextImpl context = (HttpContextImpl) attachment();

            if (context == null || context.disposed || !this.connect().isConnected()) {
                return null;
            }
            return context;
        }

        @Override
        protected void onRead() {
            HttpContextImpl context = getContext();
            if (context == null) {
                this.onClosed();
                return;
            }

            if (this.operation() == Task.OP_BUFFER) {
                this.buffer().flip();
                context.buffer.write(this.buffer());
                if (!this.buffer().hasRemaining()) {
                    service.ioBufferPool().put(this.buffer());
                } else {
                    context.readBuffer = this.buffer();
                }
                context.buffer.flush();
            }
            context.run();
        }

        @Override
        protected void onWriten() {
            HttpContextImpl context = getContext();
            if (context == null) {
                this.onClosed();
            } else {
                connect().flush();
            }
        }

        @Override
        protected void onFailed() {
            if (connect().isAcceptable()) {
                return;
            }
            Throwable cause = error();
            HttpContextImpl context = getContext();
            if (context == null) {
                this.onClosed();
            } else if (cause != null && ((cause instanceof InterruptedByTimeoutException) || (cause.getMessage() + "").indexOf("connection was aborted") > 0)) {
                System.out.println("onFailed 2:" + cause.getMessage());
                this.onClosed();
            } else {
                context.onError(cause);
            }
        }

        @Override
        protected void onClosed() {

            System.out.println("closed conn");
            try {
                connect().close(true, null);//两次确认并关闭对象
            } catch (Exception e) {
                e.printStackTrace();
            }

            HttpContextImpl context = getContext();
            if (context != null) {
                context.dispose();
            }
        }

        @Override
        protected void onFlush() {
            HttpContextImpl context = getContext();
            if (context == null) {
                this.onClosed();
            } else {
                if (connect().hasWriteTaskQueued()) {
                    connect().flush();
                } else if (context.closedFlag.get()) {
                    //System.out.println("call complete:");
                    context.complete();
                }
            }
        }
    }

}

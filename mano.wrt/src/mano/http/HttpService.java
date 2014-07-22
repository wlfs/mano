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
import java.util.logging.Level;
import mano.Activator;
import mano.Service;
import mano.ServiceContainer;
import mano.ServiceProvider;
import mano.caching.CacheProvider;
import mano.io.BufferPool;
import mano.io.ByteBufferPool;
import mano.net.AioConnection;
import mano.net.Connection;
import mano.net.Task;
import mano.util.logging.Logger;
import mano.util.logging.ILogger;
import mano.util.NameValueCollection;
import mano.util.Pool;
import mano.util.Utility;
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
    private final BufferPool _workBufferPool; //工作缓冲区池,4k
    private final BufferPool _tempBufferPool; //临时缓冲区池,64k
    private final ByteBufferPool _ioBufferPool; //io缓冲区池,512b
    private Pool<HttpTask> _factory;
    private String bootstrapPath;
    private String configPath;
    private Activator loader;
    private ILogger logger;
    private String name;

    public HttpService() {
        _factory = new Pool<>(() -> new HttpTask(this));
        _workBufferPool = new BufferPool(4096, 16);//
        _tempBufferPool = new BufferPool(1024 * 64, 1);
        _ioBufferPool = new ByteBufferPool(512, 128);
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

    public ILogger getLogger() {
        return this.logger;
    }

    public Activator getLoader() {
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
        logger = provider.getService(ILogger.class);
        loader = provider.getService(Activator.class);

        if (params != null) {
            params.entrySet().stream().forEach(item -> {
                param(item.getKey(), item.getValue());
            });
        }

        try {
            this.configServices();
        } catch (XmlException ex) {
            //ex.printStackTrace();
            Logger.error("", ex);
        }
    }

    public void init(String serviceName, Activator activator, ILogger logger) {

        try {
            this.configServices();
        } catch (XmlException ex) {
            Logger.error("", ex);
        }
    }

    private void configServices() throws XmlException {

        XmlHelper helper = XmlHelper.load(this.configPath);
        Node node, attr, root = helper.selectNode("/configuration/http.service/machine");
        String s;

        //documents
        documents = new ArrayList<>();
        NamedNodeMap attrs;
        NodeList nodes = helper.selectNodes(root, "document/add");
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            attrs = node.getAttributes();
            attr = attrs.getNamedItem("value");
            s = attr.getNodeValue();
            s = (s == null) ? "" : s.trim().toLowerCase();
            if ("".equals(s) || documents.contains(s)) {
                continue;
            }
            documents.add(s);
        }

        //modules
        modules = new NameValueCollection<>();
        nodes = helper.selectNodes(root, "modules/add");
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            attrs = node.getAttributes();
            attr = attrs.getNamedItem("name");
            s = attr.getNodeValue();
            s = (s == null) ? "" : s.trim();
            if ("".equals(s) || modules.containsKey(s)) {
                continue;
            }

            HttpModuleSettings settings = new HttpModuleSettings();
            settings.name = s;
            settings.type = attrs.getNamedItem("type").getNodeValue();
            settings.params = new NameValueCollection<>();
            NodeList params = helper.selectNodes(nodes.item(i), "params/param");
            for (int j = 0; j < params.getLength(); j++) {
                attrs = params.item(j).getAttributes();
                settings.params.put(attrs.getNamedItem("name").getNodeValue(), params.item(j).getTextContent());
            }
            modules.put(s, settings);
        }

        //applications
        root = helper.selectNode("/configuration/http.service/web.applications");
        appInfos = new NameValueCollection<>();
        nodes = helper.selectNodes(root, "application");
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            attrs = node.getAttributes();
            attr = attrs.getNamedItem("host");
            s = attr.getNodeValue();
            s = (s == null) ? "" : s.trim();
            if ("".equals(s) || modules.containsKey(s)) {
                continue;
            }

            WebApplicationStartupInfo info = new WebApplicationStartupInfo();
            info.host = s;
            info.name = attrs.getNamedItem("name").getNodeValue();
            info.type = attrs.getNamedItem("type").getNodeValue();
            info.path = attrs.getNamedItem("path").getNodeValue();
            info.settings = new NameValueCollection<>();
            info.service = this;
            info.modules = modules;
            info.serverPath = this.bootstrapPath;
            NodeList params = helper.selectNodes(nodes.item(i), "settings/add");
            for (int j = 0; j < params.getLength(); j++) {
                attrs = params.item(j).getAttributes();
                info.settings.put(attrs.getNamedItem("key").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
            }

            /*params = helper.selectNodes(nodes.item(i), "params/param");
             for (int j = 0; j < params.getLength(); j++) {
             attrs = params.item(j).getAttributes();
             info.settings.put(attrs.getNamedItem("name").getNodeValue(), params.item(j).getTextContent());
             }*/
            appInfos.put(s, info);
        }
        //include
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

    public void param(String name, Object value) {
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

                Logger.info("listening for:%s", info.address.toString());
            }
        } catch (IOException e) {
            Logger.error("mano.http.HttpService.run", e);
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
        if (ILogger.class.getName().equals(serviceType.getName())) {
            return (T) logger;
        } else if (Activator.class.getName().equals(serviceType.getName())) {
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
                Logger.error(HttpTask.class.getName(), e);
            }
            try {
                Logger.info("connected:" + this.accept().getRemoteAddress());
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

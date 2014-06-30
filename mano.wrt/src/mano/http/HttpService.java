/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;


import mano.Activator;
import mano.Service;
import mano.io.BufferPool;
import mano.io.ByteBufferPool;
import mano.net.AioConnection;
import mano.net.Task;
import mano.util.CachedObjectFactory;
import mano.util.Logger;
import mano.util.NameValueCollection;
import mano.util.Utility;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import mano.web.WebApplication;
import mano.web.WebApplicationStartupInfo;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jun
 */
public class HttpService extends Service {

    private final BufferPool _workBufferPool; //工作缓冲区池,4k
    private final BufferPool _tempBufferPool; //临时缓冲区池,64k
    private final ByteBufferPool _ioBufferPool; //io缓冲区池,512b
    CachedObjectFactory<Task> _factory;

    public HttpService() {

        //this._logger=new ark.logging.Log4jProvider();
        _factory = new CachedObjectFactory<Task>(HttpConnectionHandler.class, this, this);

        _workBufferPool = new BufferPool(4096, 16);//
        _tempBufferPool = new BufferPool(1024 * 64, 1);
        _ioBufferPool = new ByteBufferPool(512, 128);
    }

    public BufferPool workBufferPool() {
        return _workBufferPool;
    }

    public BufferPool tempBufferPool() {
        return _tempBufferPool;
    }

    public ByteBufferPool ioBufferPool() {
        return _ioBufferPool;
    }

    Task getMessage() {
        return _factory.get();
    }

    @Override
    public void stop() {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(String serviceName, Activator activator, Logger logger) {
        super.init(serviceName, activator, logger);
        try {
            this.configServices();
        } catch (XmlException ex) {
            logger.error("", ex);
        }
    }

    ArrayList<String> documents;
    NameValueCollection<HttpModuleSettings> modules;
    NameValueCollection<WebApplicationStartupInfo> appInfos;

    void configServices() throws XmlException {

        XmlHelper helper = XmlHelper.load("E:\\repositories\\java\\mano.wrt\\src\\mano\\http\\config.xml");
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

            WebApplicationStartupInfo settings = new WebApplicationStartupInfo();
            settings.host = s;
            settings.name = attrs.getNamedItem("name").getNodeValue();
            settings.type = attrs.getNamedItem("type").getNodeValue();
            settings.path = attrs.getNamedItem("path").getNodeValue();
            settings.settings = new NameValueCollection<>();
            settings.service = this;
            settings.modules = modules;
            NodeList params = helper.selectNodes(nodes.item(i), "params/param");
            for (int j = 0; j < params.getLength(); j++) {
                attrs = params.item(j).getAttributes();
                settings.settings.put(attrs.getNamedItem("name").getNodeValue(), params.item(j).getTextContent());
            }
            appInfos.put(s, settings);
        }
        //include

    }

    boolean handle(HttpContextImpl context) {
        WebApplicationStartupInfo info = null;
        info = appInfos.get(context.request().headers().get("Host").value());
        if (info == null) {
            info = appInfos.get("*"); //默认
        }
        if (info != null) {
            WebApplication app = info.getInstance();
            if (app != null) {
                context._server = new HttpServer();
                context._server.root = info.path;
                context._application = app;
                app.init(context);
                return true;
            }
        }

        return false;
    }

    class ConnectionInfo {

        public InetSocketAddress address;
        public boolean disabled = false;
    }

    NameValueCollection<ConnectionInfo> infos = new NameValueCollection<>();

    @Override
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
        }
    }

    @Override
    public void run() {
        try {
            AioConnection conn;
            for (ConnectionInfo info : infos.values()) {
                if (info.disabled) {
                    continue;
                }
                conn = new AioConnection(_factory, info.address, Executors.newFixedThreadPool(10));
                conn.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                conn.bind(100);
                conn.accept(_factory.get());

                this.logger().infoFormat("listening for:%s", info.address.toString());
            }
        } catch (IOException e) {
            this.logger().error("", e);
        }
    }

    public class HttpConnectionHandler extends Task {

        HttpService service;
        UUID id;

        public HttpConnectionHandler(HttpService listener) {
            service = listener;
            id = UUID.randomUUID();
        }

        @Override
        public void dispose() {
            if (!this.cancelDisposing) {
                return;
            }
            super.dispose();
            service._factory.put(this);
        }

        @Override
        protected void onAccepted() {

            try {
                accept().setOption(StandardSocketOptions.TCP_NODELAY, true);
                accept().setOption(StandardSocketOptions.SO_REUSEADDR, true);
                accept().setOption(StandardSocketOptions.SO_KEEPALIVE, false);
            } catch (IOException e) {
                service.logger().error(HttpConnectionHandler.class.getName(), e);
            }
            try {
                service.logger().trace("connected:" + this.accept().getRemoteAddress());
            } catch (IOException ignored) {
            }

            HttpContextImpl context = new HttpContextImpl(service, accept());
            context.init();

            //TODO: 服务器异常检测
            this.cancelDisposing=true;//重复使用当前对象
            this.connect().accept(this);
            
        }

        @Override
        protected void onRead() {
            if (connect().isAcceptable()) {
                return;
            }
            HttpContextImpl context = (HttpContextImpl) attachment();

            if (context == null || context.disposed) {
                connect().close(null);
                return;
            }

            if (this.operation() == Task.OP_BUFFER){
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
            if (connect().isAcceptable()) {
                return;
            }
            HttpContextImpl context = (HttpContextImpl) attachment();
            if (context == null || context.disposed) {
                connect().close(null);
                return;
            } else if (!this.connect().connected() || (this.error() != null && this.error() instanceof InterruptedByTimeoutException) || (this.error() != null && this.error().getMessage().indexOf("connection was aborted") > 0)) {
                context.close();
                return;
            }
            
            if(connect().hasWriteTaskQueued()){
                connect().flush();
            }
            else if(context.closedFlag.get()){
                context.complete();
            }
        }

        @Override
        protected void onFailed() {
            if (connect().isAcceptable()) {
                return;
            }
            Throwable cause = error();
            HttpContextImpl context = (HttpContextImpl) attachment();
            if (context == null || context.disposed) {
                connect().close(null);
                return;
            } else if (!this.connect().connected()
                    || (cause != null && ((cause instanceof InterruptedByTimeoutException) 
                    || (cause.getMessage() + "").indexOf("connection was aborted") > 0))) {
                context.close();
                return;
            }
            context.onError(error());
        }

        @Override
        protected void onClosed() {
            System.out.println("closed conn");
            HttpContextImpl context = (HttpContextImpl) attachment();
            if (context != null) {
                context.dispose();
            }
        }
    }
}

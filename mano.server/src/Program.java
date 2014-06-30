/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
import mano.Activator;
import mano.Service;
import mano.logging.Log4jLogger;
import mano.util.Logger;
import mano.util.NameValueCollection;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author jun
 */
public class Program {

    Logger logger;
    Activator loader;
    NameValueCollection<Service> services;
    void init() {
        logger = new Log4jLogger();
        loader = new Activator();
    }

    void configServices() throws XmlException, InstantiationException, ClassNotFoundException {
        services = new NameValueCollection<>();
        XmlHelper helper = XmlHelper.load("E:\\repositories\\java\\mano.wrt\\src\\mano\\http\\config.xml");
        NodeList nodes = helper.selectNodes("/configuration/services/service");
        
        for (int i = 0; i < nodes.getLength(); i++) {
            NamedNodeMap attrs = nodes.item(i).getAttributes();

            String name = attrs.getNamedItem("name").getNodeValue();
            String type = attrs.getNamedItem("type").getNodeValue();
            
            Service service=(Service)loader.newInstance(type);
            
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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Program server = new Program();
        server.init();
        try {
            server.loader.register("E:\\repositories\\java\\mano\\dist");
            server.loader.register("E:\\repositories\\java\\mano.wrt\\dist");
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

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import java.util.Properties;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class Mano {

    private Mano() {
    }
    private static Properties props;
    private static final Mano instance;

    static {
        instance = new Mano();
        props = new Properties();
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }
    public static String getProperty(String key, String def) {
        return props.getProperty(key, def);
    }
    
    public static void setProperty(String key, String value){
        props.setProperty(key, value);
    }
    
    public static Properties getProperties() {
        return props;
    }
    
    public static void send(String serviceName,String action){
        //spi://service_name/action
        //spi://uniform_logging_service/log
        //entry:
        //logger_name
        //content
        //time
        //source
        
    }
    
}

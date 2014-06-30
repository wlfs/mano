/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.nio.file.Paths;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpServer {
    public String root;
    public String getRootPath(){
        return root;
    }
    
    public String mapPath(String vpath){
        return Paths.get(root,vpath).toString();
    }
    
    public String getVersion(){
        return "arkserver/1.1";
    }
    
}

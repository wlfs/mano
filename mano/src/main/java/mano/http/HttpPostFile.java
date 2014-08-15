/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpPostFile {
    protected String name;
    protected String original;
    protected String filename;
    protected String contentType;
    protected long size;
    protected File file;
    HttpPostFile(File f,String field,String original,String type,long len){
        this.file=f;
        this.name=field;
        this.original=original;
        this.contentType=type;
        this.size=len;
    }
    
    public String getName(){
        return this.name;
    }

    public String getFilename(){
        return this.filename;
    }

    public String getType(){
        return this.contentType;
    }
    
    public long getLength(){
        return this.size;
    }
    
    public void savaAs(String filename) throws IOException {
        
    }

}

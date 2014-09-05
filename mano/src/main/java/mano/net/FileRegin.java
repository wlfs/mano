/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.net;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class FileRegin implements Buffer {
    private String file;
    private long pos;
    private long limit;
    public String getFilename(){
        return file;
    }
    public long getPosition(){
        return pos;
    }
    public long getLimit(){
        return limit;
    }
    
    public static FileRegin create(String file,long pos,long limit){
        FileRegin result=new FileRegin();
        result.file=file;
        result.pos=pos;
        result.limit=limit;
        return result;
    }
    
}

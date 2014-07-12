/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.web;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class ViewEngine {
    
    /**
     * 编译一个模板文件，如果编译成功则返回编译的文件名，否则返回 null.
     * @param tempdir 临时目录
     * @param tplFilename 模板文件名
     * @return 
     */
    public abstract String compile(String tempdir,String tplName);
    
    /**
     * 
     * @param service 当前上下文
     * @param tempFilename
     * @return 
     */
    public abstract void render(RouteService service,String tmpName);
    
}

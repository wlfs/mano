/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.Disposable;
import java.util.Map;

/**
 * 表示 HTTP 请求的处理程序。
 * @author jun <jun@diosay.com>
 */
public interface HttpModule extends Disposable {
    /**
     * 使用指定参数初始化。
     * @param params 初始化模块需要的参数集合。
     */
    void init(Map<String,String> params);
    
    /**
     * 筛选并处理 HTTP 请求。
     * @param context 提供对用于为 HTTP 请求提供服务的上下文对象。
     * @return true 表示已接受该请求，否则 false。
     */
    boolean handle(HttpContext context);
    
    /**
     * 筛选并处理 HTTP 请求。
     * @param context 提供对用于为 HTTP 请求提供服务的上下文对象。
     * @return true 表示已接受该请求，否则 false。
     */
    boolean handle(HttpContext context,String tryPath);
    
}

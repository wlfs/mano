/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mano.http.HttpContext;
import mano.util.NameValueCollection;

/**
 * 表示一个路由服务帮助类。
 * @author jun <jun@diosay.com>
 */
public class RouteService {
    
    private final Map<String,Object> viewbag;
    private final HttpContext context;
    private ActionResult result;
    public RouteService(HttpContext c){
        if(c==null){
            throw new IllegalArgumentException();
        }
        this.context=c;
        viewbag=new NameValueCollection<>();
    }
    
    public Set<Entry<String,Object>> getEntries(){
        return this.viewbag.entrySet();
    }
    
    /**
     * 根据键获取视图字典的值。
     * @param key
     * @return 
     */
    public Object get(String key){
        return viewbag.get(key);
    }
    
    /**
     * 设置一个视图的项。
     * @param key
     * @param value 
     */
    public void set(String key,Object value){
        viewbag.put(key, value);
    }
    
    /**
     * 获取与当前服务关联的 HTTP 上下文。
     * @return 
     */
    public HttpContext getContext(){
        return this.context;
    }
    /**
     * 获取通过 setResult 的 action 结果。
     * 如果未设置，则返回 null.
     * @return 
     */
    public ActionResult getResult(){
        return result;
    }
    
    /**
     * 设置 action 的处理结果。
     * @param r 
     */
    public void setResult(ActionResult r){
        this.result=r;
    }
}

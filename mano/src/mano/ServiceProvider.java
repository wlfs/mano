/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 定义一种检索服务对象的机制，服务对象是为其他对象提供自定义支持的对象。
 * @author jun <jun@diosay.com>
 */
public interface ServiceProvider {

    /**
     * 获取指定类型的服务对象。
     * @param <T> 具体返回的类型。
     * @param type 一个对象，它指定要获取的服务对象的类型。
     * @return 返回 serviceType 类型的服务对象（如果有），没有则返回 null 。
     */
    <T> T GetService(Class<T> serviceType);
}

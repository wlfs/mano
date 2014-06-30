/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.io.Buffer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface HttpRequestHandler {

    /**
     * 当接收到数据时调用。
     *
     * @param buffer
     */
    void onRead(Buffer buffer) throws UnsupportedEncodingException,HttpException,IOException;

    /**
     * 重新设置处理程序。
     * @return 返回下一个需要处理程序。如果自身未处理完成，请返回 this (当前处理程序)。 注意：返回当前处理类的新实例，将可能无限递归中。
     */
    HttpRequestHandler next();
}

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
 * 表示一个 HTTP 请求实体正文处理程序。
 * @author jun <jun@diosay.com>
 */
public interface HttpEntityBodyHandler {
    /**
     * 当读取到数据时调用。
     * 
     * @param buffer
     * @param appender
     * @throws UnsupportedEncodingException
     * @throws HttpException
     * @throws IOException 
     */
    void onRead(Buffer buffer,HttpRequestAppender appender) throws UnsupportedEncodingException,HttpException,IOException;
}

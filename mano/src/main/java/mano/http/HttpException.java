/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

/**
 * 描述在处理 HTTP 请求期间发生的异常。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int _httpCode = 0;

    public HttpException(int httpCode, String message) {
        super(message);
        this._httpCode = httpCode;
    }
    
    public HttpException(int httpCode, Throwable t) {
        super(t);
        this._httpCode = httpCode;
    }

    /*
     * 获取要返回给客户端的 HTTP 响应状态代码。
     */
    public int getHttpCode() {
        return this._httpCode;
    }

}

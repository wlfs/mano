/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.InvalidOperationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import mano.util.logging.Logger;

/**
 * API 参考：
 * http://msdn.microsoft.com/zh-cn/library/system.web.httpresponse(v=vs.110).aspx
 * http://msdn.microsoft.com/zh-cn/library/system.web.httpworkerrequest(v=vs.110).aspx
 * http://docs.oracle.com/javaee/7/api/javax/servlet/ServletResponse.html
 */
/**
 * 封装来自处理程序操作的 HTTP 响应信息。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class HttpResponse {

    private int _status = 200;
    private String _statusDesc = "OK";
    private Charset _charset = Charset.forName("utf-8");

    /**
     * 获取返回给客户端的输出的 HTTP 状态代码。
     *
     * @return
     */
    public int status() {
        return this._status;
    }

    public String statusDescription() {
        return this._statusDesc;
    }

    /**
     * 设置返回给客户端的输出的 HTTP 状态代码。
     *
     * @param code
     * @throws NullPointerException
     * @throws ark.InvalidOperationException
     */
    public void status(int code) throws NullPointerException, InvalidOperationException {
        this.status(code, HttpStatus.getKnowDescription(code));
    }

    /**
     * 设置返回到客户端的 Status 栏。
     *
     * @param code
     * @param desc
     * @throws ark.InvalidOperationException
     */
    public void status(int code, String desc) throws InvalidOperationException {
        if (this.headerSent()) {
            throw new InvalidOperationException("HTTP Header has been sent.");
        }
        this._status = code;
        this._statusDesc = desc;
    }

    /**
     * 获取响应标头的集合。
     *
     * @return
     */
    public abstract HttpHeaderCollection headers();

    public abstract void setHeader(String name, String text) throws InvalidOperationException;

    public abstract void setHeader(HttpHeader header) throws InvalidOperationException;

    /**
     * 设置输出流的 HTTP MIME 类型。
     *
     * @param value
     * @throws ark.InvalidOperationException
     */
    public void setContentType(String value) throws InvalidOperationException {
        HttpHeader header;
        if (this.headers().containsKey("Content-Type")) {
            header = new HttpHeader("Content-Type", this.headers().get("Content-Type").text());
        } else {
            header = new HttpHeader("Content-Type");
        }
        header.value(value);
        this.setHeader(header);
    }

    public Charset charset() {
        return this._charset;
    }

    /**
     *
     * @param value
     * @throws UnsupportedCharsetException
     * @throws ark.InvalidOperationException
     */
    public void charset(String value) throws UnsupportedCharsetException, InvalidOperationException {
        this._charset = Charset.forName(value);
        HttpHeader header;
        if (this.headers().containsKey("Content-Type")) {
            header = new HttpHeader("Content-Type", this.headers().get("Content-Type").text());
        } else {
            header = new HttpHeader("Content-Type", "text/html");
        }
        header.attr("charset", value);
        this.setHeader(header);
    }

    /**
     * 获取一个值，指示是否已为当前的请求将 HTTP 响应标头发送到客户端。
     *
     * @return
     */
    public abstract boolean headerSent();

    /**
     * 获取一个值，指示客户端连接是否仍处于活动状态。
     *
     * @return
     */
    public abstract boolean isConnected();

    /**
     * 将请求重定向到新 URL 并指定该新 URL。
     *
     * @param url
     */
    public void redirect(String url) throws InvalidOperationException {
        this.redirect(url, true, false);
    }

    /**
     * 将客户端重定向到新的 URL。 指定新的 URL 并指定当前页的执行是否应终止。
     *
     * @param url
     * @param endResponse
     * @param isPermanent
     */
    public void redirect(String url, boolean endResponse) throws InvalidOperationException {
        this.redirect(url, endResponse, false);
    }

    /**
     * 将客户端重定向到新的 URL。 指定新的 URL 并指定当前页的执行是否应终止。
     *
     * @param url
     * @param endResponse
     * @param isPermanent
     */
    public void redirect(String url, boolean endResponse, boolean isPermanent) throws InvalidOperationException {
        this.setHeader("Location", url);
        try {
            this.status(!isPermanent ? HttpStatus.TemporaryRedirect : HttpStatus.MovedPermanently);
        } catch (NullPointerException e) {

        }
        if (endResponse) {
            this.end();
        }
    }
    private HttpCookie cookie = new HttpCookie();

    public HttpResponseCookie getCookie() {
        return cookie;
    }

    /**
     * 将一个字符串写入 HTTP 响应输出流。
     *
     * @param format
     * @param args
     */
    public void write(String format, Object... args) {

        byte[] array = ((args == null || args.length == 0) ? format.toString() : String.format(format, args)).getBytes(_charset);//Charset.forName("gbk")_charset
        this.write(array, 0, array.length);
    }

    /**
     * 将一个二进制字符串写入 HTTP 输出流。
     *
     * @param buffer
     * @param offset
     * @param count
     */
    public abstract void write(byte[] buffer, int offset, int count);

    /**
     * 将指定的文件直接写入 HTTP 响应输出流，而不在内存中缓冲该文件。
     *
     * @param filename
     * @throws java.io.FileNotFoundException
     */
    public abstract void transmit(String filename) throws FileNotFoundException, IOException;

    /**
     * 将指定的文件直接写入 HTTP 响应输出流，而不在内存中缓冲该文件。
     *
     * @param filename
     * @param position
     * @param length
     * @throws java.io.FileNotFoundException
     */
    public abstract void transmit(String filename, long position, long length) throws FileNotFoundException, IOException;

    /**
     * 向客户端发送当前所有缓冲的输出。
     */
    public abstract void flush();

    /**
     * 将当前所有缓冲的输出发送到客户端，停止该页的执行，并引发 EndRequest 事件。
     */
    public abstract void end();
}

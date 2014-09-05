/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.net.URL;
import java.util.Map;
import mano.InvalidOperationException;
import mano.net.Channel;
import mano.net.ChannelHandler;
import mano.util.Utility;

/**
 * API 参考：
 * http://msdn.microsoft.com/zh-cn/library/System.Web.HttpRequest(v=vs.110).aspx
 * http://msdn.microsoft.com/zh-cn/library/system.web.httpworkerrequest(v=vs.110).aspx
 * http://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServletRequest.html
 */
/**
 * 使处理程序能够读取客户端在 Web 请求期间发送的 HTTP 值。
 *
 * @author jun <jun@diosay.com>
 */
public abstract class HttpRequest {

    /**
     * 获取当前 HTTP 请求的方法。
     *
     * @return
     */
    public abstract String method();

    /**
     * 获取请求的 HTTP 版本（如“HTTP/1.1”）。
     *
     * @return
     */
    public abstract String version();

    /**
     * 获取请求的 HTTP 协议（HTTP 或 HTTPS）。
     *
     * @return
     */
    public abstract String protocol();

    /**
     * 获取请求标头的集合。
     *
     * @return
     */
    public abstract HttpHeaderCollection headers();

    /**
     * 获取当前请求的原始 URL。
     *
     * @return
     */
    public abstract String rawUrl();

    /**
     * 获取有关当前请求的 URL 的信息。
     *
     * @return
     */
    public abstract URL url();

    /**
     * 指定客户端发送的内容长度（以字节计）。
     *
     * @return
     */
    public abstract long getContentLength();

    /**
     * 获取 HTTP 查询字符串变量集合。
     *
     * @return
     */
    public abstract Map<String, String> query();

    /**
     * 获取窗体变量集合。
     * <p>
     * 在未手动调用 loadEntityBody 方法前调用该方法，将自动阻塞调用 loadEntityBody 方法进行默认处理。</p>
     *
     * @return
     */
    public abstract Map<String, String> form();

    /**
     * 获取采用多部分 MIME 格式的由客户端上载的文件的集合。
     * <p>
     * 在未手动调用 loadEntityBody 方法前调用该方法，将自动阻塞调用 loadEntityBody 方法进行默认处理。</p>
     *
     * @return
     */
    public abstract Map<String, HttpPostFile> files();
    private HttpCookieCollection cookie;

    public HttpRequestCookie getCookie() {
        if (cookie == null) {
            cookie = new HttpCookieCollection();
            String str = null;
            if (this.headers().containsKey("Cookie")) {
                str = this.headers().get("Cookie").text();
            }
            if (str == null || "".equals(str)) {
                return cookie;
            }
            int index;
            for (String item : Utility.split(str, ";", true)) {
                index = item.indexOf("=");
                if (index < 1) {
                    continue;
                }
                cookie.set(item.substring(0, index), item.substring(index + 1));
            }
        }
        return cookie;
    }

    /**
     * 获取一个值，指示 HTTP 连接是否使用安全套接字（即 HTTPS）。
     *
     * @return
     */
    public abstract boolean isSecure();

    /**
     * 获取一个值，指示客户端连接是否仍处于活动状态。
     *
     * @return
     */
    public abstract boolean isConnected();

    /**
     * 获取一个值，指示能否调用 <code>loadEntityBody</code> 处理 HTTP 请求实体正文。
     *
     * @return
     */
    public abstract boolean canLoadEntityBody();

    /**
     * 使用自定义处理程序载入并处理 HTTP 请求实体正文。
     *
     * @param handler 数据处理程序。
     * @throws ark.InvalidOperationException
     */
    public abstract void loadEntityBody(ChannelHandler<? extends Channel, ? extends Object> handler) throws Exception;

    /**
     * 使用默认处理程序载入并处理 HTTP 请求实体正文。
     *
     * @throws InvalidOperationException
     */
    public abstract void loadEntityBody() throws InvalidOperationException;

    /**
     * 强制终止基础 TCP 连接，这会导致任何显著的 I/O 失败。
     */
    public abstract void Abort();
}

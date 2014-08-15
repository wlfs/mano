/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Map.Entry;
import mano.DateTime;
import mano.InvalidOperationException;
import mano.http.HttpContext;
import mano.http.HttpHeaderCollection;
import mano.http.HttpModule;
import mano.http.HttpStatus;
import mano.util.NameValueCollection;
import mano.util.Utility;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class StaticFileModule implements HttpModule {

    private NameValueCollection<String> mappings;

    @Override
    public void init(WebApplication app, Map<String, String> params) {
        mappings = new NameValueCollection<>();
        for (Entry<String, String> entry : params.entrySet()) {
            String[] arr = entry.getKey().toLowerCase().split(":");
            if (arr.length > 1) {
                if ("ext".equals(arr[0])) {
                    mappings.put(arr[1], entry.getValue());
                }
            }
        }
    }

    private boolean process(HttpContext context, String path, String mime) {

        context.getApplication().getLogger().debug("process static path:" + path);

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        boolean range = false;
        String etag = "\"" + Integer.toHexString((file.lastModified() + "" + file.length() + path).hashCode()) + ":df3\"";
        HttpHeaderCollection headers = context.getRequest().headers();
        long since, start = 0, end = 0, len = -1;
        try {
            since = DateTime.parseTime(headers.get("If-Modified-Since").value(), DateTime.FORMAT_GMT);
        } catch (Exception e) {
            since = 0;
        }

        //TODO: ETag 未生效
        //Expires、Cache-Control和Vary
        //If-Modified-Since: Fri, 12 May 2006 18:53:33 GMT
        //If-None-Match: W/"50b1c1d4f775c61:df3"
        //http://www.cnblogs.com/tyb1222/archive/2011/12/24/2300246.html
        boolean done = true;
        try {
            long size;
            try (FileChannel chan = new FileInputStream(file).getChannel()) {
                size = chan.size();
            }
            context.getResponse().setContentType(mime);

            if (file.lastModified() == since
                    || (headers.containsKey("If-None-Match")
                    && etag.equalsIgnoreCase(headers.get("If-None-Match").value()))) {

                context.getResponse().status(HttpStatus.NotModified);

            } else if (headers.containsKey("Range")) {
                if (headers.containsKey("If-Range") && !etag.equalsIgnoreCase(headers.get("If-Range").value())) {
                    context.getResponse().setContentLength(size);
                    context.getResponse().status(HttpStatus.OK);
                    done = false;
                } else {
                    String[] arr = headers.get("Range").value().split("=");//bytes=0-
                    if (arr.length != 2 || !"bytes".equalsIgnoreCase(arr[0])) {
                        context.getResponse().status(HttpStatus.NotAcceptable);
                    } else {
                        //http://blog.csdn.net/shuimuniao/article/details/8086438
                        int state = 0;

                        //不接受多个范围
                        arr = Utility.split(arr[1], ",", true);
                        if (arr.length != 1) {
                            context.getResponse().status(HttpStatus.RequestedRangeNotSatisfiable);
                        } else {
                            arr = (" " + arr[0] + " ").split("-");
                            if (arr.length != 2) {
                                context.getResponse().status(HttpStatus.BadRequest);
                            } else {

                                try {
                                    if ("".equals(arr[0].trim())) {
                                        start = -1;
                                    } else {
                                        start = Long.parseUnsignedLong(arr[0].trim());
                                    }
                                } catch (NumberFormatException e) {
                                    state = 1;
                                    start = -1;
                                }
                                try {
                                    if ("".equals(arr[1].trim())) {
                                        end = -1;
                                    } else {
                                        end = Long.parseUnsignedLong(arr[1].trim());
                                    }
                                } catch (NumberFormatException e) {
                                    end = -1;
                                    state = 1;
                                }
                                if (state == 0) {
                                    if (start == 0 && end == 0) {
                                        len = 1;
                                    } else if (start == -1 && end > 0) {
                                        start = 0;
                                        len = end + 1;

                                    } else if (start >= 0 && end < 0) {
                                        end = size - 1;
                                        len = size - start;
                                    } else if (start > 0 && end > 0) {
                                        len = end - start + 1;
                                    } else {
                                        len = -1;
                                        state = 1;
                                    }
                                }

                                if (state != 0) {
                                    context.getResponse().status(HttpStatus.BadRequest);
                                } else {
                                    if (start == 0 && end == size - 1) {
                                        context.getResponse().setContentLength(size);
                                        context.getResponse().status(HttpStatus.OK);
                                    } else {
                                        range = true;
                                        context.getResponse().setContentLength(len);
                                        context.getResponse().status(HttpStatus.PartialContent);
                                    }
                                    context.getResponse().setHeader("Content-Range", "bytes " + start + "-" + end + "/" + size);
                                    done = false;
                                }
                            }
                        }
                    }
                }
            } else {
                context.getResponse().setContentLength(size);
                context.getResponse().status(HttpStatus.OK);
                done = false;
            }

            if (!done) {

                context.getResponse().setHeader("Accept-Ranges", "bytes");
                context.getResponse().setHeader("ETag", etag);
                context.getResponse().setHeader("Last-Modified", new DateTime(file.lastModified()).toGMTString());
                switch (context.getRequest().method().toUpperCase()) {
                    case "GET":
                        if (range) {
                            context.getResponse().transmit(path, start, len);
                        } else {
                            context.getResponse().transmit(path);
                        }
                        break;
                    case "HEAD":
                        //nothing
                        break;
                    default:
                        context.getResponse().status(HttpStatus.MethodNotAllowed);
                }

            }
        } catch (IOException | InvalidOperationException | NullPointerException ex) {
            context.getResponse().write(ex.getMessage());
            context.getResponse().end();
        }

        return true;
    }

    @Override
    public boolean handle(HttpContext context, String tryPath) {
        tryPath = (tryPath == null) ? "" : tryPath.toLowerCase().trim();
        if (tryPath == null) {
            return false;//http://www.cnblogs.com/ghfsusan/archive/2010/06/01/1749607.html http://www.open-open.com/lib/view/open1342064478859.html
        }
        boolean result = false;
        for (Entry<String, String> entry : mappings.entrySet()) {
            if (tryPath.endsWith("." + entry.getKey())) {
                result = this.process(context, context.getServer().mapPath(tryPath), entry.getValue());
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public boolean handle(HttpContext context) {
        return this.handle(context, context.getRequest().url().getPath());
    }

    @Override
    public void dispose() {
        mappings.clear();
    }

}

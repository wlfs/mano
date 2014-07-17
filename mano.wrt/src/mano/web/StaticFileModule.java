/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import mano.InvalidOperationException;
import mano.http.HttpContext;
import mano.http.HttpModule;
import mano.http.HttpStatus;
import mano.util.DateTime;
import mano.util.NameValueCollection;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;

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
        System.out.println(path);
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        String etag = Integer.toHexString((file.lastModified() + "" + file.length()).hashCode());
        //TODO: ETag 未生效
        //Expires、Cache-Control和Vary
        //If-Modified-Since: Fri, 12 May 2006 18:53:33 GMT
        //If-None-Match: W/"50b1c1d4f775c61:df3"
        //http://www.cnblogs.com/tyb1222/archive/2011/12/24/2300246.html
        try {
            context.getResponse().setContentType(mime);
            boolean cached = context.getRequest().headers().containsKey("If-Modified-Since") && context.getRequest().headers().containsKey("If-None-Match");

            try {
                cached = cached && file.lastModified() == DateTime.parseTime(context.getRequest().headers().get("If-Modified-Since").value(), DateTime.FORMAT_GMT);
            } catch (ParseException e) {
                cached = false;
            }
            cached = cached && etag.equals(context.getRequest().headers().get("If-None-Match").value());
            if (cached) {
                context.getResponse().status(HttpStatus.NotModified);
            } else {
                context.getResponse().setHeader("Cache-Control", "private");
                context.getResponse().setHeader("ETag", "\"" + etag + ":0\"");
                context.getResponse().setHeader("Last-Modified", DateTime.format(DateTime.FORMAT_GMT, file.lastModified()));
                context.getResponse().transmit(path);
            }
            //ETag: "50b1c1d4f775c61:df3" 
            //Last-Modified: Fri, 12 May 2006 18:53:33 GMT

        } catch (IOException | InvalidOperationException | NullPointerException ex) {
            context.getResponse().write(ex.getMessage());
        }
        context.getResponse().end();

        return true;
    }

    @Override
    public boolean handle(HttpContext context, String tryPath) {
        tryPath = (tryPath == null) ? "" : tryPath.toLowerCase().trim();
        if (tryPath == null) {
            return false;
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

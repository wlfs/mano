/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import mano.util.logging.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpUtil {

    public static void queryStringToMap(String query, Map<String, String> result) {
        query = query == null ? "" : query.trim();
        if (query.startsWith("?")) {
            query = query.substring(1);
        }
        if ("".equals(query)) {
            return;
        }

        String key;
        String value;
        int index;
        for (String s : query.split("&")) {
            index = s.indexOf('=');
            value = null;
            if (index > 0) {
                key = (s.substring(0, index) + "").trim();
                value = (s.substring(index + 1) + "").trim();
            } else {
                key = s.trim();
            }

            if (key != null && !"".equals(key)) {
                try {
                    result.put(key, URLDecoder.decode(value, "UTF-8"));//TODO:编码识别GB18030
                } catch (UnsupportedEncodingException ex) {
                    result.put(key, value);
                }
            }
        }
    }
}

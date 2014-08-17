/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import mano.io.Buffer;
import mano.net.ByteArrayBuffer;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpFormUrlEncodedParser implements HttpEntityBodyHandler {

    private boolean done;
    private long read = 0;
    private Buffer worker;

    @Override
    public void onRead(ByteArrayBuffer buffer, HttpRequestAppender appender) throws UnsupportedEncodingException, HttpException, IOException {
        System.out.println("=====kkkkkkkkkkkkk");
        if (done) {
            //buffer.reset();
            return;
        }
        
        String line;
        if (buffer.length() == appender.getContentLength()) {
            System.out.println("=====aa");
            line = buffer.readstr();
            
        } else if (appender.getContentLength() > buffer.inner().capacity()) {
            System.out.println("=====bbbb");
            if (worker == null) {
                if (appender.getContentLength() > Integer.MAX_VALUE - 1) {
                    throw new HttpException(HttpStatus.RequestEntityTooLarge, "Request entity too large");
                }
                worker = new Buffer(new byte[(int) appender.getContentLength()], 0, (int) appender.getContentLength());
            }

            if (!worker.hasRemaining()) {
                throw new HttpException(HttpStatus.RequestEntityTooLarge, "Request entity too large");
            }

            worker.write(buffer.array, buffer.position(), buffer.length());
            buffer.position(buffer.position() + buffer.length());
            line = worker.readln();
        } else {
            System.out.println("=====ggggggggggggg");
            line = buffer.readln();
        }
        if (line == null) {
            System.out.println("=====dddddddddddddddd");
            return;
        }
        parse(line, appender);
    }

    private void parse(String line, HttpRequestAppender appender) {
        done = true;
        System.out.println(line);
        HashMap<String, String> map = new HashMap<>();
        HttpUtil.queryStringToMap(line, map);
        map.entrySet().forEach(item -> {
            appender.appendFormItem(item.getKey(), item.getValue());
        });
    }
}

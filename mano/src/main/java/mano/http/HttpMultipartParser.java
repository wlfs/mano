/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.io.Buffer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import mano.net.ByteArrayBuffer;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpMultipartParser implements HttpEntityBodyHandler {

    final int TOKEN = 1, HEADER = 2, DATA = 3,FILE=4,FORM=5;
    int state = 1;
    byte[] _endBoundary;
    byte[] _boundary;
    byte[] CRLF = "\r\n".getBytes();
    boolean eof = false;
    int index = -1;
    int type=0;
    long size;
    HttpHeaderCollection headers = new HttpHeaderCollection();
    OutputStream out;
    File file;
    public HttpMultipartParser(String boundary){
        _endBoundary = ("\r\n" + boundary + "--\r\n").getBytes();
        _boundary = ("\r\n" + boundary + "\r\n").getBytes();
    }

    @Override
    public void onRead(ByteArrayBuffer buffer, HttpRequestAppender appender) throws UnsupportedEncodingException, HttpException, IOException {

        if (state == TOKEN) {
            index = findBoundary(true, buffer);
            if (index < 0) {
                throw new HttpException(HttpStatus.BadRequest, "Miss multipart Boundary.");
                //buffer.position(buffer.position() + buffer.length());
            } else {
                buffer.position(buffer.position() + (index - buffer.arrayOffset()) + (_boundary.length - 2));
                state = HEADER;
            }
        } else if (state == HEADER) {
            readHeaders(buffer);
        } else if (state == DATA) {
            this.Data(buffer,appender);
        }
    }

    void readHeaders(ByteArrayBuffer buffer) throws UnsupportedEncodingException, HttpException, IOException {
        String line;
        while ((line = buffer.readln()) != null) {
            if ("".equals(line)) {
                state = DATA;
                HttpHeader header=headers.get("Content-Disposition");
                if(header==null){
                    throw new HttpException(HttpStatus.BadRequest, "Miss the Multipart-Entity Header Content-Disposition.");
                }
                String fn=header.attr("filename");
                if(fn==null || "".equals(fn.trim())){
                    type=FORM;
                    out = new ByteArrayOutputStream();
                }
                else{
                    type=FILE;
                    file=File.createTempFile("post_", ".tmp");
                    out=new FileOutputStream(file);
                }
                
                return;
            } else {
                HttpHeader header = HttpHeader.prase(line);
                if (header == null) {
                    throw new HttpException(HttpStatus.BadRequest, "Multipart Entity Header can not be resolved.");
                }
                state = HEADER;
                headers.put(header);
            }
        }

    }

    private void Data(ByteArrayBuffer buffer, HttpRequestAppender appender) throws IOException {
        index = findBoundary(false, buffer);
        int count = 0;
        if (index < 0) {
            //保存已经读取的数据
            int len = buffer.length();
            

            if (buffer.length() >= _boundary.length) {
                count = len - (_boundary.length - 4);
            }

            if (count > 0) {
                out.write(buffer.array, buffer.arrayOffset(), count);
                buffer.position(buffer.position() + count);
                size+=count;
            }
        } else {
            count=index - buffer.arrayOffset();
            if(count>0){
                out.write(buffer.array, buffer.arrayOffset(), count);
                size+=count;
            }
            buffer.position(buffer.position() + count + (eof ? _endBoundary.length : _boundary.length));
            
            state = HEADER;
            this.done(appender);
        }
    }

    private int findBoundary(boolean first, ByteArrayBuffer buffer) {

        if (first) {
            index = Buffer.bytesIndexOf(buffer.array, buffer.arrayOffset(), buffer.length(), _boundary, CRLF.length, _boundary.length - CRLF.length);
        } else {
            index = Buffer.bytesIndexOf(buffer.array, buffer.arrayOffset(), buffer.length(), _boundary);
            if (index < 0) {
                index = Buffer.bytesIndexOf(buffer.array, buffer.arrayOffset(), buffer.length(), _endBoundary);
                if (index > -1) {
                    eof = true;
                }
            }
        }
        return index;
    }
    
    private void done(HttpRequestAppender appender) throws IOException{
        HttpHeader header=headers.get("Content-Disposition");
        
        if(type==FILE){
            HttpPostFile pf=new HttpPostFile(file,
                    header.attr("name"),
                    header.attr("filename"),
                    headers.containsKey("Content-Type")?headers.get("Content-Type").value():"",
                    size);
            out.flush();
            out.close();
            appender.appendPostFile(pf);
        }
        else{
            appender.appendFormItem(header.attr("name"), out.toString());
            out.close();
        }
        eof=false;
        out=null;
        headers.clear();
    }

}

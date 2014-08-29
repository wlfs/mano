/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import mano.io.Buffer;
import mano.net.ByteArrayBuffer;
import mano.net.Channel;
import mano.net.ChannelHandler;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpMultipartParser<C extends Channel, A extends HttpRequestAppender> extends ChannelHandler<C, A> {

    final int IDLE = 0, TOKEN = 1, HEADER = 2, DATA = 3, FILE = 4, FORM = 5;//HttpRequestAppender Channel
    int state = 0;
    byte[] _endBoundary;
    byte[] _boundary;
    byte[] CRLF = "\r\n".getBytes();
    boolean eof = false;
    int index = -1;
    int type = 0;
    long size;
    HttpHeaderCollection headers = new HttpHeaderCollection();
    OutputStream out;
    File file;

    /*
     public HttpMultipartParser(String boundary) {
     _endBoundary = ("\r\n" + boundary + "--").getBytes();
     _boundary = ("\r\n" + boundary + "\r\n").getBytes();
     }

     public void onRead(ByteArrayBuffer buffer, HttpRequestAppender appender) throws UnsupportedEncodingException, HttpException, IOException {

     if (state == TOKEN) {
     index = findBoundary(true, buffer);
     if (index < 0) {
     if (buffer.position() == 0 && !buffer.hasRemaining()) {//full
     throw new HttpException(HttpStatus.BadRequest, "Miss multipart Boundary.");
     }
     //state == TOKEN
     //buffer.position(buffer.position() + buffer.length());
     } else {
     //buffer.position((index - (buffer.arrayOffset() + buffer.position())) + (_boundary.length - 2));
     buffer.position(buffer.position() + (index - (buffer.arrayOffset() + buffer.position())) + (_boundary.length - 2));
     state = HEADER;
     this.onRead(buffer, appender);
     }
     } else if (state == HEADER) {
     readHeaders(buffer, appender);
     } else if (state == DATA) {
     this.Data(buffer, appender);
     }
     }
     */
    void readHeaders(C channel, int bytesRead, ByteArrayBuffer buffer, final A attachment) throws Exception {
        String line;
        while ((line = buffer.readln()) != null) {
            if ("".equals(line)) {
                HttpHeader header = headers.get("Content-Disposition");
                if (header == null) {
                    throw new HttpException(HttpStatus.BadRequest, "Miss the Multipart-Entity Header Content-Disposition.");
                }
                String fn = header.attr("filename");
                if (fn == null || "".equals(fn.trim())) {
                    type = FORM;
                    out = new ByteArrayOutputStream();
                } else {
                    type = FILE;
                    file = File.createTempFile("post_", ".tmp");
                    out = new FileOutputStream(file);
                }
                state = DATA;
                this.onRead(channel, bytesRead, buffer, attachment);
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
        buffer.compact();
        if (!buffer.hasRemaining()) {
            throw new IndexOutOfBoundsException("buffer has been full.");
        }
        channel.read(this, attachment);
    }

    private void data(C channel, int bytesRead, ByteArrayBuffer buffer, final A attachment) throws Exception {
        index = findBoundary(false, buffer);
        int count = 0;
        int off = buffer.arrayOffset() + buffer.position();
        if (index < 0) {
            //保存已经读取的数据
            count = buffer.length();

            if (count >= _boundary.length) {
                count -= (_boundary.length);
            } else if (!buffer.hasRemaining()) {
                count = 1;
            }

            if (count > 0) {
                out.write(buffer.array, off, count);
                buffer.position(buffer.position() + count);
                size += count;
            }
            buffer.compact();
            if (!buffer.hasRemaining()) {
                throw new IndexOutOfBoundsException("buffer has been full.");
            }
            channel.read(this, attachment);

        } else {
            count = index - off;
            if (count > 0) {
                out.write(buffer.array, off, count);
                size += count;
            }
            buffer.position(buffer.position() + count + (eof ? _endBoundary.length : _boundary.length));
            state = HEADER;
            this.done(channel, bytesRead, buffer, attachment);
        }
    }

    private int findBoundary(boolean first, ByteArrayBuffer buffer) {

        if (first) {
            index = ByteArrayBuffer.bytesIndexOf(buffer.array, buffer.arrayOffset() + buffer.position(), buffer.length(), _boundary, CRLF.length, _boundary.length - CRLF.length);
        } else {
            index = ByteArrayBuffer.bytesIndexOf(buffer.array, buffer.arrayOffset() + buffer.position(), buffer.length(), _boundary);
            if (index < 0) {
                index = ByteArrayBuffer.bytesIndexOf(buffer.array, buffer.arrayOffset() + buffer.position(), buffer.length(), _endBoundary);
                if (index > -1) {
                    eof = true;
                }
            }
        }
        return index;
    }

    private void done(C channel, int bytesRead, ByteArrayBuffer buffer, final A attachment) throws Exception {
        HttpHeader header = headers.get("Content-Disposition");

        if (type == FILE) {
            HttpPostFile pf = new HttpPostFile(file,
                    header.attr("name"),
                    header.attr("filename"),
                    headers.containsKey("Content-Type") ? headers.get("Content-Type").value() : "",
                    size);
            out.flush();
            out.close();
            attachment.appendPostFile(pf);
        } else {
            attachment.appendFormItem(header.attr("name"), ((ByteArrayOutputStream) out).toString("utf-8"));
            out.close();
        }
        out = null;
        headers.clear();
        file = null;
        if (eof) {
            state = IDLE;
            eof = false;
            attachment.notifyDone();
        } else {
            this.onRead(channel, bytesRead, buffer, attachment);
        }
    }

    @Override
    protected void onRead(C channel, int bytesRead, ByteArrayBuffer buffer, final A attachment) throws Exception {

        if (state == IDLE) {
            _endBoundary = ("\r\n" + attachment.getBoundary() + "--").getBytes();
            _boundary = ("\r\n" + attachment.getBoundary() + "\r\n").getBytes();
            state = TOKEN;
            this.onRead(channel, bytesRead, buffer, attachment);
        } else if (state == TOKEN) {
            index = findBoundary(true, buffer);
            if (index < 0) {
                if (buffer.position() == 0 && !buffer.hasRemaining()) {//full
                    throw new HttpException(HttpStatus.BadRequest, "Miss multipart Boundary.");
                }
                buffer.compact();
                if (!buffer.hasRemaining()) {
                    throw new IndexOutOfBoundsException("buffer has been full.");
                }
                channel.read(this, attachment);
            } else {
                buffer.position(buffer.position() + (index - (buffer.arrayOffset() + buffer.position())) + (_boundary.length - 2));
                state = HEADER;
                this.onRead(channel, bytesRead, buffer, attachment);
            }
        } else if (state == HEADER) {
            this.readHeaders(channel, bytesRead, buffer, attachment);
        } else if (state == DATA) {
            this.data(channel, bytesRead, buffer, attachment);
        }
    }

    @Override
    protected void onFailed(C channel, Throwable exc) {
        channel.onFailed(this, exc);
    }

}

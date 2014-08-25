/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.http;

import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import mano.DateTime;
import mano.net.AioSocketChannel;
import mano.net.Buffer;
import mano.net.ByteArrayBuffer;
import mano.net.Channel;
import mano.net.ChannelHandler;
import mano.util.ObjectFactory;
import mano.util.Utility;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpChannel extends AioSocketChannel {
    
    Charset charset = Charset.forName("utf-8");
    private ParseHeader parseHeader = new ParseHeader();
    HttpService service;
    boolean sentError;
    
    @Override
    public void open(AsynchronousSocketChannel chan, ByteArrayBuffer buf) {
        buf.reset();
        super.open(chan, buf);
        this.read(parseHeader, null);
    }
    
    @Override
    public void onFailed(Object sender, Throwable exc) {
        if (!sentError && isOpen()) {
            try {
                sentError = true;
                HttpException ex;
                if (exc instanceof HttpException) {
                    ex = (HttpException) exc;
                } else {
                    ex = new HttpException(HttpStatus.InternalServerError, exc);
                }
                
                byte[] response = String.format("<html><head><title>HTTP %s Error</title></head><body>%s</body></html>", ex.getHttpCode(), ex.getMessage()).getBytes(charset);
                StringBuilder sb = new StringBuilder("HTTP/1.1 ");
                sb.append(ex.getHttpCode()).append(" ").append(HttpStatus.getKnowDescription(ex.getHttpCode())).append("\r\n");
                sb.append("Content-Length:").append(response.length).append("\r\n");
                sb.append("Connection:close\r\n");
                sb.append("Date:").append(DateTime.now().toGMTString()).append("\r\n");
                sb.append("\r\n");
                
                byte[] bytes = sb.toString().getBytes();
                this.write(new ByteArrayBuffer(bytes, 0, bytes.length));
                this.write(new ByteArrayBuffer(response, 0, response.length));
                this.close(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            exc.printStackTrace();
        }
    }
    
    @Override
    public void onFlush(Buffer buffer, long bytesTransferred) {
        
    }
    
    @Override
    public void onClosed() {
    service.onClosed(this);
    }
    
    private static class ParseHeader extends ChannelHandler<HttpChannel, HttpRequestImpl> {
        
        boolean requestline;
        
        @Override
        protected void onRead(HttpChannel channel, int bytesRead, ByteArrayBuffer buffer, HttpRequestImpl request) {
            String line;
            HttpHeader header;
            boolean done = false;
            while ((line = buffer.readln(channel.charset)) != null) {
                if (MANO_WEB_MACRO.DEBUG) {
                    System.out.println(line);
                }
                if (!requestline) {
                    
                    String[] arr = Utility.split(line, " ", true);
                    if (arr.length != 3) {
                        onFailed(channel, new HttpException(HttpStatus.BadRequest, "Bad Request(Incorrect Request Line)"));
                        return;
                    }
                    if (request == null) {
                        request = new HttpRequestImpl(channel);
                    }
                    request.method = arr[0].trim();
                    request.rawUrl = arr[1].trim();
                    request.version = arr[2].trim();
                    requestline = true;
                } else if ("".equals(line.trim())) {
                    done = true;
                    break;
                } else {
                    header = HttpHeader.prase(line);
                    if (header == null) {
                        onFailed(channel, new HttpException(HttpStatus.BadRequest, "Bad Request(Incorrect Header Entry)"));
                        return;
                    }
                    request.headers.put(header);
                }
            }
            if (!done) {
                channel.read(this, request);
            } else {
//                HttpResponseImpl rsp=new HttpResponseImpl(channel);
//                rsp.write("hello");
//                rsp.end();
                if (channel.service == null || !channel.service.processRequest(request)) {
                    onFailed(channel, new HttpException(HttpStatus.BadRequest, "Bad Request(Invalid Hostname)"));
                }
            }
        }
        
        @Override
        protected void onFailed(HttpChannel channel, Throwable exc) {
            channel.onFailed(this, exc);
        }
    }
    
}

//    class Listener {
//        
//        void setChannelFactory(ObjectFactory<ChannelHandler> factory) {
//            
//        }
//    }
//    class bb extends ChannelHandler<Conn> {
//
//        @Override
//        protected void onRead(Conn channel, int bytesRead, Buffer buffer, Object token) {
//
//            Listener listener = new Listener();
//            listener.setChannelFactory(() -> new bb());
//
//            channel.read(this, token);
//            ByteArrayBuffer buf = (ByteArrayBuffer) buffer;
//            
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        protected void onFailed(Channel channel, Throwable exc) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//    }


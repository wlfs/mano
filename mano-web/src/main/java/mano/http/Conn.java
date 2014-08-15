/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.http;

import mano.net.AioSocketChannel;
import mano.net.Buffer;
import mano.net.ByteArrayBuffer;
import mano.net.Channel;
import mano.net.ChannelHandler;
import mano.util.ObjectFactory;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Conn extends AioSocketChannel {

    @Override
    public void onFailed(Object sender, Throwable exc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onClosed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    interface ChannelContext<T extends Channel> {

        ByteArrayBuffer allocate();
        
        ByteArrayBuffer dreact();
        
        Buffer getBuffer();

        void putBuffer(Buffer buffer);

        T getChannel();

        void putChannel(T chan);

        void execute(Runnable task);
        
        
    }

    class Listener {

        void setChannelFactory(ObjectFactory<ChannelHandler> factory) {

        }
    }

    class bb extends ChannelHandler<Conn> {

        @Override
        protected void onRead(Conn channel, int bytesRead, Buffer buffer, Object token) {

            Listener listener = new Listener();
            listener.setChannelFactory(() -> new bb());

            channel.read(this, token);
            ByteArrayBuffer buf = (ByteArrayBuffer) buffer;
            
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected void onFailed(Channel channel, Throwable exc) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}

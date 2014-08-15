/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.net;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class ChannelHandler<T extends Channel> implements Runnable {

    private T chan;
    private int reads;
    private Buffer buf;
    private Object attachment;
    private Throwable error;
    private Object sender;

    final void init(Object sender, Channel channel, int bytesTransferred, Buffer buffer, Object token, Throwable exc) {
        this.sender = sender;
        this.error = exc;
        //this.chan = channel;
        this.reads = bytesTransferred;
        this.buf = buffer;
        this.attachment = token;
    }
    
    final void attach(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public synchronized final void run() {
        if (this.error == null) {
            try {
                this.onRead(chan, reads, buf, attachment);
            } catch (Exception ex) {
                this.error = ex;
            }
        }

        if (this.error != null) {
            try {
                this.onFailed(chan, error);
            } catch (Exception ex) {
                //log()
            }
        }

        synchronized (sender) {
            sender.notify();
        }
        sender=null;
        
        this.chan = null;
        this.reads = -1;
        this.buf = null;
        this.attachment = null;
        
        
    }

    protected abstract void onRead(T channel, int bytesRead, Buffer buffer, Object token);

    protected abstract void onFailed(Channel channel, Throwable exc);
}

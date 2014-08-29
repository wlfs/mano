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
public abstract class ChannelHandler<C extends Channel,A extends Object> implements Runnable {

    private C chan;
    private int reads;
    private ByteArrayBuffer buf;
    private A attachment;
    private Throwable error;
    private Object sender;

    final void init(Object sender, C channel, int bytesTransferred, ByteArrayBuffer buffer, Throwable exc) {
        this.sender = sender;
        this.error = exc;
        this.chan = channel;
        this.reads = bytesTransferred;
        this.buf = buffer;
    }
    
    final void attach(A attachment) {
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
                ex.printStackTrace(System.out);
            }
        }

        //synchronized (sender) {
        //    sender.notify();
        //}
        //this.sender=null;
        //this.chan = null;
        //this.reads = -1;
        //this.buf = null;
        //this.attachment = null;
    }

    protected abstract void onRead(C channel, int bytesRead, ByteArrayBuffer buffer, A attachment) throws Exception;

    protected abstract void onFailed(C channel, Throwable exc);
}

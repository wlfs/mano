/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.io.IOException;
import java.io.PrintStream;

/**
 * 封装的一个流类型日志处理程序。
 * @author jun <jun@diosay.com>
 */
public class StreamHandler extends Handler {

    private PrintStream output;

    protected PrintStream getStream() {
        return output;
    }

    protected void setStream(PrintStream stream) {
        output = stream;
    }

    @Override
    public boolean doLog(LogEntry entry) throws Exception {

        if (getStream() == null) {
            throw new NullPointerException("stream");
        } else if (this.getFormatter() == null) {
            throw new NullPointerException("formatter");
        }
        
        this.getStream().append(this.getFormatter().format(entry));
        this.getStream().flush();

        return false;
    }

    @Override
    public void close() throws IOException {
        if(this.getStream()!=null){
            this.getStream().flush();
            this.getStream().close();
        }
    }

    @Override
    public void flush() throws IOException {
        if(this.getStream()!=null){
            this.getStream().flush();
        }
    }
}

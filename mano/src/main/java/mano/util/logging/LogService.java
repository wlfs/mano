/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import mano.service.Intent;
import mano.service.Service;

/**
 * 实现的日志服务。
 * @author jun <jun@diosay.com>
 */
public class LogService extends Service {

    ConsoleHandler handler = new ConsoleHandler();

    @Override
    public void process(Intent intent) throws Exception {
        if ("log".equals(intent.getAction())) {
            LogEntry entry = (LogEntry) intent.get("entry");
            if (entry != null) {
                handler.log(entry);
            } else {
                System.out.println("entry is null");
            }
        } else {
            throw new NoSuchMethodException("bbb");
        }
    }

    @Override
    public String getServiceName() {
        return "mano.service.logging";
    }

    @Override
    public void run() {
        this.onStart();
    }

}

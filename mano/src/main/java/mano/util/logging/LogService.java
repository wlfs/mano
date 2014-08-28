/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.util.HashMap;
import java.util.HashSet;
import mano.service.Intent;
import mano.service.IntentService;

/**
 * 实现的日志服务。
 *
 * @author jun <jun@diosay.com>
 */
public class LogService extends IntentService {

    //ConsoleHandler handler = new ConsoleHandler();
    private HashSet<Handler> handlers = new HashSet<>();
    private static HashMap<String, Logger> loggers = new HashMap<>();

    static synchronized Logger getLogger(String name) {
        Logger log;
        if (loggers.containsKey(name)) {
            log = loggers.get(name);
        } else {
            log = new Logger(name);
            loggers.put(name, log);
        }
        return log;
    }

    @Override
    public void process(Intent intent) throws Exception {
        if ("log".equals(intent.getAction())) {
            LogEntry entry = (LogEntry) intent.get("entry");
            if (entry != null) {

                for(Handler handler:handlers){
                    if(handler.log(entry)){
                        break;
                    }
                }
            } else {
                
            }
        }
        else if ("addhandler".equals(intent.getAction())) {
            Handler item = (Handler) intent.get("handler");
            if (item != null && !handlers.contains(item)) {
                handlers.add(item);
            } else {
                
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

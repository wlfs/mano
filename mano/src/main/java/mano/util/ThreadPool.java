/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ThreadPool {

    private static final ExecutorService service = Executors.newCachedThreadPool();

    public static ExecutorService getService() {
        return service;
    }

    public static void execute(Runnable command) {
        service.execute(command);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return service.submit(task);
    }
}

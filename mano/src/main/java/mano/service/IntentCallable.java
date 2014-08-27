/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface IntentCallable {

    void onCompleted(Intent intent);

    void onFailed(Intent intent, Throwable ex);
}

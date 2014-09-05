/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface HttpRequestAppender {
    long getContentLength();
    String getBoundary();
    void appendPostFile(HttpPostFile file);
    void appendFormItem(String name, String value);
    void notifyDone();
}

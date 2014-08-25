/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.util.logging;

import mano.DateTime;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class LogEntry {
    private String project;
    private String category;
    private int level;
    private String content;
    private Throwable thrown;
    private String source;
    private DateTime time;
    private String callMethod;
    private String callClass;
    
    
    
}

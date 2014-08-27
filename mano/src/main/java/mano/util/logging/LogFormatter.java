/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.util.logging;

/**
 * 表示一个用于格式化日志的方法。
 * @author jun <jun@diosay.com>
 */
public interface LogFormatter {
    
    /**
     * 格式化一个日志项。
     * @param entry
     * @return 
     */
    CharSequence format(LogEntry entry);
}

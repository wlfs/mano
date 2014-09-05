/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import mano.DateTime;

/**
 * 简单日志格式化。
 * @author jun <jun@diosay.com>
 */
public class SimpleFormatter implements LogFormatter {

    @Override
    public CharSequence format(LogEntry entry) {

        //2014-12-12 00:00:00 [level] T14 com.Class.main[L14]:message
        //error
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getTime().toString(DateTime.FORMAT_ISO)).append(' ');
        sb.append('[').append(entry.getLevel().value).append(']').append(' ');
        sb.append('T').append(entry.getThreadId()).append(' ');
        if (entry.getSourceClassName() != null && !"".equals(entry.getSourceClassName().trim())) {
            sb.append(entry.getSourceClassName());
        }
        if (entry.getSourceMethodName() != null && !"".equals(entry.getSourceMethodName().trim())) {
            sb.append('.').append(entry.getSourceMethodName());
        }
        sb.append("[L").append(entry.getSourceLineNumber()).append("]:").append(entry.getMessage());

        if (entry.getThrown() != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                pw.println();
                entry.getThrown().printStackTrace(pw);
            }
            sb.append(sw.toString());
            sw = null;//help GC
        }
        sb.append(System.lineSeparator());
        return sb;
    }
}

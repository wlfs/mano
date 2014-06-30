/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class DateTime {

    private TimeZone _timeZone;
    private Locale _locale;
    private SimpleDateFormat _formatProvider;
    private Calendar _calendar;
    private static DateTime _instace;

    public static final String FORMAT_ISO = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_GMT = "EEE, d MMM yyyy HH:mm:ss 'GMT'";

    private DateTime() {
        this._locale = Locale.US;
        this._timeZone = TimeZone.getTimeZone("UTC+8");
        this._calendar = Calendar.getInstance(this._timeZone, this._locale);
        this._formatProvider = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", this._locale);
    }

    public static Calendar getCalendar() {
        return getInstace()._calendar;
    }

    public static DateTime getInstace() {
        if (_instace == null) {
            _instace = new DateTime();
        }
        return _instace;
    }

    public static String format(String pattern, Date time) {
        getInstace()._formatProvider.applyPattern(pattern);
        return getInstace()._formatProvider.format(time);
    }
    
    public static String format(String pattern, long time) {
        getInstace()._formatProvider.applyPattern(pattern);
        return getInstace()._formatProvider.format(time);
    }

    public static String format(String pattern) {
		//http://emily2ly.iteye.com/blog/742792
        //getInstace()._formatProvider.parse("").getTime()
        return format(pattern, Calendar.getInstance(getInstace()._timeZone, getInstace()._locale).getTime());
    }

    public static Date parse(String source, String pattern) throws ParseException {
        getInstace()._formatProvider.applyPattern(pattern);
        return getInstace()._formatProvider.parse(source);
    }

    public static long parseTime(String source, String pattern) throws ParseException {
        return parse(source, pattern).getTime();
    }
}

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import mano.InvalidOperationException;

/**
 * 表示时间上的一刻，通常以日期和当天的时间表示。
 * <p>
 * DateTime
 * 类型表示范围在公元（世界标准时间）1970年1月1日午夜00:00:00到公元9999年12月31日晚上11:59:59之间的日期和时代。</p>
 *
 * @author jun <jun@diosay.com>
 */
public class DateTime {

    java.time.LocalDateTime local;
    private TimeZone _timeZone;
    private Locale _locale;
    private static final SimpleDateFormat _formatProvider = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private Calendar _calendar;
    private static DateTime _instace;

    public static final String FORMAT_ISO = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_GMT = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
    /**
     * UTC计时时间戳（1970-01-01T00:00:00Z）的总毫秒数。
     */
    public static final long EPOCH_MILLISECONDS = 62168601600000L;
    /**
     * 标准定义的一天的总秒数。
     */
    public static final long DAY_SECONDS = 86400L;

    private int _year = 1970,
            _month = 1,
            _day = 1,
            _hour = 0,
            _minute = 0,
            _second = 0,
            _millisecond = 0;

    public DateTime() {
        this._timeZone = TimeZone.getTimeZone("UTC+8");
    }

    public DateTime(int year,
            int month,
            int day,
            int hour,
            int minute,
            int second,
            int millisecond) {
        if (year > 9999 || year < 1970) {
            throw new InvalidOperationException();
        }
        _year = year;
        if (month > 12 || month < 1) {
            throw new InvalidOperationException();
        }
        _month = month;
        if (day > maxDayOfMonth(year, month) || day < 1) {
            throw new InvalidOperationException();
        }
        _day = day;
        if (hour > 23 || hour < 0) {
            throw new InvalidOperationException();
        }
        _hour = hour;
        if (minute > 59 || minute < 0) {
            throw new InvalidOperationException();
        }
        _minute = minute;
        if (second > 59 || second < 0) {
            throw new InvalidOperationException();
        }
        _second = second;
        if (millisecond > 1000 || millisecond < 0) {
            throw new InvalidOperationException();
        }
        _millisecond = millisecond;
    }

    public DateTime(int year,
            int month,
            int day,
            int hour,
            int minute,
            int second) {
        this(year, month, day, hour, minute, second, 0);
    }

    public DateTime(int year,
            int month,
            int day) {
        this(year, month, day, 0, 0, 0, 0);
    }

    public DateTime(int hour,
            int minute,
            int second,
            int millisecond) {
        this(1970, 1, 1, hour, minute, second, millisecond);
    }

    public DateTime(long time) {
        this.AddMilliseconds(time);
    }

    public void AddYears(int value) {
        if (value + _year > 9999 || value + _year < 1970) {
            throw new InvalidOperationException();
        }
        _year += value;
    }

    public void AddMonths(int value) {
        if (value < 0) {
            value = Math.abs(value);
            if (value > 12) {
                AddYears(-(value / 12));
                _month -= value % 12;
            } else {
                _month -= value;
            }
        } else if (value > 12) {
            AddYears(value / 12);
            _month += value % 12;
        } else {
            _month += value;
        }

        if (_month > 12) {
            AddYears(1);
            _month -= 12;
        } else if (_month < 0) {
            AddYears(-1);
            _month += 12;
        }

    }

    public void AddDays(int value) {

        _day += value;
        while (_day < 1) {
            AddMonths(-1);
            _day += 1;
        }

        int mday = maxDayOfMonth(_year, _month);
        while (_day > mday) {
            AddMonths(1);
            _day -= mday;
            mday = maxDayOfMonth(_year, _month);
        }

    }

    public void AddHours(int value) {
        if (value < 0) {
            value = Math.abs(value);
            if (value > 23) {
                AddDays(-(value / 24));
                _hour -= value % 24;
            } else {
                _hour -= value;
            }
        } else if (value > 23) {
            AddDays(value / 24);
            _hour += value % 24;
        } else {
            _hour += value;
        }

        if (_hour > 23) {
            AddDays(1);
            _hour -= 24;
        } else if (_hour < 0) {
            AddDays(-1);
            _hour += 24;
        }

    }

    public void AddMinutes(int value) {
        if (value < 0) {
            value = Math.abs(value);
            if (value >= 60) {
                AddHours(-(value / 60));
                _minute -= value % 60;
            } else {
                _minute -= value;
            }
        } else if (value >= 60) {
            AddHours(value / 60);
            _minute += value % 60;
        } else {
            _minute += value;
        }

        if (_minute > 59) {
            AddHours(1);
            _minute -= 60;
        } else if (_minute < 0) {
            AddHours(-1);
            _minute += 60;
        }

    }

    public void AddSeconds(int value) {
        if (value < 0) {
            value = Math.abs(value);
            if (value >= 60) {
                AddMinutes(-(value / 60));
                _second -= value % 60;
            } else {
                _second -= value;
            }
        } else if (value >= 60) {
            AddMinutes(value / 60);
            _second += value % 60;
        } else {
            _second += value;
        }

        if (_second > 59) {
            AddMinutes(1);
            _second -= 60;
        } else if (_second < 0) {
            AddMinutes(-1);
            _second += 60;
        }

    }

    public void AddMilliseconds(long milliseconds) {
        if (milliseconds < 0) {
            milliseconds = Math.abs(milliseconds);
            if (milliseconds >= 1000) {
                this.AddSeconds(-((int) (milliseconds / 1000)));
                _millisecond -= (int) (milliseconds % 1000);
            } else {
                _millisecond -= milliseconds;
            }
        } else if (milliseconds >= 1000) {
            this.AddSeconds((int) (milliseconds / 1000));
            _millisecond += (int) (milliseconds % 1000);
        } else {
            _millisecond -= milliseconds;
        }

        if (_millisecond < 0) {
            this.AddSeconds(-1);
            _millisecond = 1000 - Math.abs(_millisecond);
        } else if (_millisecond >= 1000) {
            this.AddSeconds(1);
            _millisecond -= 1000;
        }

    }

    public int getYear() {
        return _year;
    }

    public int getMonth() {
        return _month;
    }

    public int getDay() {
        return _day;
    }

    public int getHour() {
        return _hour;
    }

    public int getMinute() {
        return _minute;
    }

    public int getSecond() {
        return _second;
    }

    public int getMilliSecond() {
        return _millisecond;
    }

    public long getTime() {

        int days = _day;
        for (int i = 1; i < _month; i++) {
            days += maxDayOfMonth(_year, i);
        }
        for (int j = 0; j < _year; j++) {
            if (isLeapYear(j)) {
                days += 366;
            } else {
                days += 365;
            }
        }

        long time = days * DAY_SECONDS;
        time += _hour * 3600L;
        time += _minute * 60L;
        time += _second;
        time = time * 1000L;
        time += _millisecond;
        return time - EPOCH_MILLISECONDS;
    }

    @Override
    public String toString() {
        return format(DateTime.FORMAT_ISO, this);
    }

    public String toString(String format) {
        return format(format, this);
    }

    public static boolean isLeapYear(int year) throws IllegalArgumentException {
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("year,range(0,9999)");
        }
        if (year % 100 == 0 && year % 400 == 0) { //如果可以被100整除（世纪元年），就再看是否能被400整除，能够则为闰年
            return true;
        } else if (year % 4 == 0) {
            return true;
        }
        return false;
    }

    public static int maxDayOfMonth(int year, int month) throws IllegalArgumentException {
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("year,range(0,9999)");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month,range(1,12)");
        }
        //闰年的2月是29天，非闰年为28，其它就是1,3,5,7,8,10,12为31天，其余30天。
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 2:
                if (isLeapYear(year)) {
                    return 29;
                }
                return 28;
            default:
                return 30;
        }
    }

    public static long nowTime() {
        return Instant.now().toEpochMilli();
    }

    public static DateTime now() {
        return new DateTime(nowTime());
    }

    public static DateTime localNow() {
        DateTime dt = new DateTime(nowTime() + TimeZone.getDefault().getRawOffset());
        return dt;
    }

    public static long localNowTime() {
        return Instant.now().toEpochMilli() + TimeZone.getDefault().getRawOffset();
    }

    public static String format(String pattern, DateTime time) {
        synchronized (_formatProvider) {
            _formatProvider.applyPattern(pattern);
            return _formatProvider.format(time.getTime());
        }
    }

    public static String format(SimpleDateFormat format, String pattern, long time) {
        format.applyPattern(pattern);
        return format.format(time);
    }

    public static String format(String pattern, Date time) {
        synchronized (_formatProvider) {
            return format(_formatProvider, pattern, time.getTime());
        }
    }

    public static String format(String pattern, long time) {
        synchronized (_formatProvider) {
            return format(_formatProvider, pattern, time);
        }
    }

    public static DateTime parse(String source, String pattern) throws ParseException {
        return new DateTime(parseTime(source, pattern));
    }

    public static long parseTime(String source, String pattern) throws ParseException {
        synchronized (_formatProvider) {
            _formatProvider.applyPattern(pattern);
            return _formatProvider.parse(source).getTime();
        }
    }
}

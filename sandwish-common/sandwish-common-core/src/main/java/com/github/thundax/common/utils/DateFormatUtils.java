package com.github.thundax.common.utils;

import com.github.thundax.common.utils.ext.DateFormatUtilEx;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.util.StringUtils;

/** @author thundax */
public class DateFormatUtils extends DateFormatUtilEx {

    private DateFormatUtils() {}

    public static String formatDate(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    public static String formatTime(Date date) {
        return DateFormat.getTimeInstance().format(date);
    }

    public static Date parseTime(Date time) {
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date result = time;
        try {
            result = format.parse(formatTime(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Date parseDate(Date time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date result = time;
        try {
            result = format.parse(formatDate(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /** 根据日期字符串长度判断是长日期还是短日期。只支持yyyy-MM-dd，yyyy-MM-dd HH:mm:ss两种格式。 扩展支持yyyy,yyyy-MM日期格式 */
    private static final DateFormat DF_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final DateFormat DF_SHORT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat DF_YEAR = new SimpleDateFormat("yyyy");
    private static final DateFormat DF_MONTH = new SimpleDateFormat("yyyy-MM");
    private static final int SHORT_DATE = 10;
    private static final int YEAR_DATE = 4;
    private static final int MONTH_DATE = 7;

    public static Date parseDate(String text) throws IllegalArgumentException {
        text = text.trim();
        if (!StringUtils.hasText(text)) {
            return null;
        } else {
            try {
                if (text.length() <= YEAR_DATE) {
                    return DF_YEAR.parse(text);
                } else if (text.length() <= MONTH_DATE) {
                    return DF_MONTH.parse(text);
                } else if (text.length() <= SHORT_DATE) {
                    return DF_SHORT.parse(text);
                } else {
                    return DF_LONG.parse(text);
                }
            } catch (ParseException ex) {
                IllegalArgumentException iae =
                        new IllegalArgumentException("Could not parse date: " + ex.getMessage());
                iae.initCause(ex);
                throw iae;
            }
        }
    }

    public static String format(Date date) {
        return DF_LONG.format(date);
    }
}

package com.github.thundax.common.utils;

import com.github.thundax.common.utils.ext.DateUtilEx;
import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 日期工具类, 继承org.apache.commons.lang.time.DateUtils类
 *
 * @author wdit
 */
public class DateUtils extends DateUtilEx {

    private static final String[] PARSE_PATTERNS = {
        "yyyy-MM-dd",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM",
        "yyyy/MM/dd",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm",
        "yyyy/MM",
        "yyyy.MM.dd",
        "yyyy.MM.dd HH:mm:ss",
        "yyyy.MM.dd HH:mm",
        "yyyy.MM"
    };

    /** 得到当前日期字符串 格式（yyyy-MM-dd） */
    public static String getDate() {
        return getDate("yyyy-MM-dd");
    }

    /** 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E" */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    /** 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E" */
    public static String formatDate(Date date, Object... pattern) {
        String formatDate;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    /** 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss） */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /** 得到当前时间字符串 格式（HH:mm:ss） */
    public static String getTime() {
        return formatDate(new Date(), "HH:mm:ss");
    }

    /** 得到当前日期和时间字符串 格式（yyyy-MM-dd HH:mm:ss） */
    public static String getDateTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /** 得到当前年份字符串 格式（yyyy） */
    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    /** 得到当前月份字符串 格式（MM） */
    public static String getMonth() {
        return formatDate(new Date(), "MM");
    }

    /** 得到当天字符串 格式（dd） */
    public static String getDay() {
        return formatDate(new Date(), "dd");
    }

    /** 得到当前星期字符串 格式（E）星期几 */
    public static String getWeek() {
        return formatDate(new Date(), "E");
    }

    /**
     * 日期型字符串转化为日期 格式 { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd",
     * "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd
     * HH:mm" }
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), PARSE_PATTERNS);
        } catch (ParseException e) {
            return null;
        }
    }

    /** 获取过去的天数 */
    public static long pastDays(Date date) {
        return pastDays(new Date(), date);
    }

    public static long pastDays(Date after, Date before) {
        return (after.getTime() - before.getTime()) / MILLIS_PER_DAY;
    }

    /** 获取过去的小时 */
    public static long pastHour(Date date) {
        return pastHour(new Date(), date);
    }

    public static long pastHour(Date after, Date before) {
        return (after.getTime() - before.getTime()) / MILLIS_PER_HOUR;
    }

    /** 获取过去的分钟 */
    public static long pastMinutes(Date date) {
        return pastMinutes(new Date(), date);
    }

    public static long pastMinutes(Date after, Date before) {
        return (after.getTime() - before.getTime()) / MILLIS_PER_MINUTE;
    }

    /** 获取过去的秒 */
    public static long pastSeconds(Date date) {
        return pastSeconds(new Date(), date);
    }

    public static long pastSeconds(Date after, Date before) {
        return (after.getTime() - before.getTime()) / MILLIS_PER_SECOND;
    }

    /** 转换为时间（天,时:分:秒.毫秒） */
    public static String formatDateTime(long timeMillis) {
        long day = timeMillis / (24 * 60 * 60 * 1000);
        long hour = (timeMillis / (60 * 60 * 1000) - day * 24);
        long min = ((timeMillis / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (timeMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        long sss =
                (timeMillis
                        - day * 24 * 60 * 60 * 1000
                        - hour * 60 * 60 * 1000
                        - min * 60 * 1000
                        - s * 1000);
        return (day > 0 ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
    }
}

package org.limbo.flowjob.broker.core.utils.time;

import lombok.experimental.UtilityClass;

import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author brozen
 * @since 1.0.3
 */
@UtilityClass
public class TimeUtils {

    /**
     * 将{@link LocalTime}格式化为指定格式，并格式化时进行时区转换。
     * @param time 时间
     * @param pattern 日期格式
     * @param zone 格式化前，先将时间戳转换为指定时区，再进行格式化
     * @return 时间格式化后的字符串
     */
    public static String format(LocalTime time, String pattern, ZoneId zone) {
        return Formatters.getFormatter(pattern).withZone(zone).format(time);
    }


    /**
     * 将{@link LocalTime}格式化为指定格式，并格式化时进行时区转换。
     * @param time 时间
     * @param pattern 日期格式
     * @param zone 格式化前，先将时间戳转换为指定时区，再进行格式化
     * @return 时间格式化后的字符串
     */
    public static String format(LocalTime time, String pattern, String zone) {
        return format(time, pattern, ZoneId.of(zone));
    }


    /**
     * 将{@link LocalTime}格式化为指定格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param time 时间
     * @param pattern 日期格式
     * @return 时间格式化后的字符串
     */
    public static String format(LocalTime time, String pattern) {
        return Formatters.getFormatter(pattern).format(time);
    }


    /**
     * 将{@link LocalTime}格式化为"<code>HH:mm:ss</code>"格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param time 时间
     * @return 时间格式化后的字符串
     */
    public static String formatHMS(LocalTime time) {
        return Formatters.hms().format(time);
    }


    /**
     * 将格式化日期字符串转换为{@link LocalTime}，日期字符串的时区通过参数指定。
     * @param time 格式化的日期字符串
     * @param pattern 日期格式
     * @param zone 日期字符串的时区
     * @return 日期字符串对应的时间戳
     */
    public static LocalTime parse(String time, String pattern, ZoneId zone) {
        return Formatters.getFormatter(pattern).withZone(zone).parse(time, LocalTime::from);
    }


    /**
     * 将格式化日期字符串转换为{@link LocalTime}，日期字符串的时区通过参数指定。
     * @param time 格式化的日期字符串
     * @param pattern 日期格式
     * @param zone 日期字符串的时区
     * @return 日期字符串对应的时间戳
     */
    public static LocalTime parse(String time, String pattern, String zone) {
        return parse(time, pattern, ZoneId.of(zone));
    }


    /**
     * 将格式化日期字符串转换为{@link LocalTime}，日期字符串的时区使用默认时区{@link Formatters#DEFAULT_ZONE}。
     * @param time 格式化的日期字符串
     * @param pattern 日期格式
     * @return 日期字符串对应的时间戳
     */
    public static LocalTime parse(String time, String pattern) {
        return Formatters.getFormatter(pattern).parse(time, LocalTime::from);
    }


    /**
     * 将"<code>HH:mm:ss</code>"格式的日期字符串转换为{@link LocalTime}，日期部分使用默认时区{@link Formatters#DEFAULT_ZONE}的当前时间。
     * @param time 格式化的日期字符串
     * @return 日期字符串对应的时间戳
     */
    public static LocalTime parseHMS(String time) {
        return Formatters.hms().parse(time, LocalTime::from);
    }


    /**
     * 获取时分秒均为0的时间。
     * @return 今天的开始
     */
    public static LocalTime beginning() {
        return LocalTime.of(0, 0, 0);
    }


    /**
     * 获取23时59分59秒999毫秒的时间。
     * @return 今天的结束
     */
    public static LocalTime ending() {
        return LocalTime.of(23, 59, 59, 999_999_999);
    }


}

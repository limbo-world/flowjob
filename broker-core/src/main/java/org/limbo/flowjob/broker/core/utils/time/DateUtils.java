package org.limbo.flowjob.broker.core.utils.time;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author brozen
 * @since 1.0
 */
@UtilityClass
public class DateUtils {

    /**
     * 将{@link LocalDate}格式化为指定格式，并格式化时进行时区转换。
     * @param date 时间
     * @param pattern 日期格式
     * @param zone 格式化前，先将时间戳转换为指定时区，再进行格式化
     * @return 时间格式化后的字符串
     */
    public static String format(LocalDate date, String pattern, ZoneId zone) {
        return Formatters.getFormatter(pattern).withZone(zone).format(date);
    }


    /**
     * 将{@link LocalDate}格式化为指定格式，并格式化时进行时区转换。
     * @param date 时间
     * @param pattern 日期格式
     * @param zone 格式化前，先将时间戳转换为指定时区，再进行格式化
     * @return 时间格式化后的字符串
     */
    public static String format(LocalDate date, String pattern, String zone) {
        return format(date, pattern, ZoneId.of(zone));
    }


    /**
     * 将{@link LocalDate}格式化为指定格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param date 时间
     * @param pattern 日期格式
     * @return 时间格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        return Formatters.getFormatter(pattern).format(date);
    }


    /**
     * 将{@link LocalDate}格式化为"<code>yyyy-MM-dd</code>"格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param date 时间
     * @return 时间格式化后的字符串
     */
    public static String formatYMD(LocalDate date) {
        return Formatters.ymd().format(date);
    }


    /**
     * 将格式化日期字符串转换为{@link LocalDate}，日期字符串的时区通过参数指定。
     * @param date 格式化的日期字符串
     * @param pattern 日期格式
     * @param zone 日期字符串的时区
     * @return 日期字符串对应的时间戳
     */
    public static LocalDate parse(String date, String pattern, ZoneId zone) {
        return Formatters.getFormatter(pattern).withZone(zone).parse(date, LocalDate::from);
    }


    /**
     * 将格式化日期字符串转换为{@link LocalDate}，日期字符串的时区通过参数指定。
     * @param date 格式化的日期字符串
     * @param pattern 日期格式
     * @param zone 日期字符串的时区
     * @return 日期字符串对应的时间戳
     */
    public static LocalDate parse(String date, String pattern, String zone) {
        return parse(date, pattern, ZoneId.of(zone));
    }


    /**
     * 将格式化日期字符串转换为{@link LocalDate}，日期字符串的时区使用默认时区{@link Formatters#DEFAULT_ZONE}。
     * @param date 格式化的日期字符串
     * @param pattern 日期格式
     * @return 日期字符串对应的时间戳
     */
    public static LocalDate parse(String date, String pattern) {
        return Formatters.getFormatter(pattern).parse(date, LocalDate::from);
    }


    /**
     * 将"<code>yyyy-MM-dd</code>"格式的日期字符串转换为{@link LocalDate}，日期字符串的时区使用默认时区{@link Formatters#DEFAULT_ZONE}。
     * 时分秒自动填入0。
     * @param date 格式化的日期字符串
     * @return 日期字符串对应的时间戳
     */
    public static LocalDate parseYMD(String date) {
        return Formatters.ymd().parse(date, LocalDate::from);
    }

}

/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.common.utils.time;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author brozen
 * @since 1.0
 */
@UtilityClass
public class DateTimeUtils {

    /**
     * 将{@link LocalDateTime}格式化为指定格式，并格式化时进行时区转换。
     * @param date 时间
     * @param pattern 日期格式
     * @param zone 格式化前，先将时间戳转换为指定时区，再进行格式化
     * @return 时间格式化后的字符串
     */
    public static String format(LocalDateTime date, String pattern, ZoneId zone) {
        return Formatters.getFormatter(pattern).withZone(zone).format(date);
    }


    /**
     * 将{@link LocalDateTime}格式化为指定格式，并格式化时进行时区转换。
     * @param date 时间
     * @param pattern 日期格式
     * @param zone 格式化前，先将时间戳转换为指定时区，再进行格式化
     * @return 时间格式化后的字符串
     */
    public static String format(LocalDateTime date, String pattern, String zone) {
        return format(date, pattern, ZoneId.of(zone));
    }


    /**
     * 将{@link LocalDateTime}格式化为指定格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param date 时间
     * @param pattern 日期格式
     * @return 时间格式化后的字符串
     */
    public static String format(LocalDateTime date, String pattern) {
        return Formatters.getFormatter(pattern).format(date);
    }


    /**
     * 将{@link LocalDateTime}格式化为"<code>yyyy-MM-dd HH:mm:ss</code>"格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param date 时间
     * @return 时间格式化后的字符串
     */
    public static String formatYMDHMS(LocalDateTime date) {
        return Formatters.ymdhms().format(date);
    }


    /**
     * 将{@link LocalDateTime}格式化为"<code>yyyy-MM-dd</code>"格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param date 时间
     * @return 时间格式化后的字符串
     */
    public static String formatYMD(LocalDateTime date) {
        return Formatters.ymd().format(date);
    }


    /**
     * 将{@link LocalDateTime}格式化为"<code>HH:mm:ss</code>"格式，并使用默认时区{@link Formatters#DEFAULT_ZONE}进行格式化。
     * @param date 时间戳
     * @return 时间格式化后的字符串
     */
    public static String formatHMS(LocalDateTime date) {
        return Formatters.hms().format(date);
    }


    /**
     * 将格式化日期字符串转换为{@link LocalDateTime}，日期字符串的时区通过参数指定。
     * @param date 格式化的日期字符串
     * @param pattern 日期格式
     * @param zone 日期字符串的时区
     * @return 日期字符串对应的时间戳
     */
    public static LocalDateTime parse(String date, String pattern, ZoneId zone) {
        return Formatters.getFormatter(pattern).withZone(zone).parse(date, LocalDateTime::from);
    }


    /**
     * 将格式化日期字符串转换为{@link LocalDateTime}，日期字符串的时区通过参数指定。
     * @param date 格式化的日期字符串
     * @param pattern 日期格式
     * @param zone 日期字符串的时区
     * @return 日期字符串对应的时间戳
     */
    public static LocalDateTime parse(String date, String pattern, String zone) {
        return parse(date, pattern, ZoneId.of(zone));
    }


    /**
     * 将格式化日期字符串转换为{@link LocalDateTime}，日期字符串的时区使用默认时区{@link Formatters#DEFAULT_ZONE}。
     * @param date 格式化的日期字符串
     * @param pattern 日期格式
     * @return 日期字符串对应的时间戳
     */
    public static LocalDateTime parse(String date, String pattern) {
        return Formatters.getFormatter(pattern).parse(date, LocalDateTime::from);
    }


    /**
     * 将"<code>yyyy-MM-dd HH:mm:ss</code>"格式的日期字符串转换为{@link LocalDateTime}，日期字符串的时区使用默认时区{@link Formatters#DEFAULT_ZONE}。
     * @param date 格式化的日期字符串
     * @return 日期字符串对应的时间戳
     */
    public static LocalDateTime parseYMDHMS(String date) {
        return Formatters.ymdhms().parse(date, LocalDateTime::from);
    }


    /**
     * 将"<code>yyyy-MM-dd</code>"格式的日期字符串转换为{@link LocalDateTime}，日期字符串的时区使用默认时区{@link Formatters#DEFAULT_ZONE}。
     * 时分秒自动填入0。
     * @param date 格式化的日期字符串
     * @return 日期字符串对应的时间戳
     */
    public static LocalDateTime parseYMD(String date) {
        return Formatters.ymd().parse(date, LocalDate::from).atTime(0, 0, 0);
    }


    /**
     * 将"<code>HH:mm:ss</code>"格式的日期字符串转换为{@link LocalDateTime}，日期部分使用默认时区{@link Formatters#DEFAULT_ZONE}的当前时间。
     * @param date 格式化的日期字符串
     * @return 日期字符串对应的时间戳
     */
    public static LocalDateTime parseHMS(String date) {
        return Formatters.hms().parse(date, LocalTime::from).atDate(LocalDate.now());
    }


    /**
     * 获取时间所在天的开始时间，即年月日保持不变，时分秒均为0的时间。
     * @param dateTime 时间
     * @return 入参时间当天的开始
     */
    public static LocalDateTime beginningOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atTime(0, 0, 0);
    }


    /**
     * 获取时间所在天的结束时间，即年月日保持不变，23时59分59秒999毫秒的时间。
     * @param dateTime 时间
     * @return 入参时间当天的结束
     */
    public static LocalDateTime endingOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atTime(23, 59, 59, 999_999_999);
    }


    /**
     * 获取今天的开始时间（按默认时区{@link Formatters#DEFAULT_ZONE}获取时间），即年月日保持不变，时分秒均为0的时间。
     * @return 今天的开始
     */
    public static LocalDateTime beginningOfToday() {
        return LocalDate.now(Formatters.DEFAULT_ZONE).atTime(0, 0, 0);
    }


    /**
     * 获取今天的结束时间（按默认时区{@link Formatters#DEFAULT_ZONE}获取时间），即年月日保持不变，23时59分59秒999毫秒的时间。
     * @return 今天的结束
     */
    public static LocalDateTime endingOfToday() {
        return LocalDate.now(Formatters.DEFAULT_ZONE)
                .atTime(23, 59, 59, 999_999_999);
    }


}

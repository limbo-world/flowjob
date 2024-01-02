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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author brozen
 * @since 1.0.3
 */
@UtilityClass
public class LocalTimeUtils {

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
    public static String format(LocalDateTime time, String pattern) {
        return Formatters.getFormatter(pattern).format(time);
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


}

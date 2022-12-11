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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * @author Devil
 * @since 2021/8/31
 */
public class TimeUtils {

    private static ZoneOffset zoneOffset;

    private static final Clock CLOCK = Clock.systemDefaultZone();

    public static ZoneOffset zoneOffset() {
        if (zoneOffset == null) {
            zoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now(CLOCK));
        }
        return zoneOffset;
    }

    public static Instant currentInstant() {
        return Instant.now(CLOCK);
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(zoneOffset());
    }

    public static LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now(CLOCK);
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, zoneOffset());
    }


    public static LocalDateTime toLocalDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * 获取时分秒均为0的时间。
     * @return 今天的开始
     */
    public static LocalTime beginningOfToday() {
        return LocalTime.of(0, 0, 0);
    }


    /**
     * 获取23时59分59秒999毫秒的时间。
     * @return 今天的结束
     */
    public static LocalTime endingOfToday() {
        return LocalTime.of(23, 59, 59, 999_999_999);
    }


}

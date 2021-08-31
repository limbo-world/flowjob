package org.limbo.flowjob.tracker.commons.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Devil
 * @since 2021/8/31
 */
public class TimeUtil {

    public static Instant nowInstant() {
        return Instant.now(Clock.systemDefaultZone());
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(zoneOffset());
    }

    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(Clock.systemDefaultZone());
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, zoneOffset());
    }

    private static ZoneOffset zoneOffset;


    public static ZoneOffset zoneOffset() {
        if (zoneOffset == null) {
            zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(nowInstant());
        }
        return zoneOffset;
    }

}

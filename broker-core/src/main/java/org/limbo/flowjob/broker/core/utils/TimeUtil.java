package org.limbo.flowjob.broker.core.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Devil
 * @since 2021/8/31
 */
public class TimeUtil {

    private static ZoneOffset zoneOffset;

    private static final Clock CLOCK = Clock.systemDefaultZone();

    public static ZoneOffset zoneOffset() {
        if (zoneOffset == null) {
            zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now(CLOCK));
        }
        return zoneOffset;
    }

    public static Instant nowInstant() {
        return Instant.now(CLOCK);
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(zoneOffset());
    }

    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(CLOCK);
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, zoneOffset());
    }



}

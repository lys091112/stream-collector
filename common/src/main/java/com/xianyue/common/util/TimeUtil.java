package com.xianyue.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@UtilityClass
@Slf4j
public class TimeUtil {

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String formatTime(long timeStamp) {
        if (timeStamp <= 0) {
            log.error("timestamp must be greater than zero");
            throw new IllegalArgumentException("timestamp must be greater than zero");
        }
        return timeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), TimeZone
                .getDefault().toZoneId()));
    }
}

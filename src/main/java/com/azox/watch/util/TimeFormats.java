package com.azox.watch.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeFormats {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US);

    private TimeFormats() {
    }

    public static String today() {
        return LocalDate.now(ZoneId.systemDefault()).format(DATE_FORMATTER);
    }

    public static String nowTime() {
        return LocalTime.now(ZoneId.systemDefault()).format(TIME_FORMATTER);
    }
}

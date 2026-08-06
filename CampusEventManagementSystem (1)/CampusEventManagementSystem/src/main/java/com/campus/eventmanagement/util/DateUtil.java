package com.campus.eventmanagement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Small helpers for formatting/parsing dates & times consistently
 * (used by mappers and CSV export in AdminService).
 */
public final class DateUtil {

    private DateUtil() {
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.TIME_FORMAT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        return time == null ? "" : time.format(TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    public static boolean isUpcoming(LocalDate eventDate) {
        return eventDate != null && !eventDate.isBefore(LocalDate.now());
    }
}

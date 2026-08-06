package com.campus.eventmanagement.util;

/**
 * Shared constants used across the application.
 */
public final class Constants {

    private Constants() {
        // utility class - no instances
    }

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;
    public static final String DEFAULT_SORT_FIELD = "eventDate";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
}

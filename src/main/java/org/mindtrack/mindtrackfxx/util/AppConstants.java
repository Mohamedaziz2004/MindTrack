package org.mindtrack.mindtrackfxx.util;

import java.time.format.DateTimeFormatter;

/**
 * Application-wide constants and configuration.
 * Centralizes all magic numbers and strings for easy maintenance.
 */
public final class AppConstants {

    private AppConstants() {
        // Private constructor to prevent instantiation
    }

    // ========================================
    // DATE/TIME FORMATTERS
    // ========================================

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMMM dd, yyyy - hh:mm a");

    public static final DateTimeFormatter CARD_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public static final DateTimeFormatter CARD_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("hh:mm a");

    // ========================================
    // DISPLAY LIMITS
    // ========================================

    /** Number of entries to show before "Show More" */
    public static final int INITIAL_DISPLAY_COUNT = 3;

    /** Maximum length of preview text in cards */
    public static final int PREVIEW_TEXT_LENGTH = 100;

    // ========================================
    // DEFAULT VALUES
    // ========================================

    public static final int DEFAULT_USER_ID = 1;
}

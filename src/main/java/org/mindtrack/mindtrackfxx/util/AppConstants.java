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
    // ANIMATION DURATIONS (in milliseconds)
    // ========================================

    public static final int FADE_DURATION = 300;
    public static final int SLIDE_DURATION = 400;
    public static final int SCALE_DURATION = 300;
    public static final int CARD_ENTRANCE_DELAY = 80;

    // ========================================
    // DEFAULT VALUES
    // ========================================

    public static final int DEFAULT_USER_ID = 1;
    public static final int DEFAULT_INTENSITY = 5;
    public static final String DEFAULT_MOOD = "Neutral";

    // ========================================
    // MOOD TYPES
    // ========================================

    public static final String[] MOOD_TYPES = {
        "Happy", "Calm", "Neutral", "Sad", "Anxious"
    };

    // ========================================
    // RESOURCE PATHS
    // ========================================

    public static final String STYLES_PATH = "/org/mindtrack/mindtrackfxx/styles/styles.css";
    public static final String ICONS_PATH = "/org/mindtrack/mindtrackfxx/icons/";
    public static final String EMOJIS_PATH = "/org/mindtrack/mindtrackfxx/emojis/";
    public static final String VIEWS_PATH = "/org/mindtrack/mindtrackfxx/view/";
}

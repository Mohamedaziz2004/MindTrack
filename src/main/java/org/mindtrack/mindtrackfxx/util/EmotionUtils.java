package org.mindtrack.mindtrackfxx.util;


public final class EmotionUtils {

    private EmotionUtils() {
        // Private constructor to prevent instantiation
    }

    public static String getEmotionEmoji(String emotion) {
        if (emotion == null) return "😐";

        String e = emotion.toLowerCase();
        if (e.contains("joy") || e.contains("happy")) return "😊";
        if (e.contains("sad")) return "😢";
        if (e.contains("anger") || e.contains("angry")) return "😠";
        if (e.contains("fear")) return "😨";
        if (e.contains("surprise")) return "😲";
        if (e.contains("disgust")) return "🤢";
        if (e.contains("anxiety") || e.contains("anxious")) return "😰";
        if (e.contains("stress")) return "😫";
        if (e.contains("hope")) return "🌟";
        if (e.contains("love")) return "❤️";
        if (e.contains("excite")) return "🎉";
        if (e.contains("calm")) return "😌";
        if (e.contains("neutral")) return "😐";
        return "😐";
    }

    /**
     * Gets the gradient color for an emotion.
     */
    public static String getEmotionColor(String emotion) {
        if (emotion == null) return "linear-gradient(to right, #f1f5f9, #f8fafc)";

        String e = emotion.toLowerCase();
        if (e.contains("joy") || e.contains("happy"))
            return "linear-gradient(to right, #dcfce7, #f0fdf4)";
        if (e.contains("sad"))
            return "linear-gradient(to right, #e0e7ff, #eef2ff)";
        if (e.contains("anger") || e.contains("angry"))
            return "linear-gradient(to right, #fee2e2, #fef2f2)";
        if (e.contains("fear"))
            return "linear-gradient(to right, #fef3c7, #fffbeb)";
        if (e.contains("anxiety") || e.contains("anxious"))
            return "linear-gradient(to right, #fce7f3, #fdf2f8)";
        if (e.contains("hope") || e.contains("love") || e.contains("excite"))
            return "linear-gradient(to right, #fce7f3, #fdf2f8)";
        if (e.contains("calm"))
            return "linear-gradient(to right, #dbeafe, #eff6ff)";
        return "linear-gradient(to right, #f1f5f9, #f8fafc)";
    }

    /**
     * Gets the emoji representation for a sentiment.
     */
    public static String getSentimentEmoji(String sentiment) {
        if (sentiment == null) return "😐";

        String s = sentiment.toLowerCase();
        if (s.contains("positive")) return "👍";
        if (s.contains("negative")) return "👎";
        return "😐";
    }

    /**
     * Gets the gradient color for a sentiment.
     */
    public static String getSentimentColor(String sentiment) {
        if (sentiment == null) return "linear-gradient(to right, #f1f5f9, #f8fafc)";

        String s = sentiment.toLowerCase();
        if (s.contains("positive"))
            return "linear-gradient(to right, #dcfce7, #f0fdf4)";
        if (s.contains("negative"))
            return "linear-gradient(to right, #fee2e2, #fef2f2)";
        return "linear-gradient(to right, #f1f5f9, #f8fafc)";
    }

    /**
     * Gets the CSS badge class for a mood type.
     */
    public static String getBadgeClass(String mood) {
        if (mood == null) return "badge-neutral";

        String m = mood.toLowerCase();
        if (m.contains("happy")) return "badge-happy";
        if (m.contains("calm")) return "badge-calm";
        if (m.contains("sad")) return "badge-sad";
        if (m.contains("anxious")) return "badge-anxious";
        return "badge-neutral";
    }

    /**
     * Gets the emoji file name for a mood type.
     */
    public static String getMoodEmojiFile(String mood) {
        if (mood == null) return "neutral.png";
        return mood.toLowerCase() + ".png";
    }
}

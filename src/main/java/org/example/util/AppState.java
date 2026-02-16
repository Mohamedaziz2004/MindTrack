package org.example.util;

public class AppState {
    private static int currentUserId = 1; // Default user ID

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }
}

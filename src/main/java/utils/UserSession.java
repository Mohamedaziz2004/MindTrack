package utils;

import entities.Utilisateur;

public class UserSession {

    private static Utilisateur currentUser;
    private static Utilisateur viewedUser;

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void setViewedUser(Utilisateur user) {
        viewedUser = user;
    }

    public static Utilisateur getViewedUser() {
        return viewedUser;
    }

    public static void clearViewedUser() {
        viewedUser = null;
    }

    public static void clear() {
        currentUser = null;
        viewedUser = null;
    }
}

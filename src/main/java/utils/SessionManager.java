package utils;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static SessionManager instance;

    private Integer currentUserId;
    private String currentUserName;
    private String currentUserEmail;
    private LocalDateTime loginTime;

    private String theme = "light";
    private String language = "fr";
    private final Map<String, Object> sessionData = new HashMap<>();

    private SessionManager() {
        // Mode démo par défaut
        this.currentUserId = 1;
        this.currentUserName = "Démo";
        this.currentUserEmail = "demo@mindtrack.com";
        this.loginTime = LocalDateTime.now();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void loginUser(int userId, String userName, String email) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.currentUserEmail = email;
        this.loginTime = LocalDateTime.now();
        System.out.println("✅ Utilisateur connecté: " + userName);
    }

    public void logoutUser() {
        System.out.println("👋 Déconnexion de: " + currentUserName);
        this.currentUserId = null;
        this.currentUserName = null;
        this.currentUserEmail = null;
        this.loginTime = null;
        sessionData.clear();
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    public Integer getCurrentUserId() {
        return currentUserId != null ? currentUserId : 1;
    }

    public String getCurrentUserName() {
        return currentUserName != null ? currentUserName : "Utilisateur";
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public long getSessionDuration() {
        if (loginTime == null) return 0;
        return Duration.between(loginTime, LocalDateTime.now()).toMinutes();
    }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public boolean isDarkTheme() { return "dark".equals(theme); }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public void setData(String key, Object value) { sessionData.put(key, value); }
    public Object getData(String key) { return sessionData.get(key); }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> type) {
        Object value = sessionData.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }

    public void removeData(String key) { sessionData.remove(key); }
    public void clearCache() { sessionData.clear(); }

    public String getSessionSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════\n");
        sb.append("📋 RÉSUMÉ DE SESSION\n");
        sb.append("══════════════════════════════\n");
        sb.append("👤 Utilisateur: ").append(getCurrentUserName()).append("\n");
        sb.append("🆔 ID: ").append(getCurrentUserId()).append("\n");
        sb.append("📧 Email: ").append(currentUserEmail != null ? currentUserEmail : "Non défini").append("\n");
        sb.append("🕐 Connecté depuis: ").append(loginTime != null ? loginTime : "-").append("\n");
        sb.append("⏱️ Durée: ").append(getSessionDuration()).append(" minutes\n");
        sb.append("🎨 Thème: ").append(theme).append("\n");
        sb.append("🌍 Langue: ").append(language).append("\n");
        sb.append("📦 Cache: ").append(sessionData.size()).append(" élément(s)\n");
        sb.append("══════════════════════════════\n");
        return sb.toString();
    }

    public void reset() {
        logoutUser();
        theme = "light";
        language = "fr";
        sessionData.clear();
        // Mode démo
        this.currentUserId = 1;
        this.currentUserName = "Démo";
        this.loginTime = LocalDateTime.now();
    }
}
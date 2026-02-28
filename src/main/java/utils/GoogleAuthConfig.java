package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GoogleAuthConfig {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    private GoogleAuthConfig(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public static GoogleAuthConfig of(String clientId, String clientSecret, String redirectUri) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        String redirect = redirectUri == null || redirectUri.isBlank()
                ? "http://localhost:0/"
                : redirectUri.trim();
        return new GoogleAuthConfig(clientId.trim(), blankToNull(clientSecret), redirect);
    }

    public static GoogleAuthConfig load() {
        Properties properties = new Properties();
        try (InputStream input = GoogleAuthConfig.class.getResourceAsStream("/google.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ignored) {
        }

        String clientId = firstNonBlank(
                System.getenv("GOOGLE_CLIENT_ID"),
                properties.getProperty("google.clientId")
        );
        String clientSecret = firstNonBlank(
                System.getenv("GOOGLE_CLIENT_SECRET"),
                properties.getProperty("google.clientSecret")
        );
        String redirectUri = firstNonBlank(
                System.getenv("GOOGLE_REDIRECT_URI"),
                properties.getProperty("google.redirectUri"),
                "http://localhost:0/"
        );

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google client ID not configured. Set GOOGLE_CLIENT_ID or google.clientId.");
        }

        return new GoogleAuthConfig(clientId.trim(), blankToNull(clientSecret), redirectUri.trim());
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}

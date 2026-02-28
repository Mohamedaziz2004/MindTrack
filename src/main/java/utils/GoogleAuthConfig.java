package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                ? "http://localhost:0/callback"
                : redirectUri.trim();
        return new GoogleAuthConfig(clientId.trim(), blankToNull(clientSecret), redirect);
    }

    public static GoogleAuthConfig load() {
        Properties dotEnv = loadDotEnv();

        String clientId = firstNonBlank(
                System.getenv("GOOGLE_CLIENT_ID"),
                dotEnv.getProperty("GOOGLE_CLIENT_ID"),
                dotEnv.getProperty("google.clientId")
        );
        String clientSecret = firstNonBlank(
                System.getenv("GOOGLE_CLIENT_SECRET"),
                dotEnv.getProperty("GOOGLE_CLIENT_SECRET"),
                dotEnv.getProperty("google.clientSecret")
        );
        String redirectUri = firstNonBlank(
                System.getenv("GOOGLE_REDIRECT_URI"),
                dotEnv.getProperty("GOOGLE_REDIRECT_URI"),
                dotEnv.getProperty("google.redirectUri"),
                "http://localhost:0/"
        );

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google client ID not configured. Set GOOGLE_CLIENT_ID or .env google.clientId.");
        }

        return new GoogleAuthConfig(clientId.trim(), blankToNull(clientSecret), redirectUri.trim());
    }

    private static Properties loadDotEnv() {
        Properties properties = new Properties();
        Path path = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.exists(path)) {
            return properties;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (trimmed.startsWith("export ")) {
                    trimmed = trimmed.substring("export ".length()).trim();
                }
                int equalsIndex = trimmed.indexOf('=');
                if (equalsIndex <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, equalsIndex).trim();
                String value = trimmed.substring(equalsIndex + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (!key.isEmpty()) {
                    properties.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
        }
        return properties;
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

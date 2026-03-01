package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class SmtpConfig {
    private static final Properties PROPERTIES = loadProperties();

    private SmtpConfig() {
    }

    public static String getHost() {
        return resolve("SMTP_HOST", "smtp.gmail.com");
    }

    public static int getPort() {
        String value = resolve("SMTP_PORT", "587");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 587;
        }
    }

    public static String getUsername() {
        return resolve("SMTP_USERNAME", null);
    }

    public static String getPassword() {
        return resolve("SMTP_PASSWORD", null);
    }

    public static String getFrom() {
        String from = resolve("SMTP_FROM", null);
        if (from == null || from.isBlank()) {
            return getUsername();
        }
        return from;
    }

    public static boolean isTlsEnabled() {
        String value = resolve("SMTP_TLS", "true");
        return Boolean.parseBoolean(value);
    }

    public static boolean isAuthEnabled() {
        String value = resolve("SMTP_AUTH", "true");
        return Boolean.parseBoolean(value);
    }

    private static String resolve(String key, String fallback) {
        String fromProps = normalizeValue(System.getProperty(key));
        if (fromProps != null) {
            return fromProps;
        }
        String fromEnv = normalizeValue(System.getenv(key));
        if (fromEnv != null) {
            return fromEnv;
        }
        String fromFile = normalizeValue(PROPERTIES.getProperty(key));
        if (fromFile != null) {
            return fromFile;
        }
        return fallback;
    }

    private static String normalizeValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        String lower = trimmed.toLowerCase();
        if (lower.contains("your_account") || lower.contains("your_app_password")) {
            return null;
        }
        return trimmed;
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = SmtpConfig.class.getResourceAsStream("/smtp.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // ignore
        }
        Path localFile = Path.of("smtp.properties");
        if (Files.exists(localFile)) {
            try (InputStream in = Files.newInputStream(localFile)) {
                props.load(in);
            } catch (IOException ignored) {
                // ignore
            }
        }
        return props;
    }
}

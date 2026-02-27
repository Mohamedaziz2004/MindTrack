package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ComprefaceConfig {
    private static final String DEFAULT_BASE_URL = "http://localhost:8000";
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.90;
    private static final Properties PROPERTIES = loadProperties();

    private ComprefaceConfig() {
    }

    public static String getBaseUrl() {
        String fromProps = System.getProperty("COMPRE_FACE_BASE_URL");
        if (fromProps != null && !fromProps.isBlank()) {
            return fromProps.trim();
        }
        String fromEnv = System.getenv("COMPRE_FACE_BASE_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromFile = PROPERTIES.getProperty("COMPRE_FACE_BASE_URL");
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }
        return DEFAULT_BASE_URL;
    }

    public static String getApiKey() {
        String fromProps = System.getProperty("COMPRE_FACE_API_KEY");
        if (fromProps != null && !fromProps.isBlank()) {
            return fromProps.trim();
        }
        String fromEnv = System.getenv("COMPRE_FACE_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromFile = PROPERTIES.getProperty("COMPRE_FACE_API_KEY");
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }
        return null;
    }

    public static double getSimilarityThreshold() {
        String fromProps = System.getProperty("COMPRE_FACE_SIMILARITY_THRESHOLD");
        if (fromProps != null && !fromProps.isBlank()) {
            return parseDoubleOrDefault(fromProps.trim());
        }
        String fromEnv = System.getenv("COMPRE_FACE_SIMILARITY_THRESHOLD");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return parseDoubleOrDefault(fromEnv.trim());
        }
        String fromFile = PROPERTIES.getProperty("COMPRE_FACE_SIMILARITY_THRESHOLD");
        if (fromFile != null && !fromFile.isBlank()) {
            return parseDoubleOrDefault(fromFile.trim());
        }
        return DEFAULT_SIMILARITY_THRESHOLD;
    }

    private static double parseDoubleOrDefault(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return DEFAULT_SIMILARITY_THRESHOLD;
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = ComprefaceConfig.class.getResourceAsStream("/compreface.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // ignore
        }
        Path localFile = Path.of("compreface.properties");
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

package utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {
    private static final int WORK_FACTOR = 12;

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(String rawPassword, String hashed) {
        if (rawPassword == null || hashed == null || hashed.isBlank()) {
            return false;
        }
        if (!isBcryptHash(hashed)) {
            return rawPassword.equals(hashed);
        }
        return BCrypt.checkpw(rawPassword, hashed);
    }

    public static boolean isBcryptHash(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}


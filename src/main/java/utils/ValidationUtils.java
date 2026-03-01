package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-ZÀ-ÿ\\s-]{2,50}$");

    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && !phone.trim().isEmpty() && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean hasMinLength(String value, int minLength) {
        return value != null && value.trim().length() >= minLength;
    }

    public static boolean hasMaxLength(String value, int maxLength) {
        return value != null && value.trim().length() <= maxLength;
    }

    public static boolean hasLengthBetween(String value, int min, int max) {
        if (value == null) return false;
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    public static boolean isPositiveInteger(Integer value) {
        return value != null && value > 0;
    }

    public static boolean isIntegerBetween(Integer value, int min, int max) {
        return value != null && value >= min && value <= max;
    }

    public static boolean isValidPercentage(Integer value) {
        return value == null || (value >= 0 && value <= 100);
    }

    public static boolean isValidRating(Integer value) {
        return value == null || (value >= 1 && value <= 10);
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidDifficulte(String difficulte) {
        return difficulte != null &&
                (difficulte.equals("Débutant") || difficulte.equals("Intermédiaire") ||
                        difficulte.equals("Avancé") || difficulte.equals("Expert"));
    }

    public static boolean isValidExerciceType(String type) {
        return isNotEmpty(type) && hasMaxLength(type, 50);
    }

    public static boolean isValidDescription(String description) {
        return description == null || hasMaxLength(description, 500);
    }

    public static boolean isValidSessionResult(String result) {
        return result == null || hasMaxLength(result, 200);
    }

    public static boolean isValidCommentaires(String commentaires) {
        return commentaires == null || hasMaxLength(commentaires, 500);
    }

    public static ValidationResult validateExercice(String nom, String type, Integer duree,
                                                    String difficulte, String description) {
        ValidationResult result = new ValidationResult();

        if (!isValidName(nom)) {
            result.addError("nom", "Le nom doit contenir 2-50 caractères (lettres uniquement)");
        }

        if (!isValidExerciceType(type)) {
            result.addError("type", "Le type doit contenir 1-50 caractères");
        }

        if (!isPositiveInteger(duree)) {
            result.addError("duree", "La durée doit être positive");
        } else if (duree > 480) {
            result.addError("duree", "La durée ne peut pas dépasser 480 minutes");
        }

        if (!isValidDifficulte(difficulte)) {
            result.addError("difficulte", "La difficulté doit être Débutant, Intermédiaire, Avancé ou Expert");
        }

        if (!isValidDescription(description)) {
            result.addError("description", "La description ne peut pas dépasser 500 caractères");
        }

        return result;
    }

    public static class ValidationResult {
        private final Map<String, String> errors = new HashMap<>();

        public void addError(String field, String message) {
            errors.put(field, message);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("\n", errors.values());
        }

        @Override
        public String toString() {
            return getErrorMessage();
        }
    }
}
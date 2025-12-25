package org.example.project.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtil() {}

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    public static void requireMaxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(fieldName + " must be at most " + max + " characters.");
        }
    }

    public static void requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be ≥ 0.");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0.");
        }
    }

    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0.");
        }
    }

    public static void requireValidEmail(String email, String fieldName) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid " + fieldName + ".");
        }
    }

    public static void requireNotFuture(LocalDate date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " cannot be in the future.");
        }
    }

    public static void requireOrder(LocalDate start, LocalDate end,
                                    String startName, String endName) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(startName + " and " + endName + " are required.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(endName + " cannot be before " + startName + ".");
        }
    }
}

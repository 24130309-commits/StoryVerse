package com.example.storyverse.utils;

public final class ValidationUtils {

    private ValidationUtils() {
        // Prevent creating object
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }

        return android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
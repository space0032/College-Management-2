package com.college.utils;

import java.util.regex.Pattern;

/**
 * Shared validation rules for warden records and auto-generated accounts.
 */
public final class WardenValidation {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s-]{7,15}$");

    private WardenValidation() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** Blank phone is allowed; otherwise it must be a plausible number. */
    public static boolean isValidPhone(String phone) {
        return isBlank(phone) || PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static String trimToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
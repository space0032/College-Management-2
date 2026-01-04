package com.college.utils;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 * Provides methods to validate email, phone, and other inputs
 */
public class ValidationUtils {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Phone validation pattern (10 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    /**
     * Validate email format
     * 
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number (10 digits)
     * 
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Check if string is not null or empty
     * 
     * @param value String to check
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate if string is a valid integer
     * 
     * @param value String to validate
     * @return true if valid integer, false otherwise
     */
    public static boolean isValidInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate if string is a valid decimal number
     * 
     * @param value String to validate
     * @return true if valid decimal, false otherwise
     */
    public static boolean isValidDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate marks (0-100)
     * 
     * @param marks Marks to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidMarks(double marks) {
        return marks >= 0 && marks <= 100;
    }

    /**
     * Calculate grade from marks
     * 
     * @param marks Marks obtained
     * @return Grade (A+, A, B+, B, C, D, F)
     */
    public static String calculateGrade(double marks) {
        if (marks >= 90)
            return "A+";
        else if (marks >= 80)
            return "A";
        else if (marks >= 70)
            return "B+";
        else if (marks >= 60)
            return "B";
        else if (marks >= 50)
            return "C";
        else if (marks >= 40)
            return "D";
        else
            return "F";
    }

    /**
     * Hash password using SHA-256
     * 
     * @param password Plain text password
     * @return Hashed password
     */
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate random 4-digit number
     * 
     * @return Random 4-digit string
     */
    public static String generateRandom4Digits() {
        return String.format("%04d", (int) (Math.random() * 10000));
    }
}

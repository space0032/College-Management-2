package com.college.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for improved security
 */
public class PasswordUtils {

    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    private static final String ALGORITHM = "SHA-256";

    /**
     * Hash a password with a randomly generated salt
     * 
     * @param password Plain text password
     * @return Hashed password in format: salt:hash
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash password with salt
            String hash = hashWithSalt(password, salt);

            // Return salt:hash format
            return Base64.getEncoder().encodeToString(salt) + ":" + hash;
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Hash a password for legacy compatibility (no salt)
     * Used for existing passwords in the database
     * 
     * @param password Plain text password
     * @return Hashed password without salt
     * @deprecated Use hashPassword(String) for new passwords
     */
    @Deprecated
    public static String hashPasswordLegacy(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a password against a stored hash
     * Supports both salted and legacy (unsalted) hashes
     * 
     * @param password    Plain text password
     * @param storedHash  Stored hash (either "salt:hash" or just "hash")
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Check if it's a salted hash (contains ":")
            if (storedHash.contains(":")) {
                String[] parts = storedHash.split(":", 2);
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                String expectedHash = parts[1];
                String actualHash = hashWithSalt(password, salt);
                return expectedHash.equals(actualHash);
            } else {
                // Legacy hash (no salt)
                String actualHash = hashPasswordLegacy(password);
                return storedHash.equals(actualHash);
            }
        } catch (Exception e) {
            Logger.error("Error verifying password", e);
            return false;
        }
    }

    /**
     * Hash password with a given salt
     */
    private static String hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(salt);
        byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Convert byte array to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

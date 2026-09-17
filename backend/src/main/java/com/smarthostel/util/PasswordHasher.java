package com.smarthostel.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * ============================================================================
 * PASSWORD HASHER UTILITY (Security & Hashing Layer)
 * ============================================================================
 * 
 * Provides BCrypt-based hashing and verification to ensure student passwords
 * are never stored in plain text.
 */
public final class PasswordHasher {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    private PasswordHasher() {
        // Prevent instantiation
    }

    /**
     * Hashes a plain text password using BCrypt with a secure salt.
     * 
     * @param rawPassword Plain text password
     * @return Hashed string
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * Verifies if a raw password matches the stored BCrypt hash.
     * 
     * @param rawPassword Plain text password
     * @param hashedPassword Stored BCrypt hash
     * @return true if password matches, false otherwise
     */
    public static boolean verify(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, hashedPassword);
    }
}

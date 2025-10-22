package org.example.Utilities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    // Length of the salt (in bytes)
    private static final int SALT_LENGTH = 16;

    // Generate a random salt as Base64 string
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Hash password + salt using SHA-256
    public static String hashPassword(String password, String salt) {
        try {
            String combined = password + salt;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Compare hashed input password with stored hash
    public static boolean verifyPassword(String inputPassword, String storedSalt, String storedHash) {
        String hashedInput = hashPassword(inputPassword, storedSalt);
        return hashedInput.equals(storedHash);
    }
}

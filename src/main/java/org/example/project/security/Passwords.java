package org.example.project.security;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class Passwords {
    private static final SecureRandom RNG = new SecureRandom();

    public static String randomSaltHex() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return toHex(salt);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(input.getBytes("UTF-8"));
            return toHex(dig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashWithSalt(String plain, String saltHex) {
        return sha256Hex(plain + ":" + saltHex);
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}

package com.example.kaliqwi.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Утилита для хэширования паролей.
 * Формат хранения: "salt:hash" (hex).
 */
public class PasswordUtils {

    private static final int SALT_LENGTH = 16;

    /** Создаёт новый хэш из пароля (с генерацией соли) */
    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        String saltHex = bytesToHex(salt);
        String hashHex = sha256(saltHex + password);
        return saltHex + ":" + hashHex;
    }

    /** Проверяет, соответствует ли пароль сохранённому хэшу */
    public static boolean verify(String password, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":", 2);
        if (parts.length != 2) return false;
        String saltHex = parts[0];
        String expected = parts[1];
        String actual = sha256(saltHex + password);
        return expected.equals(actual);
    }

    /** Обратная совместимость: если в БД лежит старый пароль без ":", просто сравниваем */
    public static boolean matches(String password, String stored) {
        if (stored == null) return false;
        if (stored.contains(":")) return verify(password, stored);
        return stored.equals(password); // legacy
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
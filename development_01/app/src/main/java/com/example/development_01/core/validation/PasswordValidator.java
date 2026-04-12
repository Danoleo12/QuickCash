package com.example.development_01.core.validation;

public class PasswordValidator {
    private static final int MIN_LENGTH = 6;

    public static boolean isStrong(String password) {
        if (isEmpty(password) || password.length() < MIN_LENGTH) return false;
        return hasLetter(password) && hasDigit(password) && hasSpecial(password);
    }

    public static boolean isEmpty(String password) {
        return password == null || password.trim().isEmpty();
    }

    private static boolean hasLetter(String pw) {
        return pw.chars().anyMatch(Character::isLetter);
    }

    private static boolean hasDigit(String pw) {
        return pw.chars().anyMatch(Character::isDigit);
    }

    private static boolean hasSpecial(String pw) {
        return pw.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }
}
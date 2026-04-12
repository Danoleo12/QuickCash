package com.example.development_01.core.validation;

public class RoleValidator {
    public static boolean isValid(String role) {
        if (role == null) return false;
        String r = role.trim();
        return r.equalsIgnoreCase("Employer") || r.equalsIgnoreCase("Employee");
    }

    public static boolean isEmpty(String role) {
        return role == null || role.trim().isEmpty();
    }
}
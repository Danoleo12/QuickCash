package com.example.development_01.core.validation;

public class EmailValidator {
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)?[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2})?$";

    public static boolean isValid(String email) {
        return email != null && !email.trim().isEmpty() && email.matches(EMAIL_REGEX);
    }

    public static boolean isEmpty(String email) {
        return email == null || email.trim().isEmpty();
    }
}

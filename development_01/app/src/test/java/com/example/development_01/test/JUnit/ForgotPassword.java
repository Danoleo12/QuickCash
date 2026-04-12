package com.example.development_01.test.JUnit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.development_01.core.validation.EmailValidator;

/**
 * Pure JUnit tests for Forgot Password logic (no Robolectric, no Android UI).
 */
public class ForgotPassword {

    private ForgotPasswordLogic logic;
    private FakeResetSender sender;
    private FakeUserDirectory directory;

    private static final String EXISTING_EMAIL = "hulk@email.com";
    private static final String NON_EXISTING_EMAIL = "nope@email.com";

    // --- Fakes (unchanged) ---
    private static class FakeResetSender implements PasswordResetSender {
        int sendCalls = 0;
        String lastEmail = null;

        @Override
        public boolean sendPasswordResetEmail(String email) {
            sendCalls++;
            lastEmail = email;
            return true;
        }
    }

    private static class FakeUserDirectory implements UserDirectory {
        private final java.util.Set<String> existing = new java.util.HashSet<>();

        FakeUserDirectory withExistingEmail(String email) {
            existing.add(email);
            return this;
        }

        @Override
        public boolean emailExists(String email) {
            return existing.contains(email);
        }
    }

    @Before
    public void setUp() {
        sender = new FakeResetSender();
        directory = new FakeUserDirectory().withExistingEmail(EXISTING_EMAIL);
        logic = new ForgotPasswordLogic(sender, directory);
    }

    // --- AC4 / local validation: empty input ---
    @Test
    public void requestReset_returnsFalse_whenEmailNull() {
        assertFalse(logic.requestReset(null));
        assertEquals(0, sender.sendCalls);
    }

    @Test
    public void requestReset_returnsFalse_whenEmailEmptyOrBlank() {
        assertFalse(logic.requestReset(""));
        assertFalse(logic.requestReset("   "));
        assertEquals(0, sender.sendCalls);
    }

    // --- AC4 / local validation: invalid format ---
    @Test
    public void requestReset_returnsFalse_whenEmailInvalidFormat() {
        assertFalse(logic.requestReset("not-an-email"));
        assertEquals(0, sender.sendCalls);
    }

    // --- AC4: non-existent email ---
    @Test
    public void requestReset_returnsFalse_whenEmailDoesNotExist() {
        assertFalse(logic.requestReset(NON_EXISTING_EMAIL));
        assertEquals(0, sender.sendCalls);
    }

    // --- AC3: valid email triggers reset ---
    @Test
    public void requestReset_returnsTrue_andCallsSender_whenEmailValidAndExists() {
        assertTrue(logic.requestReset(EXISTING_EMAIL));
        assertEquals(1, sender.sendCalls);
        assertEquals(EXISTING_EMAIL, sender.lastEmail);
    }

    // --- Interfaces (unchanged) ---
    interface PasswordResetSender {
        boolean sendPasswordResetEmail(String email);
    }

    interface UserDirectory {
        boolean emailExists(String email);
    }

    // --- Updated Logic (uses EmailValidator.isValid()) ---
    static class ForgotPasswordLogic {
        private final PasswordResetSender sender;
        private final UserDirectory directory;

        ForgotPasswordLogic(PasswordResetSender sender, UserDirectory directory) {
            this.sender = sender;
            this.directory = directory;
        }

        boolean requestReset(String email) {
            if (email == null) return false;
            String e = email.trim();
            if (e.isEmpty()) return false;

            // YOUR CHANGE: CredentialValidator regex → EmailValidator.isValid()
            if (!EmailValidator.isValid(e)) return false;

            // Optional AC4: non-existent email check
            if (directory != null && !directory.emailExists(e)) return false;

            return sender.sendPasswordResetEmail(e);
        }
    }
}

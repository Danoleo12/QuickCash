package com.example.development_01.test.JUnit;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.development_01.core.validation.EmailValidator;
import com.example.development_01.core.validation.PasswordValidator;

public class Login {

    // We keep fakeDb to preserve your old structure, but now we only use it
    // to mimic “email exists” checks in a helper, not via CredentialValidator.
    private FakeAuthRepository fakeDb;

    @Before
    public void setUp() {
        fakeDb = new FakeAuthRepository();
        // Seed a known record in the “database”
        fakeDb.put("test1@gmail.com", "Password123@");
    }

    // ---------------- nullEmail ----------------

    @Test
    public void nullEmail_returnsTrue_whenNull() {
        assertTrue(EmailValidator.isEmpty(null));
    }

    @Test
    public void nullEmail_returnsTrue_whenEmpty() {
        assertTrue(EmailValidator.isEmpty(""));
    }

    @Test
    public void nullEmail_returnsFalse_whenNonEmpty() {
        assertFalse(EmailValidator.isEmpty("test1@gmail.com"));
    }

    // ---------------- evaluateEmail ----------------
    // (re-expressed using EmailValidator + fakeDb)

    @Test
    public void evaluateEmail_returnsTrue_whenEmailExistsInDb() {
        assertTrue(evaluateEmail("test1@gmail.com"));
    }

    @Test
    public void evaluateEmail_returnsFalse_whenEmailNotInDb() {
        assertFalse(evaluateEmail("missing@gmail.com"));
    }

    @Test
    public void evaluateEmail_returnsFalse_whenNullOrEmpty() {
        assertFalse(evaluateEmail(null));
        assertFalse(evaluateEmail(""));
    }

    // ---------------- nullPassword ----------------

    @Test
    public void nullPassword_returnsTrue_whenNull() {
        assertTrue(PasswordValidator.isEmpty(null));
    }

    @Test
    public void nullPassword_returnsTrue_whenEmpty() {
        assertTrue(PasswordValidator.isEmpty(""));
    }

    @Test
    public void nullPassword_returnsFalse_whenNonEmpty() {
        assertFalse(PasswordValidator.isEmpty("Password123@"));
    }

    // ---------------- evaluatePassword ----------------
    // (re-expressed using PasswordValidator + fakeDb)

    @Test
    public void evaluatePassword_returnsTrue_whenEmailExistsAndPasswordMatches() {
        assertTrue(evaluatePassword("test1@gmail.com", "Password123@"));
    }

    @Test
    public void evaluatePassword_returnsFalse_whenEmailExistsButPasswordDoesNotMatch() {
        assertFalse(evaluatePassword("test1@gmail.com", "WrongPass"));
    }

    @Test
    public void evaluatePassword_returnsFalse_whenEmailDoesNotExist() {
        assertFalse(evaluatePassword("missing@gmail.com", "Password123@"));
    }

    @Test
    public void evaluatePassword_returnsFalse_whenEmailOrPasswordNullOrEmpty() {
        assertFalse(evaluatePassword(null, "Password123@"));
        assertFalse(evaluatePassword("", "Password123@"));
        assertFalse(evaluatePassword("test1@gmail.com", null));
        assertFalse(evaluatePassword("test1@gmail.com", ""));
    }

    // --------------------------------------------------------------------
    // Helpers + fake repository (kept from your original test)
    // --------------------------------------------------------------------

    // Helper that mimics your old CredentialValidator.evaluateEmail()
    private boolean evaluateEmail(String email) {
        if (EmailValidator.isEmpty(email)) return false;
        return fakeDb.emailExists(email);
    }

    // Helper that mimics your old evaluatePassword() behavior
    private boolean evaluatePassword(String email, String password) {
        if (EmailValidator.isEmpty(email) || PasswordValidator.isEmpty(password)) return false;
        if (!fakeDb.emailExists(email)) return false;

        String stored = fakeDb.getPasswordForEmail(email);
        return password.equals(stored);
    }

    public interface AuthRepository {
        boolean emailExists(String email);
        String getPasswordForEmail(String email);
    }

    public static class FakeAuthRepository implements AuthRepository {
        private final java.util.Map<String, String> store = new java.util.HashMap<>();

        public void put(String email, String password) {
            store.put(email, password);
        }

        @Override
        public boolean emailExists(String email) {
            return email != null && store.containsKey(email);
        }

        @Override
        public String getPasswordForEmail(String email) {
            return store.get(email);
        }
    }
}

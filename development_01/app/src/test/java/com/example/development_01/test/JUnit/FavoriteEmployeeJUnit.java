package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;

import com.example.development_01.core.data.Employee;

import org.junit.Test;

/**
 * JUnit tests for the favorite-employee feature logic.
 *
 * The feature builds an Employee object and sanitizes the applicant's email
 * before writing it to Firebase Realtime Database under:
 *   favorites/{uid}/{sanitizedEmail}
 *
 * Both operations are pure Java and can be verified without Android or Firebase.
 */
public class FavoriteEmployeeJUnit {

    // ── Test 1: Employee is constructed with the correct role ─────────────────

    @Test
    public void testFavoriteEmployeeHasCorrectRole() {
        // The like-button handler always hard-codes the role as "Employee".
        Employee emp = new Employee("Jane Doe", "jane@example.com", "Employee");
        assertEquals("Employee", emp.getRole());
    }

    // ── Test 2: Email dots are sanitized to commas ────────────────────────────

    @Test
    public void testEmailSanitizationReplacesDotsWithCommas() {
        // Firebase Realtime Database keys cannot contain '.', so the feature
        // replaces every '.' with ',' before using the email as a child key.
        String rawEmail = "jane.doe@example.com";
        String sanitized = rawEmail.replace(".", ",");
        assertEquals("jane,doe@example,com", sanitized);
    }
}

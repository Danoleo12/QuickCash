package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;

import com.example.development_01.core.core.HiredEmployee;

import org.junit.Test;

/**
 * JUnit tests for the HiredEmployee model.
 *
 * HiredEmployee is an immutable value object — its only behaviour is
 * storing 6 fields in its constructor and exposing them through getters.
 * These tests confirm that every field is stored and retrieved correctly,
 * including edge cases like $0.00 pay and very large pay values.
 */
public class HiredEmployeeJUnitTest {

    // ─ Helpers ───────────────────────────────────────────────────────────────

    /** Builds a standard HiredEmployee used across most tests. */
    private HiredEmployee buildEmployee() {
        return new HiredEmployee(
                "Alice Johnson",
                "alice@example.com",
                "Software Developer",
                "Halifax, NS",
                25.50,
                "job_001"
        );
    }

    // ── AC: Constructor stores all fields ─────────────────────────────────────

    @Test
    public void testGetNameReturnsConstructorValue() {
        // Confirms getName() returns exactly the String passed to the constructor.
        HiredEmployee emp = buildEmployee();
        assertEquals("Alice Johnson", emp.getName());
    }

    @Test
    public void testGetEmailReturnsConstructorValue() {
        // Confirms getEmail() is not confused with getName() or any other String field.
        HiredEmployee emp = buildEmployee();
        assertEquals("alice@example.com", emp.getEmail());
    }

    @Test
    public void testGetJobTitleReturnsConstructorValue() {
        HiredEmployee emp = buildEmployee();
        assertEquals("Software Developer", emp.getJobTitle());
    }

    @Test
    public void testGetLocationReturnsConstructorValue() {
        HiredEmployee emp = buildEmployee();
        assertEquals("Halifax, NS", emp.getLocation());
    }

    @Test
    public void testGetJobPayReturnsConstructorValue() {
        // assertEquals for doubles requires a delta (tolerance) — 0.001 is standard.
        HiredEmployee emp = buildEmployee();
        assertEquals(25.50, emp.getJobPay(), 0.001);
    }

    @Test
    public void testGetJobIdReturnsConstructorValue() {
        HiredEmployee emp = buildEmployee();
        assertEquals("job_001", emp.getJobId());
    }

    // ── AC: Edge-case pay values ──────────────────────────────────────────────

    @Test
    public void testZeroPayIsStoredCorrectly() {
        // The repository defaults pay to 0.0 when Firestore has no "pay" field.
        // This test ensures 0.0 is stored and not silently altered.
        HiredEmployee emp = new HiredEmployee("Bob", "bob@x.com", "Intern",
                "Dartmouth, NS", 0.0, "job_002");
        assertEquals(0.0, emp.getJobPay(), 0.001);
    }

    @Test
    public void testLargePayValueIsStoredCorrectly() {
        // Confirms that large double values (e.g. senior salaries) are not truncated.
        HiredEmployee emp = new HiredEmployee("Carol", "carol@x.com", "CTO",
                "Bedford, NS", 999999.99, "job_003");
        assertEquals(999999.99, emp.getJobPay(), 0.001);
    }

    // ── AC: Two employees with same name have independent fields ──────────────

    @Test
    public void testTwoEmployeesAreIndependent() {
        // Regression guard: verifies there is no static or shared mutable state
        // between two separate HiredEmployee instances.
        HiredEmployee emp1 = new HiredEmployee("Alice", "a@x.com", "Dev",
                "Halifax", 20.0, "j1");
        HiredEmployee emp2 = new HiredEmployee("Alice", "b@x.com", "QA",
                "Dartmouth", 18.0, "j2");

        // Same name — but email, jobTitle, pay, and jobId must differ
        assertEquals("a@x.com", emp1.getEmail());
        assertEquals("b@x.com", emp2.getEmail());
        assertEquals("Dev", emp1.getJobTitle());
        assertEquals("QA",  emp2.getJobTitle());
        assertEquals(20.0,  emp1.getJobPay(), 0.001);
        assertEquals(18.0,  emp2.getJobPay(), 0.001);
        assertEquals("j1",  emp1.getJobId());
        assertEquals("j2",  emp2.getJobId());
    }
}

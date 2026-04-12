package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;

import com.example.development_01.core.data.Employee;

import org.junit.Test;

public class RatingJUnitTests {
    @Test
    public void testInitialRating() {
        Employee employee = new Employee("John", "john@test.com", "Employee");
        assertEquals(0.0, employee.getRating(), 0.001);
        assertEquals(0, employee.getNumRatings());
    }

    @Test
    public void testAddFirstRating() {
        Employee employee = new Employee("John", "john@test.com", "Employee");
        employee.addRating(4.0);
        assertEquals(4.0, employee.getRating(), 0.001);
        assertEquals(1, employee.getNumRatings());
    }

    @Test
    public void testAverageRating() {
        Employee employee = new Employee("John", "john@test.com", "Employee");
        employee.addRating(5.0);
        employee.addRating(3.0);
        // (5 + 3) / 2 = 4.0
        assertEquals(4.0, employee.getRating(), 0.001);
        assertEquals(2, employee.getNumRatings());

        employee.addRating(1.0);
        // (4.0 * 2 + 1) / 3 = 9 / 3 = 3.0
        assertEquals(3.0, employee.getRating(), 0.001);
        assertEquals(3, employee.getNumRatings());
    }
}

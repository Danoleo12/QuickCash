package com.example.development_01.test;
import static org.junit.Assert.assertEquals;

import com.example.development_01.core.core.RoleRouter;

import org.junit.Before;
import org.junit.Test;



public class DashBoard {
    private RoleRouter router;

    @Before
    public void setUp() {
        router = new RoleRouter();
    }
    @Test
    public void testEmployeeEmailReturnsEmployeeRole(){
        assertEquals("Employee", router.determineRole("employee@dal.ca"));
    }

    @Test
    public void testEmployerEmailReturnsEmployerRole(){
        assertEquals("Employer", router.determineRole("employer@dal.ca"));
    }

    @Test
    public void testInvalidEmailReturnsInvalidEmail(){
        assertEquals("Invalid Email", router.determineRole("Invalid Email"));
    }

    @Test
    public void testEmptyEmail(){
        assertEquals("Empty Email", router.determineRole(""));
    }
}

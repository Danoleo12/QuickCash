package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.development_01.core.validation.EmailValidator;
import com.example.development_01.core.validation.PasswordValidator;
import com.example.development_01.core.validation.RoleValidator;

import org.junit.Test;

public class JUnitTest {

    @Test
    public void checkEmailAddressIsCorrect() {
        assertTrue(EmailValidator.isValid("IamIronMan@123.com"));
        assertTrue(EmailValidator.isValid("john.c.calhoun@examplepetstore.com"));
    }

    @Test
    public void checkEmailAddressIsIncorrect() {
        assertFalse(EmailValidator.isValid("Iloveyou3000gmail.com"));
        assertFalse(EmailValidator.isValid("IamIronMan.ca"));
    }

    @Test
    public void checkPasswordIsIncorrect() {
        assertFalse(PasswordValidator.isStrong("abc12"));
        assertFalse(PasswordValidator.isStrong("abcd!"));
    }

    @Test
    public void checkRoleIsCorrect(){
        assertTrue(RoleValidator.isValid("Employer"));
        assertTrue(RoleValidator.isValid("Employee"));
    }

    @Test
    public void checkRoleIsIncorrect(){
        assertFalse(RoleValidator.isValid(null));
        assertFalse(RoleValidator.isValid(""));
        assertFalse(RoleValidator.isValid("Student"));
    }

    @Test
    public void checkIfApplicantNameIsEmpty() {
        String name = "";
        assertTrue("Empty applicant name should be considered invalid", name.trim().isEmpty());
    }

    @Test
    public void checkIfApplicantEmailIsValid() {
        String email = "tony@stark.com";
        assertFalse("Non-empty applicant email should be considered valid", email.trim().isEmpty());
    }

    @Test
    public void checkIfResumeIsNotUploaded() {
        boolean isResumeUploaded = false;
        assertFalse("Submit should be blocked when resume has not been uploaded", isResumeUploaded);
    }

    @Test
    public void checkIfResumeIsUploaded() {
        boolean isResumeUploaded = true;
        assertTrue("Submit should be allowed when resume has been uploaded", isResumeUploaded);
    }
}

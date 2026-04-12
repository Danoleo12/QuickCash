package com.example.development_01.androidTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginUiTest {

    private static final long TIMEOUT_MS = 10_000;

    private UiDevice device;
    private String targetPackage;


    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        targetPackage = context.getPackageName();

        // Launch app from home screen (cold start)
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(targetPackage);
        assertNotNull("Launch intent was null. Check your targetPackage/app install.", intent);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Wait for app to appear
        device.wait(Until.hasObject(By.pkg(targetPackage).depth(0)), TIMEOUT_MS);
    }

    @Test
    public void loginFlow_validCredentials_showsValidCredentialWithUsername() {
        // 1) From bootup (MainActivity), find "SIGN IN" and click it
        UiObject2 signInBtn = device.wait(Until.findObject(By.res(targetPackage, "signinBtn")), TIMEOUT_MS);
        assertNotNull("Could not find SIGN IN button on MainActivity", signInBtn);
        signInBtn.click();

        // 2) Verify we're at LoginActivity by finding "Log In" button
        UiObject2 logInBtn = device.wait(Until.findObject(By.text("Log in")), TIMEOUT_MS);
        assertNotNull("Did not navigate to LoginActivity (Log In button not found)", logInBtn);

        // 3) Find Email input labeled "Email" and enter valid email
        UiObject2 emailField = device.wait(Until.findObject(By.hint("Email")), 1_000);
        if (emailField == null) emailField = findInputFieldForLabel("Email");
        assertNotNull("Email input field not found (label/hint: Email)", emailField);
        emailField.click();
        emailField.setText("tester1@employee.com");

        // 4) Find Password input labeled "Password" and enter valid password
        UiObject2 passwordField = device.wait(Until.findObject(By.hint("Password")), 1_000);
        if (passwordField == null) passwordField = findInputFieldForLabel("Password");
        assertNotNull("Password input field not found (label/hint: Password)", passwordField);
        passwordField.click();
        passwordField.setText("Employee@123");

        // 5) Click Log In button
        logInBtn.click();

        // 6) Verify navigation to the next screen after successful login.
        boolean reachedDashboard = device.wait(
                Until.hasObject(By.res(targetPackage, "btnSearchJobs")),
                TIMEOUT_MS
        ) || device.wait(
                Until.hasObject(By.res(targetPackage, "addJob")),
                TIMEOUT_MS
        );
        assertTrue("Expected to navigate to dashboard after valid login", reachedDashboard);
    }


    /**
     * Attempts to find the EditText associated with a label, such as a Material TextInputLayout label.
     * Works best when the visible label text is exactly "Email"/"Password".
     */
    private UiObject2 findInputFieldForLabel(String labelText) {
        // Fast path: if the hint is set directly on the EditText
        UiObject2 byHint = device.findObject(By.hint(labelText));
        if (byHint != null) return byHint;

        // Find the label TextView (e.g., Material floating label)
        UiObject2 label = device.findObject(By.text(labelText));
        if (label == null) return null;

        // Walk up a few parent levels and search for an EditText within that container
        UiObject2 parent = label.getParent();
        for (int i = 0; i < 4 && parent != null; i++) {
            UiObject2 editText = parent.findObject(By.clazz("android.widget.EditText"));
            if (editText != null) return editText;
            parent = parent.getParent();
        }

        return null;
    }

    @Test
    public void loginScreen_displaysRequiredFields(){
        UiObject2 signInBten = device.wait(Until.findObject(By.res(targetPackage, "signinBtn")), TIMEOUT_MS);
        assertNotNull("SIGN IN button not found", signInBten);
        signInBten.click();

        UiObject2 loginBtn = device.wait(Until.findObject(By.text("Log in")), TIMEOUT_MS);
        assertNotNull("Log in button not found", loginBtn);

        UiObject2 emailField = device.wait(Until.findObject(By.hint("Email")), TIMEOUT_MS);
        assertNotNull("Email field not visible", emailField);

        UiObject2 passwordField = device.wait(Until.findObject(By.hint("Password")), TIMEOUT_MS);
        assertNotNull("Password field not visible", passwordField);
    }
}

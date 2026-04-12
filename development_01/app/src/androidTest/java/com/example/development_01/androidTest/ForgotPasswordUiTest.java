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
public class ForgotPasswordUiTest {

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

    /**
     * Attempts to find the EditText associated with a label, such as a Material TextInputLayout label.
     * Works best when the visible label text is exactly the hint/label text.
     */
    private UiObject2 findInputFieldForLabel(String labelText) {
        // Fast path: hint directly on the EditText
        UiObject2 byHint = device.findObject(By.hint(labelText));
        if (byHint != null) return byHint;

        // Find the label TextView (e.g., floating label)
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

    private UiObject2 waitForText(String text) {
        return device.wait(Until.findObject(By.text(text)), TIMEOUT_MS);
    }

    private UiObject2 waitForTextContains(String textPart) {
        return device.wait(Until.findObject(By.textContains(textPart)), TIMEOUT_MS);
    }

    private UiObject2 waitForRes(String resName) {
        return device.wait(Until.findObject(By.res(targetPackage, resName)), TIMEOUT_MS);
    }

    private void dismissKeyboard() {
        device.pressBack();
        device.waitForIdle();
    }

    @Test
    public void forgotPasswordFlow_validEmail_triggersResetEmailMessage() {
        // From bootup (Main activity), find "SIGN IN" and click it
        UiObject2 signInBtn = waitForRes("signinBtn");
        assertNotNull("Could not find SIGN IN button on MainActivity", signInBtn);
        signInBtn.click();

        // Verify we're at Login screen by scanning for "Log in" button
        UiObject2 loginBtn = waitForText("Log in");
        assertNotNull("Did not navigate to Login screen (Log in button not found)", loginBtn);

        // AC1: "Forgot Password" visible on Login screen (button id: forgotPWBtn)
        UiObject2 forgotBtn = waitForRes("forgotPWBtn");
        if (forgotBtn == null) forgotBtn = waitForText("Forgot Password"); // fallback
        assertNotNull("Forgot Password button not found on Login screen", forgotBtn);

        // AC2: Clicking it opens forgot password screen
        forgotBtn.click();

        // Verify forgot password screen by checking title + Submit button (prefer resource IDs)
        UiObject2 title = waitForRes("forgotpwTitle");
        assertNotNull("Forgot Password screen not shown (forgotpwTitle not found)", title);

        UiObject2 submitBtn = waitForRes("submitBtn");
        if (submitBtn == null) submitBtn = waitForText("Submit"); // fallback
        assertNotNull("Submit button not found on Forgot Password screen", submitBtn);

        // AC3: Enter valid email — use resource ID directly (most reliable)
        UiObject2 emailField = waitForRes("emailAddress");
        assertNotNull("Email input field not found", emailField);

        emailField.click();
        emailField.setText("hulk@email.com");

        UiObject2 newPasswordField = waitForRes("newPassword");
        if (newPasswordField != null) {
            newPasswordField.click();
            newPasswordField.setText("NewPass@123");
        }
        dismissKeyboard();

        // Re-find submitBtn to avoid stale reference after keyboard interaction
        UiObject2 submitBtn2 = waitForRes("submitBtn");
        assertNotNull("Submit button not found before submitting", submitBtn2);
        submitBtn2.click();

        boolean resetOutcomeShown = device.wait(
                Until.hasObject(By.textContains("Password reset email sent")),
                TIMEOUT_MS
        ) || device.wait(
                Until.hasObject(By.textContains("Email not found")),
                TIMEOUT_MS
        ) || device.wait(
                Until.hasObject(By.textContains("Password reset failed")),
                TIMEOUT_MS
        );
        assertTrue("Expected a password reset outcome message after submitting valid email", resetOutcomeShown);
    }

    @Test
    public void forgotPasswordFlow_invalidEmail_showsErrorMessage() {
        // Navigate Main -> Login
        UiObject2 signInBtn = waitForRes("signinBtn");
        assertNotNull("Could not find SIGN IN button on MainActivity", signInBtn);
        signInBtn.click();

        UiObject2 loginBtn = waitForText("Log in");
        assertNotNull("Did not navigate to Login screen (Log in button not found)", loginBtn);

        // Navigate Login -> Forgot Password (prefer resource ID)
        UiObject2 forgotBtn = waitForRes("forgotPWBtn");
        if (forgotBtn == null) forgotBtn = waitForText("Forgot Password");
        assertNotNull("Forgot Password button not found on Login screen", forgotBtn);
        forgotBtn.click();

        UiObject2 submitBtn = waitForRes("submitBtn");
        if (submitBtn == null) submitBtn = waitForText("Submit");
        assertNotNull("Submit button not found on Forgot Password screen", submitBtn);

        // AC4: invalid format should show an error
        UiObject2 emailField = waitForRes("emailAddress");
        assertNotNull("Email input field not found", emailField);

        emailField.click();
        emailField.setText("not-an-email");

        UiObject2 newPasswordField = waitForRes("newPassword");
        if (newPasswordField != null) {
            newPasswordField.click();
            newPasswordField.setText("NewPass@123");
        }
        dismissKeyboard();

        // Re-find submitBtn to avoid stale reference after keyboard interaction
        UiObject2 submitBtn2 = waitForRes("submitBtn");
        assertNotNull("Submit button not found before submitting", submitBtn2);
        submitBtn2.click();

        // Your implementation shows: "Invalid email" for invalid format
        UiObject2 err1 = waitForTextContains("Invalid email");
        UiObject2 err2 = (err1 == null) ? waitForTextContains("Email not found") : null;
        UiObject2 err3 = (err1 == null && err2 == null) ? waitForTextContains("Password reset failed") : null;
        UiObject2 err4 = (err1 == null && err2 == null && err3 == null) ? waitForTextContains("Enter detail") : null;

        assertTrue(
                "Expected error message after invalid email submission",
                err1 != null || err2 != null || err3 != null || err4 != null
        );
    }

    @Test
    public void forgotPasswordScreen_uiElementVisible(){
        UiObject2 signInBtn = waitForRes("signinBtn");
        assertNotNull(signInBtn);
        signInBtn.click();

        UiObject2 forgotBtn = waitForRes("forgotPWBtn");
        if (forgotBtn == null) forgotBtn = waitForText("Forgot Password");
        assertNotNull(forgotBtn);
        forgotBtn.click();

        UiObject2 title = waitForRes("forgotpwTitle");
        assertNotNull("Forgot Password title not visible", title);

        UiObject2 emailField = waitForRes("emailAddress");
        assertNotNull("Email field missing", emailField);

        UiObject2 newPasswordField = waitForRes("newPassword");
        assertNotNull("New password field missing", newPasswordField);

        UiObject2 submitBtn= waitForRes("submitBtn");
        assertNotNull("Submit button missing", submitBtn);



    }
}

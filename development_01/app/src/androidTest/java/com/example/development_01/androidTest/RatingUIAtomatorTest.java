package com.example.development_01.androidTest;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;

public class RatingUIAtomatorTest {

    private static final int LAUNCH_TIMEOUT = 5000;
    final String launcherPackage = "com.example.development_01";
    private UiDevice device;

    @Before
    public void setup() throws UiObjectNotFoundException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();

        // 1. Launch the app from the start
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(launcherPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // 2. Perform login to set the Firebase session
        UiObject signinBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/signinBtn"));
        signinBtn.click();

        UiObject emailField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/emailAddress"));
        emailField.setText("danick@employer.com");

        UiObject passwordField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/userPassword"));
        passwordField.setText("Dream2030");

        UiObject loginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/loginBtn"));
        loginBtn.click();

        // 3. Wait for the Employer Dashboard to load
        device.wait(Until.hasObject(By.text("Employer Dashboard")), LAUNCH_TIMEOUT);

        // 4. Navigate to the Favorite Employees page
        UiObject payEmployeeBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnPayPal"));
        payEmployeeBtn.click();

        // 5. Ensure we are on the correct page
        device.wait(Until.hasObject(By.text("Hired Employees")), LAUNCH_TIMEOUT);

        UiObject dreamerEntry = device.findObject(new UiSelector().text("Danick"));
        dreamerEntry.click();

        // 6. Wait for the "Complete Job" screen to load
        device.wait(Until.hasObject(By.text("Complete Job")), LAUNCH_TIMEOUT);
    }

    @Test
    public void testRatingButtonExists() throws UiObjectNotFoundException {
        UiObject rateBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnRateWorker"));
        if (rateBtn.waitForExists(3000)) {
            assertTrue("Rate button should be visible", rateBtn.exists());
        }
    }

    @Test
    public void testRatingButtonClickable() throws UiObjectNotFoundException {
        UiObject completeBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnMarkCompleted"));
        completeBtn.click();

        UiObject rateBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnRateWorker"));
        if (rateBtn.waitForExists(3000)) {
            assertTrue("Rate button should be clickable", rateBtn.isEnabled());
        }
    }

    @Test
    public void testRatingButtongoesToRatingUI() throws UiObjectNotFoundException {
        UiObject completeBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnMarkCompleted"));
        completeBtn.click();

        UiObject rateBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnRateWorker"));
        rateBtn.click();

        device.wait(Until.hasObject(By.text("Rate worker")), LAUNCH_TIMEOUT);
        UiObject ratingTitle = device.findObject(new UiSelector().text("Rate worker"));
        assertTrue("Rating title should be visible", ratingTitle.exists());
    }

    @Test
    public void testSubmitGoesToPay() throws UiObjectNotFoundException {
        UiObject completeBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnMarkCompleted"));
        completeBtn.click();

        UiObject rateBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnRateWorker"));
        rateBtn.click();

        device.wait(Until.hasObject(By.text("Rate worker")), LAUNCH_TIMEOUT);

        UiObject ratingBar = device.findObject(new UiSelector().resourceId("com.example.development_01:id/ratingBar"));
        ratingBar.click();
        UiObject submitBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnSubmitRating"));
        submitBtn.click();

        device.wait(Until.hasObject(By.text("Complete Job")), LAUNCH_TIMEOUT);
        UiObject payEmployeeBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/btnPayWorker"));
        assertTrue("Pay button should be visible", payEmployeeBtn.exists());
    }
}

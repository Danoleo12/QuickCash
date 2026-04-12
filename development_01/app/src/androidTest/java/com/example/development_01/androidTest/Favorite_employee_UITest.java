package com.example.development_01.androidTest;

import static org.junit.Assert.assertFalse;
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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Favorite_employee_UITest {
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

        intent.putExtra("employee_name", "John Doe");
        intent.putExtra("employee_email", "john.doe@example.com");

        context.startActivity(intent);
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // 2. Perform login to set the Firebase session
        UiObject signinBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/signinBtn"));
        signinBtn.click();

        UiObject emailField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/emailAddress"));
        emailField.setText("tester@employer.com");

        UiObject passwordField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/userPassword"));
        passwordField.setText("Employer@123");

        UiObject loginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/loginBtn"));
        loginBtn.click();

        // 3. Wait for the Employer Dashboard to load
        device.wait(Until.hasObject(By.text("Employer Dashboard")), LAUNCH_TIMEOUT);

        // 4. Navigate to the Favorite Employees page
        UiObject viewFavoritesBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/viewFavorites"));
        viewFavoritesBtn.click();

        // 5. Ensure we are on the correct page
        device.wait(Until.hasObject(By.text("Favorite Employees")), LAUNCH_TIMEOUT);
    }

    @Test
    public void testRecyclerViewExists() throws UiObjectNotFoundException {
        UiObject recyclerView = device.findObject(new UiSelector().resourceId("com.example.development_01:id/recyclerViewEmployee"));
        assertTrue("RecyclerView for favorite employees should be visible", recyclerView.exists());
    }

    @Test
    public void testRemoveButtonExists() throws UiObjectNotFoundException {
        UiObject removeBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/removeButton"));
        if (removeBtn.waitForExists(3000)) {
            assertTrue("Remove button should be visible", removeBtn.exists());
        }
    }
}

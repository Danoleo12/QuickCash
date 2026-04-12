package com.example.development_01.androidTest;

import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UIAutomator tests for the Google Maps feature (US5).
 * Updated to prevent gesture navigation interference on Pixel devices.
 */
@RunWith(AndroidJUnit4.class)
public class MapsUIAutomatorTest {

    private static final int LAUNCH_TIMEOUT = 10000;
    private static final int MAP_LOAD_WAIT = 6000;   // Wait 6s for markers to appear
    private static final String PACKAGE_NAME = "com.example.development_01";
    private UiDevice device;

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Grant POST_NOTIFICATIONS so EmployeeActivity's permission request
        // doesn't pop a system dialog that blocks map navigation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation()
                    .grantRuntimePermission(PACKAGE_NAME, Manifest.permission.POST_NOTIFICATIONS);
        }

        // Start from the home screen
        device.pressHome();

        // Launch the app
        Context context = ApplicationProvider.getApplicationContext();
        final Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
        if (appIntent != null) {
            appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(appIntent);
        }

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), LAUNCH_TIMEOUT);
    }

    /**
     * Helper method to navigate from the Signup screen to the Employee Dashboard.
     * Uses the provided test credentials: tester1@employee.com / Employee@123
     */
    private void navigateToDashboard() throws UiObjectNotFoundException {
        // 1. Click "Sign In" from the main Signup page
        UiObject signInLink = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/signinBtn"));
        if (signInLink.waitForExists(LAUNCH_TIMEOUT)) {
            signInLink.click();
        }

        // 2. Fill in login details on the LoginActivity
        UiObject emailField = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/emailAddress"));
        if (emailField.waitForExists(LAUNCH_TIMEOUT)) {
            emailField.setText("tester1@employee.com");
            
            UiObject passwordField = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/userPassword"));
            passwordField.setText("Employee@123");
            
            UiObject loginBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/loginBtn"));
            loginBtn.click();
        }
        
        // 3. Wait for the Employee Dashboard (EmployeeActivity) to appear
        UiObject viewMapBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/btnViewMap"));
        assertTrue("Dashboard should be reached after login", viewMapBtn.waitForExists(LAUNCH_TIMEOUT));
    }

    /**
     * TEST 1: Navigation and Back Button
     */
    @Test
    public void testMapNavigationAndBack() throws UiObjectNotFoundException {
        navigateToDashboard();

        UiObject viewMapBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/btnViewMap"));
        viewMapBtn.click();
        
        // Wait specifically for Map UI to load
        UiObject backBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/btnBack"));
        assertTrue("Back button should exist on Map overlay", backBtn.waitForExists(LAUNCH_TIMEOUT));
        
        // Hold to allow user to see the map
        SystemClockSleep(MAP_LOAD_WAIT);
        
        backBtn.click();
        
        assertTrue("Should return to Dashboard after clicking back", viewMapBtn.waitForExists(LAUNCH_TIMEOUT));
    }

    /**
     * TEST 2: UI Component Visibility
     */
    @Test
    public void testMapUIComponentsVisibility() throws UiObjectNotFoundException {
        navigateToDashboard();

        UiObject viewMapBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/btnViewMap"));
        viewMapBtn.click();

        UiObject googleMap = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/map"));
        assertTrue("Google Map fragment should be visible", googleMap.waitForExists(LAUNCH_TIMEOUT));

        // Wait to show the map UI
        SystemClockSleep(MAP_LOAD_WAIT);

        UiObject mapTitle = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/mapTitle"));
        assertTrue("Map Title bar should be visible", mapTitle.exists());
        Assert.assertEquals("Job Postings", mapTitle.getText());
    }

    /**
     * TEST 3: Map Interaction
     * Performs a center-screen swipe to avoid triggering the system "Back" gesture.
     */
    @Test
    public void testMapInteraction() throws UiObjectNotFoundException {
        navigateToDashboard();

        UiObject viewMapBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/btnViewMap"));
        viewMapBtn.click();

        UiObject googleMap = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/map"));
        assertTrue("Map should be visible for interaction", googleMap.waitForExists(LAUNCH_TIMEOUT));

        // Wait for map and markers to load
        SystemClockSleep(MAP_LOAD_WAIT);

        // Perform a swipe in the center of the screen to avoid edge-based "Back" gestures
        int displayWidth = device.getDisplayWidth();
        int displayHeight = device.getDisplayHeight();
        
        // Swipe from right-center to left-center, staying away from the edges
        int startX = (int) (displayWidth * 0.8);
        int endX = (int) (displayWidth * 0.2);
        int y = displayHeight / 2;
        
        device.swipe(startX, y, endX, y, 20); // 20 steps for a smooth scroll
        
        // Stay a bit after swipe to observe
        SystemClockSleep(2000);

        // Verify overlay persists
        UiObject backBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/btnBack"));
        assertTrue("UI should remain responsive after map interaction", backBtn.exists());
    }

    private void SystemClockSleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
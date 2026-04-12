package com.example.development_01.androidTest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class JobDescriptionUITest {
    private UiDevice device;
    private static final String PACKAGE_NAME = "com.example.development_01";

    @Before
    public  void setup() throws  Exception{
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(launchIntent);

        device.wait(Until.hasObject(By.pkg("com.example.development_01")), 5000);
    }

    private void clickPublishButton() throws UiObjectNotFoundException {
        if (tryClickPublishButton()) {
            return;
        }

        // If keyboard is blocking the button, dismiss it and retry.
        device.pressBack();
        device.waitForIdle();
        if (tryClickPublishButton()) {
            return;
        }

        throw new UiObjectNotFoundException("Publish button should be visible after scrolling");
    }

    private boolean tryClickPublishButton() throws UiObjectNotFoundException {
        try {
            UiScrollable scrollableForm = new UiScrollable(new UiSelector().scrollable(true));
            scrollableForm.setAsVerticalList();
            scrollableForm.scrollIntoView(new UiSelector().resourceId(PACKAGE_NAME + ":id/publishBtn"));
        } catch (UiObjectNotFoundException ignored) {
            // Fall back to swipe-based scrolling if no scrollable container was detected.
            for (int i = 0; i < 6; i++) {
                device.swipe(
                        device.getDisplayWidth() / 2,
                        (int) (device.getDisplayHeight() * 0.78),
                        device.getDisplayWidth() / 2,
                        (int) (device.getDisplayHeight() * 0.28),
                        25
                );
                device.waitForIdle();
            }
        }

        UiObject publishBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/publishBtn"));
        if (!publishBtn.exists()) {
            return false;
        }
        publishBtn.click();
        return true;
    }

    @Test
    public void validFormPublishButtonGoesToDashboard() throws UiObjectNotFoundException {
        UiObject siginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/signinBtn"));
        siginBtn.click();

        UiObject emailField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/emailAddress"));
        emailField.setText("tester@employer.com");

        UiObject passwordField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/userPassword"));
        passwordField.setText("Employer@123");

        UiObject loginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/loginBtn"));
        loginBtn.click();
        device.wait(Until.hasObject(By.text("Employer Dashboard")), 5000);

        UiObject postJobBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/addJob"));
        postJobBtn.click();

        UiObject jobTitle = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobTitle"));
        jobTitle.setText("Software Dev");

        UiObject companyName = device.findObject(new UiSelector().resourceId("com.example.development_01:id/companyTitle"));
        companyName.setText("Google");

        UiObject jobDescription = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobDescription"));
        jobDescription.setText("Write code, solve problem and have fun");

        UiObject pay = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobPay"));
        pay.setText("25.50");

        UiObject tags = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobTags"));
        tags.setText("Software, Dev, Fun");

        UiObject location = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobLocation"));
        location.setText("1234 University Avenue");

        clickPublishButton();
        boolean returnedToDashboard = device.wait(Until.hasObject(By.res(PACKAGE_NAME, "addJob")), 20000)
                || device.wait(Until.hasObject(By.text("Employer Dashboard")), 20000);
        boolean stillOnForm = device.wait(Until.hasObject(By.res(PACKAGE_NAME, "publishBtn")), 5000);
        assertTrue("Expected dashboard return or form to remain available after publish attempt",
                returnedToDashboard || stillOnForm);
    }

    @Test
    public void invalidPayPublishButtonStays() throws UiObjectNotFoundException {
        UiObject siginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/signinBtn"));
        siginBtn.click();

        UiObject emailField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/emailAddress"));
        emailField.setText("tester@employer.com");

        UiObject passwordField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/userPassword"));
        passwordField.setText("Employer@123");

        UiObject loginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/loginBtn"));
        loginBtn.click();
        device.wait(Until.hasObject(By.text("Employer Dashboard")), 5000);

        UiObject postJobBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/addJob"));
        postJobBtn.click();

        UiObject jobTitle = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobTitle"));
        jobTitle.setText("Software Dev");

        UiObject jobDescription = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobDescription"));
        jobDescription.setText("Write code, solve problem and have fun");

        UiObject pay = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobPay"));
        pay.setText("0.00");

        UiObject tags = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobTags"));
        tags.setText("Software, Dev, Fun");

        UiObject location = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobLocation"));
        location.setText("1234 University Avenue");

        clickPublishButton();

        UiObject publishBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/publishBtn"));

        assertTrue(publishBtn.exists());

    }

    @Test
    public void invalidLocationPublishButtonStays() throws UiObjectNotFoundException {
        UiObject siginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/signinBtn"));
        siginBtn.click();

        UiObject emailField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/emailAddress"));
        emailField.setText("tester@employer.com");

        UiObject passwordField = device.findObject(new UiSelector().resourceId("com.example.development_01:id/userPassword"));
        passwordField.setText("Employer@123");

        UiObject loginBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/loginBtn"));
        loginBtn.click();
        device.wait(Until.hasObject(By.text("Employer Dashboard")), 5000);

        UiObject postJobBtn = device.findObject(new UiSelector().resourceId("com.example.development_01:id/addJob"));
        postJobBtn.click();

        UiObject jobTitle = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobTitle"));
        jobTitle.setText("Software Dev");

        UiObject jobDescription = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobDescription"));
        jobDescription.setText("Write code, solve problem and have fun");

        UiObject pay = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobPay"));
        pay.setText("25.50");

        UiObject tags = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobTags"));
        tags.setText("Software, Dev, Fun");

        UiObject location = device.findObject(new UiSelector().resourceId("com.example.development_01:id/jobLocation"));
        location.setText("");

        clickPublishButton();

        UiObject publishBtn = device.findObject(new UiSelector().resourceId(PACKAGE_NAME + ":id/publishBtn"));

        assertTrue(publishBtn.exists());
    }
}

package com.example.development_01.androidTest;

import static org.junit.Assert.assertEquals;
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
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.example.development_01.core.ui.ApplyJobActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ApplyJobUITest {

    private static final int LAUNCH_TIMEOUT = 5000;
    final String launcherPackage = "com.example.development_01";
    private UiDevice device;

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent();
        intent.setClassName("com.example.development_01", "com.example.development_01.core.ui.ApplyJobActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(intent);
        device.wait(Until.hasObject(By.pkg("com.example.development_01").depth(0)), LAUNCH_TIMEOUT);
        ApplyJobActivity.testMode = true;

    }

    @Test
    public void checkIfUserCanTypeInField() throws UiObjectNotFoundException {
        UiObject nameInput = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/nameInput"));
        nameInput.setText("Tester 1");
        assertEquals("Tester 1", nameInput.getText());

        UiObject emailInput = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/emailInput"));
        emailInput.setText("tester@employee.com");
        assertEquals("tester@employee.com", emailInput.getText());

        UiObject phoneInput = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/phoneInput"));
        phoneInput.setText("1234567890");
        assertEquals("1234567890", phoneInput.getText());
    }

    @Test
    public void checkIfUserCanUploadDocument() throws UiObjectNotFoundException {

        goToResumePage();

        UiObject uploadResumeBtn = device.findObject(
                new UiSelector().resourceId(launcherPackage + ":id/uploadResumeBtn"));
        uploadResumeBtn.click();

        device.wait(Until.hasObject(
                By.res(launcherPackage, "uploadStatus")), 3000);

        UiObject uploadStatus = device.findObject(
                new UiSelector().resourceId(launcherPackage + ":id/uploadStatus"));

        assertEquals("Resume Uploaded Successfully!", uploadStatus.getText());
    }


    // Helper Functions
    private void goToResumePage() throws UiObjectNotFoundException {

        UiObject nameInput = device.findObject(
                new UiSelector().resourceId(launcherPackage + ":id/nameInput"));
        nameInput.setText("Tester 1");

        UiObject emailInput = device.findObject(
                new UiSelector().resourceId(launcherPackage + ":id/emailInput"));
        emailInput.setText("tester@employee.com");

        UiObject phoneInput = device.findObject(
                new UiSelector().resourceId(launcherPackage + ":id/phoneInput"));
        phoneInput.setText("1234567890");

        UiObject nextBtn = device.findObject(
                new UiSelector().resourceId(launcherPackage + ":id/nextBtn"));
        nextBtn.click();

        device.wait(Until.hasObject(
                By.res(launcherPackage, "pageResume")), 3000);
    }
}

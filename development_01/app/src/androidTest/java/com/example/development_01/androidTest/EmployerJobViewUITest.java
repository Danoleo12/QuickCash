package com.example.development_01.androidTest;

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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EmployerJobViewUITest {

    private static final int LAUNCH_TIMEOUT = 5000;
    final String launcherPackage = "com.example.development_01";
    private UiDevice device;

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent();
        intent.setClassName("com.example.development_01", "com.example.development_01.core.ui.EmployerJobViewActivity");
        intent.putExtra("email", "employer@test.com");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(intent);
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void checkIfJobListPageIsVisible() throws UiObjectNotFoundException {
        UiObject header = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/header"));
        assertTrue(header.exists());

        UiObject rvJobs = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/rvEmployerJobs"));
        assertTrue(rvJobs.exists());

        UiObject btnBack = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/btnBack"));
        assertTrue(btnBack.exists());
    }

    @Test
    public void testNavigationToDetails() throws UiObjectNotFoundException {
        UiObject rvJobs = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/rvEmployerJobs"));
        if (rvJobs.exists() && rvJobs.getChildCount() > 0) {
            rvJobs.getChild(new UiSelector().index(0)).click();
            
            UiObject detailHeader = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/tvHeaderJobTitle"));
            assertTrue(detailHeader.waitForExists(3000));
        }
    }
}
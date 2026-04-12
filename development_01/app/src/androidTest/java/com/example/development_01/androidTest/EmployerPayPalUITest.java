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
public class EmployerPayPalUITest {

    private static final int LAUNCH_TIMEOUT = 5000;
    final String launchPackage = "com.example.development_01";
    private UiDevice device;

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();

        Intent intent = new Intent();
        intent.setClassName(launchPackage, "com.example.development_01.core.ui.EmployerPayPal");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Dummy Data for testing
        intent.putExtra("JOB_ID", "test_job_id");
        intent.putExtra("JOB_TITLE", "Software Developer");
        intent.putExtra("LOCATION", "Halifax, NS");
        intent.putExtra("APPLICANT_NAME", "John Doe");
        intent.putExtra("APPLICANT_EMAIL", "john@example.com");
        intent.putExtra("JOB_PAY", 50.0);

        context.startActivity(intent);
        device.wait(Until.hasObject(By.pkg(launchPackage).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void checkIfPayPalPageIsVisible() {
        UiObject jobTitle = device.findObject(new UiSelector().resourceId(launchPackage + ":id/tvCompleteJobTitle"));
        assertTrue("Job Title should be visible", jobTitle.exists());

        UiObject jobLocation = device.findObject(new UiSelector().resourceId(launchPackage + ":id/tvCompleteJobLocation"));
        assertTrue("Job Location should be visible", jobLocation.exists());

        UiObject workerName = device.findObject(new UiSelector().resourceId(launchPackage + ":id/tvWorkerName"));
        assertTrue("Worker Name should be visible", workerName.exists());

        UiObject workerEmail = device.findObject(new UiSelector().resourceId(launchPackage + ":id/tvWorkerEmail"));
        assertTrue("Worker Email should be visible", workerEmail.exists());

        UiObject agreedPay = device.findObject(new UiSelector().resourceId(launchPackage + ":id/tvAgreedPay"));
        assertTrue("Agreed Pay should be visible", agreedPay.exists());

        UiObject totalDue = device.findObject(new UiSelector().resourceId(launchPackage + ":id/tvTotalDue"));
        assertTrue("Total Due should be visible", totalDue.exists());

        UiObject markCompletedBtn = device.findObject(new UiSelector().resourceId(launchPackage + ":id/btnMarkCompleted"));
        assertTrue("Mark Completed button should be visible", markCompletedBtn.exists());

        UiObject payWorkerBtn = device.findObject(new UiSelector().resourceId(launchPackage + ":id/btnPayWorker"));
        assertTrue("Pay Worker button should be visible", payWorkerBtn.exists());
    }

    @Test
    public void checkIfUserCanMarkJobAsCompleted() throws UiObjectNotFoundException {
        UiObject markCompletedBtn = device.findObject(new UiSelector().resourceId(launchPackage + ":id/btnMarkCompleted"));
        markCompletedBtn.click();

        UiObject statusBanner = device.findObject(new UiSelector().resourceId(launchPackage + ":id/cardStatusBanner"));
        assertTrue("Status banner should appear after marking completed", statusBanner.exists());
        
        UiObject payWorkerBtn = device.findObject(new UiSelector().resourceId(launchPackage + ":id/btnPayWorker"));
        assertTrue("Pay Worker button should be enabled after marking completed", payWorkerBtn.isEnabled());
    }

    @Test
    public void checkIfUserCanPayWorkerViaPayPal() throws UiObjectNotFoundException {
        UiObject markCompletedBtn = device.findObject(new UiSelector().resourceId(launchPackage + ":id/btnMarkCompleted"));
        markCompletedBtn.click();

        UiObject payWorkerBtn = device.findObject(new UiSelector().resourceId(launchPackage + ":id/btnPayWorker"));
        device.wait(Until.findObject(By.res(launchPackage, "btnPayWorker").enabled(true)), 2000);
        
        payWorkerBtn.click();

        // Check if Employer UI has disappeared, assume the PayPal screen has opened.
        boolean employerUiDisappeared = device.wait(Until.gone(By.res(launchPackage, "tvCompleteJobTitle")), 10000);
        
        boolean packageChanged = !device.getCurrentPackageName().equals(launchPackage);

        assertTrue("PayPal screen should have opened (Employer UI did not disappear and package stayed the same)", 
                employerUiDisappeared || packageChanged);
    }
}

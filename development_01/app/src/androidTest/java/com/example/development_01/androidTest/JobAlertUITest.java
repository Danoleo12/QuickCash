package com.example.development_01.androidTest;

import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UIAutomator end-to-end test for the Job Alert notification feature.
 *
 * No second emulator is needed. After the employee logs in and the Firestore
 * listener is live, this test itself writes a matching job document directly
 * to Firestore — the same action an employer would perform. The service detects
 * the new document via its snapshot listener and fires a notification.
 *
 * Prerequisites (must exist in Firebase before running):
 *   Employee account —
 *       email:      tester2@employee.com
 *       password:   Employee@123
 *       role:       Employee
 *       preference: ["Software Development", "Developer"]
 *
 * Test flow:
 *   1. Log in as the employee → EmployeeActivity loads.
 *   2. JobAlertService starts and attaches a Firestore snapshot listener.
 *   3. This test writes a job with title "Software Developer" to Firestore.
 *   4. The snapshot listener fires → preference "Developer" matches → notification posted.
 *   5. Test opens notification shade and taps the job-match notification.
 *   6. Assert PostDescription opens showing the correct job title.
 *
 * Cleanup:
 *   The test job document is deleted from Firestore in @After so it does not
 *   accumulate across repeated runs.
 */
@RunWith(AndroidJUnit4.class)
public class JobAlertUITest {

    private static final String PACKAGE        = "com.example.development_01";
    private static final int    LAUNCH_TIMEOUT = 5000;
    private static final int    UI_TIMEOUT     = 5000;
    private static final int    LOGIN_TIMEOUT  = 8000;   // Firebase Auth round-trip
    private static final int    NOTIF_TIMEOUT  = 20000;  // Firestore listener + notification delivery

    private static final String EMPLOYEE_EMAIL    = "tester1@employee.com";
    private static final String EMPLOYEE_PASSWORD = "Employee@123";

    // Job that intentionally matches the employee's "Developer" preference
    private static final String TEST_JOB_TITLE_PREFIX = "Software Developer UITest";
    private static final String TEST_JOB_COMPANY = "UITest Corp";

    private UiDevice device;

    // Keeps a reference to the test job so @After can delete it
    private String postedJobId = null;

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        FirebaseAuth.getInstance().signOut();

        // Fix 1: Grant POST_NOTIFICATIONS before the app launches so the
        // permission check in JobAlertService.showJobNotification() passes.
        // Without this, the service builds the notification but silently returns
        // before calling NotificationManagerCompat.notify().
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation()
                    .grantRuntimePermission(PACKAGE, Manifest.permission.POST_NOTIFICATIONS);
        }

        CountDownLatch authLatch = new CountDownLatch(1);
        final boolean[] authOk = {false};
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(EMPLOYEE_EMAIL, EMPLOYEE_PASSWORD)
                .addOnSuccessListener(result -> {
                    authOk[0] = true;
                    authLatch.countDown();
                })
                .addOnFailureListener(e -> authLatch.countDown());
        try {
            authLatch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertTrue("Employee auth failed before JobAlert UI test", authOk[0]);

        // Launch EmployeeActivity directly so JobAlertService starts immediately.
        Context context = ApplicationProvider.getApplicationContext();
        Intent dashboardIntent = new Intent();
        dashboardIntent.setClassName(PACKAGE, PACKAGE + ".core.ui.EmployeeActivity");
        dashboardIntent.putExtra("name", "Tester One");
        dashboardIntent.putExtra("email", EMPLOYEE_EMAIL);
        dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(dashboardIntent);

        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    @After
    public void tearDown() {
        // Delete the test job document so repeated runs stay clean.
        if (postedJobId != null) {
            FirebaseFirestore.getInstance()
                    .collection("jobs")
                    .document(postedJobId)
                    .delete();
        }
    }

    /**
     * AC1 – Employee login routes to EmployeeActivity.
     * AC2 – JobAlertService starts and its Firestore listener is live.
     * AC3 – A new matching job written by this test triggers a job-match notification.
     * AC4 – Tapping the notification opens PostDescription for the correct job.
     */
    @Test
    public void newMatchingJobPosted_employeeReceivesNotification_tappingOpensPostDescription()
            throws UiObjectNotFoundException {

        // ── Step 1: Wait for EmployeeActivity and the service listener ────────
        boolean dashboardVisible = waitForEmployeeDashboard(5000) || loginThroughUiFallback();
        assertTrue("EmployeeActivity must appear after employee login", dashboardVisible);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ensurePreferenceContainsDeveloper(uid);

        // Fix 2: waitForIdle() only waits for the UI thread to go idle (~500ms),
        // but the Firestore listener's initial snapshot is a background network
        // operation. If the test writes the job before the snapshot arrives,
        // isFirstSnapshot is still true and the ADDED event is skipped.
        // A fixed sleep gives Firestore time to deliver the initial snapshot and
        // flip isFirstSnapshot = false before we write the test job.
        device.waitForIdle(UI_TIMEOUT);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ── Step 3: Write a matching job to Firestore (replaces second emulator)
        postedJobId = postTestJobToFirestore();
        assertTrue("Test job must be written to Firestore successfully",
                postedJobId != null);

        // ── Step 4: Open the notification shade and wait for the job alert ────
        device.openNotification();

        boolean jobNotificationVisible = device.wait(
                Until.hasObject(By.textContains("New Job Match!")), NOTIF_TIMEOUT)
                || device.wait(Until.hasObject(By.textContains("View Job Details")), NOTIF_TIMEOUT);
        assertTrue(
                "A 'New Job Match!' notification must appear after a matching job " +
                "is added to Firestore, confirming the snapshot listener and " +
                "preference-matching algorithm are working end-to-end.",
                jobNotificationVisible
        );

        // ── Step 5: Tap the notification ─────────────────────────────────────
        UiObject2 notification = device.findObject(By.textContains("New Job Match!"));
        if (notification == null) {
            notification = device.findObject(By.textContains("View Job Details"));
        }
        assertTrue("Expected job notification row to be tappable", notification != null);
        notification.click();

        // ── Step 6: Assert PostDescription opens with the correct job title ───
        boolean postDescriptionVisible = device.wait(
                Until.hasObject(By.pkg(PACKAGE).depth(0)), UI_TIMEOUT);
        assertTrue("App must come to foreground after tapping the notification",
                postDescriptionVisible);

        UiObject jobTitleView = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/jobTitleText"));
        assertTrue("Job title view must be visible in PostDescription", jobTitleView.exists());
        assertTrue(
                "PostDescription must display the job that triggered the notification",
                jobTitleView.getText().contains(TEST_JOB_TITLE_PREFIX)
        );
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Writes a test job document to the Firestore "jobs" collection from within
     * the instrumented test process. This is how the test simulates an employer
     * posting a job without requiring a second device or emulator.
     *
     * The job title "Software Developer" intentionally contains "Developer",
     * which matches tester2's stored preference.
     *
     * @return the Firestore document ID of the newly created job, or null on failure.
     */
    private String postTestJobToFirestore() {
        String testJobTitle = TEST_JOB_TITLE_PREFIX + " " + System.currentTimeMillis();

        Map<String, Object> job = new HashMap<>();
        job.put("title",       testJobTitle);
        job.put("companyName", TEST_JOB_COMPANY);
        job.put("description", "Instrumented test job — safe to delete");
        job.put("location",    "Halifax, NS");
        job.put("pay",         45.0);
        job.put("tags",        Arrays.asList("Java", "Android"));
        job.put("employerEmail", "uitest@employer.com");

        final String[] resultId = {null};
        CountDownLatch latch = new CountDownLatch(1);

        FirebaseFirestore.getInstance()
                .collection("jobs")
                .add(job)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference ref) {
                        // Update the "id" field to match the document ID,
                        // consistent with how JobDescription.java posts jobs.
                        ref.update("id", ref.getId());
                        ref.update("title", testJobTitle);
                        resultId[0] = ref.getId();
                        latch.countDown();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        latch.countDown();
                    }
                });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return resultId[0];
    }

    private void ensurePreferenceContainsDeveloper(String uid) {
        CountDownLatch latch = new CountDownLatch(1);
        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("preference")
                .setValue(Arrays.asList("developer"))
                .addOnSuccessListener(unused -> latch.countDown())
                .addOnFailureListener(e -> latch.countDown());
        try {
            boolean done = latch.await(10, TimeUnit.SECONDS);
            assertTrue("Failed to write employee preference for alert test", done);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while writing employee preference");
        }
    }

    private boolean waitForEmployeeDashboard(long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (device.hasObject(By.res(PACKAGE, "btnSearchJobs"))
                    || device.hasObject(By.res(PACKAGE, "btnPreferredJobs"))
                    || device.hasObject(By.res(PACKAGE, "btnViewMap"))
                    || device.hasObject(By.res(PACKAGE, "employeeLogOut"))) {
                return true;
            }
            device.waitForIdle(500);
        }
        return false;
    }

    private boolean loginThroughUiFallback() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(PACKAGE);
        if (appIntent != null) {
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(appIntent);
        }
        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), LAUNCH_TIMEOUT);

        UiObject signIn = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/signinBtn"));
        if (signIn.waitForExists(8000)) {
            try {
                signIn.clickAndWaitForNewWindow(5000);
            } catch (UiObjectNotFoundException e) {
                return false;
            }
        }

        UiObject emailField = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/emailAddress"));
        UiObject passwordField = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/userPassword"));
        UiObject loginButton = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/loginBtn"));
        try {
            if (emailField.waitForExists(8000) && passwordField.exists() && loginButton.exists()) {
                emailField.setText(EMPLOYEE_EMAIL);
                passwordField.setText(EMPLOYEE_PASSWORD);
                loginButton.click();
            }
        } catch (UiObjectNotFoundException e) {
            return false;
        }

        return waitForEmployeeDashboard(LOGIN_TIMEOUT);
    }
}

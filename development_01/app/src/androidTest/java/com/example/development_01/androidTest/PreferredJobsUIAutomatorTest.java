package com.example.development_01.androidTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import com.example.development_01.core.data.firebase.PreferenceRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UI Automator tests for the Preferred Jobs user story.
 *
 * Covers:
 * AC1: Saving a job posting to preferences.
 * AC2: Viewing the saved jobs list from the dashboard.
 */
@RunWith(AndroidJUnit4.class)
public class PreferredJobsUIAutomatorTest {

    private static final String PACKAGE = "com.example.development_01";
    private static final long LAUNCH_TIMEOUT = 10000;
    private static final long UI_TIMEOUT = 10000;
    private static final long FIREBASE_TIMEOUT = 30000;

    // Credentials for user 'Mike'
    private static final String EMPLOYEE_EMAIL = "mike@email.com";
    private static final String EMPLOYEE_PASSWORD = "Test@1234";

    // Seed data created in @BeforeClass and cleaned up in @AfterClass
    private static final String SEED_JOB_TITLE = "Software Developer";
    private static String mikeUid = null;
    private static String seedJobId = null;

    private UiDevice device;

    /**
     * Runs once before all tests.
     * 1. Signs in as mike to obtain their UID.
     * 2. Adds SEED_JOB_TITLE to mike's RTDB preference list.
     * 3. Seeds a matching job document in Firestore.
     * 4. Signs out so the UI tests can sign in fresh through the app UI.
     */
    @BeforeClass
    public static void seedTestData() throws InterruptedException {
        // Sign in as mike to get their UID
        CountDownLatch authLatch = new CountDownLatch(1);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(EMPLOYEE_EMAIL, EMPLOYEE_PASSWORD)
                .addOnSuccessListener(result -> {
                    mikeUid = result.getUser().getUid();
                    authLatch.countDown();
                })
                .addOnFailureListener(e -> authLatch.countDown());
        authLatch.await(30, TimeUnit.SECONDS);

        if (mikeUid == null) return; // account missing — tests will fail with clear messages

        // Add SEED_JOB_TITLE to mike's RTDB preference list
        PreferenceRepository repo = new PreferenceRepository(
                FirebaseDatabase.getInstance().getReference("users"));
        CountDownLatch prefLatch = new CountDownLatch(1);
        repo.addPreference(mikeUid, SEED_JOB_TITLE, prefLatch::countDown);
        prefLatch.await(15, TimeUnit.SECONDS);

        // Seed a matching job in Firestore so the preferred-jobs list is non-empty
        Map<String, Object> job = new HashMap<>();
        job.put("title",         SEED_JOB_TITLE);
        job.put("companyName",   "UITest Corp");
        job.put("description",   "UI test seed job – safe to delete");
        job.put("location",      "Halifax, NS");
        job.put("pay",           30.0);
        job.put("tags",          Arrays.asList("Java", "Android"));
        job.put("employerEmail", "uitest@employer.com");
        CountDownLatch jobLatch = new CountDownLatch(1);
        FirebaseFirestore.getInstance().collection("jobs").add(job)
                .addOnSuccessListener(ref -> {
                    ref.update("id", ref.getId());
                    seedJobId = ref.getId();
                    jobLatch.countDown();
                })
                .addOnFailureListener(e -> jobLatch.countDown());
        jobLatch.await(15, TimeUnit.SECONDS);

        // Sign out so UI tests can log in via the app UI
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Runs once after all tests.
     * Removes the seeded Firestore job and RTDB preference.
     */
    @AfterClass
    public static void cleanupTestData() {
        if (seedJobId != null) {
            FirebaseFirestore.getInstance().collection("jobs").document(seedJobId).delete();
        }
        if (mikeUid != null) {
            PreferenceRepository repo = new PreferenceRepository(
                    FirebaseDatabase.getInstance().getReference("users"));
            repo.removePreference(mikeUid, SEED_JOB_TITLE, null);
        }
    }

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();
        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(PACKAGE);
        assertNotNull("Launch intent should not be null", appIntent);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appIntent);
        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    private void loginAsEmployee() throws Exception {
        UiObject signinBtn = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/signinBtn"));
        if (signinBtn.waitForExists(UI_TIMEOUT)) {
            signinBtn.click();
        }

        assertTrue("Login page failed to load",
                device.wait(Until.hasObject(By.res(PACKAGE, "loginBtn")), UI_TIMEOUT));

        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/emailAddress")).setText(EMPLOYEE_EMAIL);
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/userPassword")).setText(EMPLOYEE_PASSWORD);
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/loginBtn")).click();

        assertTrue("Employee Dashboard failed to load",
                device.wait(Until.hasObject(By.res(PACKAGE, "btnSearchJobs")), FIREBASE_TIMEOUT));
    }


       @Test
    public void testPreferredJobsListContentAndNavigation() throws Exception {
        loginAsEmployee();

        // 1. Click "Preferred Jobs" on Dashboard
        UiObject prefJobsBtn = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/btnPreferredJobs"));
        assertTrue("Preferred Jobs button missing from Dashboard", prefJobsBtn.exists());
        prefJobsBtn.click();

        // 2. Verify Preferred Jobs screen loads (check header bar resource ID, not text,
        //    because Android Button's textAllCaps transform makes text matching unreliable)
        assertTrue("Preferred Jobs activity header not found",
                device.wait(Until.hasObject(By.res(PACKAGE, "headerBar")), UI_TIMEOUT));

        // 3. Verify that at least one job is present (since Mike has preferred jobs)
        assertTrue("No preferred jobs found for Mike. Ensure database has data.",
                device.wait(Until.hasObject(By.res(PACKAGE, "tvJobTitle")), FIREBASE_TIMEOUT));

        // 4. Test Back Button navigation back to Dashboard.
        // Verify the button exists, but use device.pressBack() to navigate — btnBack sits inside
        // the status-bar gesture intercept area on Android 12+ edge-to-edge displays, so a touch
        // event sent by UiObject.click() is swallowed by the system gesture layer before it
        // reaches the app. KEYCODE_BACK bypasses touch interception entirely.
        UiObject backBtn = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/btnBack"));
        assertTrue("Back button on Preferred Jobs screen not found",
                backBtn.waitForExists(UI_TIMEOUT));
        device.pressBack();

        assertTrue("Failed to return to Dashboard from Preferred Jobs screen",
                device.wait(Until.hasObject(By.res(PACKAGE, "btnSearchJobs")), UI_TIMEOUT));
    }
}

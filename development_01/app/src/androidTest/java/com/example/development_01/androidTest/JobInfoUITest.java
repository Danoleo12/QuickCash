package com.example.development_01.androidTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * US2: As an employee, I want to view the full details of a job posting
 *      so that I can decide whether to apply.
 *
 * Flow: MainActivity → LoginActivity → EmployeeActivity
 *       → SearchActivity → (chip "Chef") → PostSearchActivity
 *       → (tap job card) → PostDescription (job detail screen)
 */
@RunWith(AndroidJUnit4.class)
public class JobInfoUITest {

    private static final String TAG              = "JobInfoUITest";
    private static final long   LAUNCH_TIMEOUT   = 10_000;
    private static final long   UI_TIMEOUT       = 8_000;
    private static final long   FIREBASE_TIMEOUT = 15_000;
    private static final String PACKAGE          = "com.example.development_01";

    private static final String EMPLOYEE_EMAIL    = "tester1@employee.com";
    private static final String EMPLOYEE_PASSWORD = "Employee@123";

    /** Unique searchable prefix for the seeded job title. */
    private static final String SEARCH_QUERY = "Chef Position UITest";
    private static final String EMPLOYER_EMAIL = "tester@employer.com";
    private static final String EMPLOYER_PASSWORD = "Employer@123";

    /** Firestore document ID of the seed job created in @BeforeClass, deleted in @AfterClass */
    private static String seedJobId = null;

    private UiDevice device;

    // ─── Class-level seed data ────────────────────────────────────────────────

    @BeforeClass
    public static void createSeedJob() throws InterruptedException {
        CountDownLatch authLatch = new CountDownLatch(1);
        final boolean[] authSucceeded = {false};
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(EMPLOYER_EMAIL, EMPLOYER_PASSWORD)
                .addOnSuccessListener(authResult -> {
                    authSucceeded[0] = true;
                    authLatch.countDown();
                })
                .addOnFailureListener(e -> authLatch.countDown());
        authLatch.await(15, TimeUnit.SECONDS);
        if (!authSucceeded[0]) {
            throw new AssertionError("Failed to authenticate test employer before seeding jobs");
        }

        String seededTitle = SEARCH_QUERY + " " + System.currentTimeMillis();
        Map<String, Object> job = new HashMap<>();
        job.put("title", seededTitle);
        job.put("companyName", "Test Restaurant");
        job.put("description", "UI test seed job – safe to delete");
        job.put("location", "Halifax, NS");
        job.put("pay", 20.0);
        job.put("tags", Arrays.asList("Chef", "Cooking", "Food"));
        job.put("employerEmail", EMPLOYER_EMAIL);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] seedSucceeded = {false};
        FirebaseFirestore.getInstance()
                .collection("jobs")
                .add(job)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference ref) {
                        ref.update("id", ref.getId())
                                .addOnSuccessListener(unused -> {
                                    seedJobId = ref.getId();
                                    seedSucceeded[0] = true;
                                    latch.countDown();
                                })
                                .addOnFailureListener(e -> latch.countDown());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        latch.countDown();
                    }
                });
        latch.await(15, TimeUnit.SECONDS);
        if (!seedSucceeded[0] || seedJobId == null) {
            throw new AssertionError("Failed to seed Firestore job for JobInfoUITest");
        }

        // Give Firestore indexing/propagation a moment before the first UI query.
        Thread.sleep(2000);
    }

    @AfterClass
    public static void deleteSeedJob() {
        if (seedJobId != null) {
            FirebaseFirestore.getInstance()
                    .collection("jobs")
                    .document(seedJobId)
                    .delete();
        }
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();
        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(PACKAGE);
        assertNotNull("Launch intent must not be null", appIntent);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appIntent);
        device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    // ─── Diagnostic helper ───────────────────────────────────────────────────

    private void dumpHierarchy(String tag) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            device.dumpWindowHierarchy(baos);
            String xml = baos.toString("UTF-8");
            Log.d(TAG, "[DUMP:" + tag + "]\n" + xml);
            File dir = new File(
                    "/sdcard/Android/media/" + PACKAGE + "/additional_test_output");
            dir.mkdirs();
            File f = new File(dir, "hierarchy_" + tag + ".xml");
            FileWriter fw = new FileWriter(f);
            fw.write(xml);
            fw.close();
        } catch (Exception e) {
            Log.e(TAG, "dump failed: " + e.getMessage());
        }
    }

    // ─── Navigation helpers ──────────────────────────────────────────────────

    /**
     * Signs in as the test employee account and waits for the dashboard to appear.
     */
    private void loginAsEmployee() throws Exception {
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/signinBtn")).click();

        assertTrue("Login page failed to load",
                device.wait(Until.hasObject(By.res(PACKAGE, "loginBtn")), UI_TIMEOUT));

        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/emailAddress"))
                .setText(EMPLOYEE_EMAIL);
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/userPassword"))
                .setText(EMPLOYEE_PASSWORD);
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/loginBtn")).click();

        assertTrue("Employee dashboard failed to load",
                device.wait(Until.hasObject(By.res(PACKAGE, "btnSearchJobs")), FIREBASE_TIMEOUT));
    }

    /**
     * Taps "Search Jobs" and waits for SearchActivity.
     */
    private void navigateToSearch() throws Exception {
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/btnSearchJobs")).click();
        assertTrue("Search page failed to load",
                device.wait(Until.hasObject(By.res(PACKAGE, "searchView")), UI_TIMEOUT));
        device.waitForIdle();
    }

    /**
     * Submits a SearchView keyword and waits for PostSearchActivity to load.
     */
    private void submitSearchAndWaitForResults(String queryText) throws Exception {
        device.waitForIdle();

        UiObject searchButton = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/search_button"));
        if (searchButton.waitForExists(UI_TIMEOUT)) {
            searchButton.click();
            device.waitForIdle();
        }

        UiObject searchInput = device.findObject(new UiSelector().resourceId(PACKAGE + ":id/search_src_text"));
        assertTrue("Search input not found", searchInput.waitForExists(UI_TIMEOUT));
        searchInput.click();
        searchInput.clearTextField();
        searchInput.setText(queryText);
        device.waitForIdle();

        dumpHierarchy("before_search_submit_" + queryText.replace(' ', '_'));
        device.pressEnter();

        assertTrue("PostSearchActivity did not load after submitting search '" + queryText + "'",
                device.wait(Until.hasObject(By.res(PACKAGE, "tvSearchQueryContext")), UI_TIMEOUT));
    }

    /**
     * Taps the first visible job card in the RecyclerView and waits for
     * PostDescription (job detail screen) to load.
     *
     * PostDescription is identified by the presence of jobTitleText, which is
     * the TextInputEditText that receives the job title in activity_job_detail.xml.
     */
    private void openFirstJobCard() throws Exception {
        // Wait for at least one card's title to appear in the RecyclerView
        assertTrue("No job cards appeared in search results",
                device.wait(Until.hasObject(By.res(PACKAGE, "tvJobTitle")), FIREBASE_TIMEOUT));

        dumpHierarchy("before_open_job_card");

        UiObject firstCard = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/tvJobTitle").instance(0));
        assertTrue("First job card not found", firstCard.exists());

        firstCard.clickAndWaitForNewWindow(UI_TIMEOUT);

        dumpHierarchy("after_open_job_card");

        // PostDescription renders the job title into jobTitleText (TextInputEditText)
        assertTrue("Job detail screen (PostDescription) did not open",
                device.wait(Until.hasObject(By.res(PACKAGE, "jobTitleText")), UI_TIMEOUT));
    }

    // ─── Full navigation helper ───────────────────────────────────────────────

    /**
     * Performs the full navigation:
     * Login → Dashboard → Search → keyword search → Results → First card → Detail screen
     */
    private void navigateToJobDetail() throws Exception {
        loginAsEmployee();
        navigateToSearch();
        submitSearchAndWaitForResults(SEARCH_QUERY);
        openFirstJobCard();
    }

    // ─── Tests ───────────────────────────────────────────────────────────────

    /**
     * AC1 — Tapping a job card opens the detail screen.
     * Verifies the PostDescription activity is displayed after tapping a card.
     */
    @Test
    public void testTapJobCardOpensDetailScreen() throws Exception {
        loginAsEmployee();
        navigateToSearch();
        submitSearchAndWaitForResults(SEARCH_QUERY);

        assertTrue("Job cards must appear before tapping one",
                device.wait(Until.hasObject(By.res(PACKAGE, "tvJobTitle")), FIREBASE_TIMEOUT));

        UiObject firstCard = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/tvJobTitle").instance(0));
        assertTrue("First job card not found", firstCard.exists());
        firstCard.clickAndWaitForNewWindow(UI_TIMEOUT);

        assertTrue("Detail screen did not open after tapping job card",
                device.wait(Until.hasObject(By.res(PACKAGE, "jobTitleText")), UI_TIMEOUT));
    }

    /**
     * AC2 — Job title is displayed on the detail screen and is not empty.
     */
    @Test
    public void testJobDetailDisplaysTitle() throws Exception {
        navigateToJobDetail();

        UiObject titleField = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/jobTitleText"));
        assertTrue("jobTitleText field is missing", titleField.waitForExists(UI_TIMEOUT));

        String titleText = titleField.getText();
        assertNotNull("Job title text is null", titleText);
        assertFalse("Job title should not be empty", titleText.trim().isEmpty());

        Log.d(TAG, "Job title displayed: " + titleText);
    }

    /**
     * AC3 — Job description is displayed on the detail screen and is not empty.
     */
    @Test
    public void testJobDetailDisplaysDescription() throws Exception {
        navigateToJobDetail();

        UiObject descriptionField = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/jobDescriptionText"));
        assertTrue("jobDescriptionText field is missing", descriptionField.waitForExists(UI_TIMEOUT));

        String descriptionText = descriptionField.getText();
        assertNotNull("Job description text is null", descriptionText);
        assertFalse("Job description should not be empty", descriptionText.trim().isEmpty());

        Log.d(TAG, "Job description displayed: " + descriptionText);
    }

    /**
     * AC4 — Location is displayed on the detail screen and is not empty.
     */
    @Test
    public void testJobDetailDisplaysLocation() throws Exception {
        navigateToJobDetail();

        UiObject locationField = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/jobLocationText"));
        assertTrue("jobLocationText field is missing", locationField.waitForExists(UI_TIMEOUT));

        String locationText = locationField.getText();
        assertNotNull("Location text is null", locationText);
        assertFalse("Location should not be empty", locationText.trim().isEmpty());

        Log.d(TAG, "Location displayed: " + locationText);
    }

    /**
     * AC5 — Pay is displayed on the detail screen and is not empty.
     */
    @Test
    public void testJobDetailDisplaysPay() throws Exception {
        navigateToJobDetail();

        UiObject payField = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/payText"));
        assertTrue("payText field is missing", payField.waitForExists(UI_TIMEOUT));

        String payText = payField.getText();
        assertNotNull("Pay text is null", payText);
        assertFalse("Pay should not be empty", payText.trim().isEmpty());

        Log.d(TAG, "Pay displayed: " + payText);
    }

    /**
     * AC6 — Tags chip group is present on the detail screen.
     */
    @Test
    public void testJobDetailDisplaysTags() throws Exception {
        navigateToJobDetail();

        assertTrue("tagChipGroup is missing on job detail screen",
                device.wait(Until.hasObject(By.res(PACKAGE, "tagChipGroup")), UI_TIMEOUT));

        Log.d(TAG, "Tag chip group found on detail screen");
    }

    /**
     * AC7 — All key detail fields are present together in a single traversal.
     * Combines checks for title, description, location, pay, and tags.
     */
    @Test
    public void testJobDetailScreenShowsAllFields() throws Exception {
        navigateToJobDetail();

        dumpHierarchy("job_detail_all_fields");

        assertTrue("jobTitleText missing",
                device.findObject(new UiSelector().resourceId(PACKAGE + ":id/jobTitleText"))
                        .waitForExists(UI_TIMEOUT));

        assertTrue("jobDescriptionText missing",
                device.findObject(new UiSelector().resourceId(PACKAGE + ":id/jobDescriptionText"))
                        .waitForExists(UI_TIMEOUT));

        assertTrue("jobLocationText missing",
                device.findObject(new UiSelector().resourceId(PACKAGE + ":id/jobLocationText"))
                        .waitForExists(UI_TIMEOUT));

        assertTrue("payText missing",
                device.findObject(new UiSelector().resourceId(PACKAGE + ":id/payText"))
                        .waitForExists(UI_TIMEOUT));

        assertTrue("tagChipGroup missing",
                device.wait(Until.hasObject(By.res(PACKAGE, "tagChipGroup")), UI_TIMEOUT));
    }

    /**
     * AC8 — Apply button is visible on the detail screen for a job not yet applied to.
     */
    @Test
    public void testJobDetailShowsApplyButton() throws Exception {
        navigateToJobDetail();

        assertTrue("applyBtn is missing on job detail screen",
                device.wait(Until.hasObject(By.res(PACKAGE, "applyBtn")), UI_TIMEOUT));

        UiObject applyBtn = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/applyBtn"));
        assertTrue("Apply button should be visible", applyBtn.isEnabled());

        Log.d(TAG, "Apply button is visible and enabled");
    }

    /**
     * AC9 — Back button on the detail screen returns the user to the search results list.
     */
    @Test
    public void testBackButtonReturnsToSearchResults() throws Exception {
        navigateToJobDetail();

        assertTrue("jobDetailBackButton not found",
                device.wait(Until.hasObject(By.res(PACKAGE, "jobDetailBackButton")), UI_TIMEOUT));

        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/jobDetailBackButton")).click();
        device.waitForIdle();

        dumpHierarchy("after_detail_back_button");

        // Detail screen should be gone
        boolean detailGone = device.wait(
                Until.gone(By.res(PACKAGE, "jobTitleText")), UI_TIMEOUT);
        assertTrue("Job detail screen did not close", detailGone);

        // Search results list should be visible again
        assertTrue("Search results (PostSearchActivity) did not reappear",
                device.wait(Until.hasObject(By.res(PACKAGE, "tvSearchQueryContext")), UI_TIMEOUT));
    }

    /**
     * AC10 — "Already Applied" indicator is hidden when the user has not yet applied.
     * The alreadyAppliedIndicator TextView has visibility="gone" by default.
     */
    @Test
    public void testAlreadyAppliedIndicatorHiddenByDefault() throws Exception {
        navigateToJobDetail();

        // The indicator is visibility="gone" in XML; it should NOT appear in the tree
        boolean indicatorVisible = device.findObject(
                new UiSelector().resourceId(PACKAGE + ":id/alreadyAppliedIndicator")).exists();

        assertFalse("alreadyAppliedIndicator should be hidden for a new job", indicatorVisible);

        Log.d(TAG, "alreadyAppliedIndicator correctly hidden");
    }
}

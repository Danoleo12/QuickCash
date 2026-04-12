package com.example.development_01.androidTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;

/**
 * US1: As an employee, I want to search for jobs on the application
 *      so that I can find relevant job opportunities.
 */
@RunWith(AndroidJUnit4.class)
public class JobSearchUIAutomatorTest {

    private static final String TAG              = "JobSearchUITest";
    private static final long   LAUNCH_TIMEOUT   = 10000;
    private static final long   UI_TIMEOUT       = 8000;
    private static final long   FIREBASE_TIMEOUT = 15000;
    private static final String PACKAGE          = "com.example.development_01";

    private static final String EMPLOYEE_EMAIL    = "tester1@employee.com";
    private static final String EMPLOYEE_PASSWORD = "Employee@123";

    private UiDevice device;

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

    // ─── Diagnostic helpers ──────────────────────────────────────────────────

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

    private void loginAsEmployee() throws Exception {
        //assertFalse("MainActivity sign-in button not found",
            // device.wait(Until.hasObject(By.res(PACKAGE, "signinBtn")), UI_TIMEOUT));

        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/signinBtn")).click();

        assertTrue("Login page failed to load",
            device.wait(Until.hasObject(By.res(PACKAGE, "loginBtn")), UI_TIMEOUT));

        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/emailAddress"))
              .setText(EMPLOYEE_EMAIL);
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/userPassword"))
              .setText(EMPLOYEE_PASSWORD);
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/loginBtn")).click();

        assertTrue("Dashboard failed to load",
            device.wait(Until.hasObject(By.res(PACKAGE, "btnSearchJobs")), FIREBASE_TIMEOUT));
    }

    private void navigateToSearch() throws Exception {
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/btnSearchJobs")).click();
        assertTrue("Search page failed to load",
            device.wait(Until.hasObject(By.res(PACKAGE, "searchView")), UI_TIMEOUT));
        device.waitForIdle();
    }

    /**
     * Types a query into the AppCompat SearchView and submits with Enter.
     *
     * Root cause found via hierarchy dump: the SearchView is in ICONIFIED state.
     * Only `search_button` (resource com.example.development_01:id/search_button,
     * the magnifying glass icon) is in the accessibility tree — the text field
     * (search_src_text) is hidden. Clicking the outer SearchView container does nothing
     * because the container is not clickable.
     *
     * Fix:
     *   1. Click search_button to expand the SearchView (setIconified(false)).
     *   2. Wait for search_src_text to appear in the tree.
     *   3. Use UiObject.setText() / sendStringSync + pressEnter to submit.
     */
    private void typeAndSubmitSearch(String query) throws Exception {
        // Step 1: expand the SearchView by clicking its search icon button
        UiObject searchButton = device.findObject(
            new UiSelector().resourceId(PACKAGE + ":id/search_button"));
        if (searchButton.waitForExists(UI_TIMEOUT)) {
            searchButton.click();
            device.waitForIdle();
        } else {
            // Already expanded — fall through
        }

        // Step 2: find the now-visible inner text field.
        // After expansion, AppCompat exposes search_src_text in the tree.
        UiObject searchInput = device.findObject(
            new UiSelector().resourceId(PACKAGE + ":id/search_src_text"));
        if (!searchInput.waitForExists(UI_TIMEOUT)) {
            // Fallback: type directly into whatever has focus
            InstrumentationRegistry.getInstrumentation().sendStringSync(query);
            device.pressEnter();
            assertTrue("PostSearchActivity failed to load after keyword search",
                device.wait(Until.hasObject(By.res(PACKAGE, "tvSearchQueryContext")), UI_TIMEOUT));
            return;
        }

        searchInput.click(); // ensure focus before typing
        searchInput.clearTextField();
        searchInput.setText(query);
        device.waitForIdle();
        device.pressEnter();

        assertTrue("PostSearchActivity failed to load after keyword search",
            device.wait(Until.hasObject(By.res(PACKAGE, "tvSearchQueryContext")), UI_TIMEOUT));
    }

    /**
     * Clicks a suggestion chip and waits for PostSearchActivity to load.
     *
     * The chips are rendered as android.widget.Button nodes with clickable=true.
     * They have no resource IDs in XML, only text.
     *
     * We use UiObject.clickAndWaitForNewWindow() which is purpose-built for taps that
     * open a new Activity window (TYPE_WINDOW_STATE_CHANGED accessibility event).
     * If that times out we add a final hasObject wait as a backstop.
     */
    private void clickChipAndWaitForResults(String chipText) throws Exception {
        device.waitForIdle();

        assertTrue("Chip '" + chipText + "' not found",
            device.wait(Until.hasObject(By.text(chipText)), UI_TIMEOUT));

        UiObject chip = device.findObject(new UiSelector().text(chipText));
        assertTrue("Chip '" + chipText + "' UiObject missing", chip.exists());

        dumpHierarchy("before_chip_" + chipText.replace(' ', '_'));

        // clickAndWaitForNewWindow blocks until a window-state-changed event fires
        // (i.e., the new PostSearchActivity window appears).
        chip.clickAndWaitForNewWindow(UI_TIMEOUT);

        dumpHierarchy("after_chip_" + chipText.replace(' ', '_'));

        assertTrue("PostSearchActivity failed to load after chip click on '" + chipText + "'",
            device.wait(Until.hasObject(By.res(PACKAGE, "tvSearchQueryContext")), UI_TIMEOUT));
    }

    // ─── Tests ───────────────────────────────────────────────────────────────

    @Test
    public void testEmployeeCanAccessSearchPage() throws Exception {
        loginAsEmployee();
        navigateToSearch();
        assertTrue("SearchView should be visible",
            device.findObject(new UiSelector().resourceId(PACKAGE + ":id/searchView")).exists());
    }

    @Test
    public void testSearchPageElementsAreVisible() throws Exception {
        loginAsEmployee();
        navigateToSearch();

        dumpHierarchy("search_page_elements");

        assertTrue("SearchView should exist",
            device.findObject(new UiSelector().resourceId(PACKAGE + ":id/searchView")).exists());
        assertTrue("Back button should exist",
            device.findObject(new UiSelector().resourceId(PACKAGE + ":id/imageButton")).exists());
        assertTrue("Chip group should exist",
            device.findObject(new UiSelector().resourceId(PACKAGE + ":id/chipGroupSuggestions")).exists());
        assertTrue("AI chip missing",
            device.wait(Until.hasObject(By.text("AI Development Co-op")), UI_TIMEOUT));
    }

    @Test
    public void testKeywordSearch_AC3() throws Exception {
        loginAsEmployee();
        navigateToSearch();
        typeAndSubmitSearch("AI");

        UiObject resultContext = device.findObject(
            new UiSelector().resourceId(PACKAGE + ":id/tvSearchQueryContext"));
        assertTrue("tvSearchQueryContext missing", resultContext.exists());
        assertTrue("Header text should contain 'AI'", resultContext.getText().contains("AI"));
    }

    @Test
    public void testChipClickSearch_Chef_AC1() throws Exception {
        loginAsEmployee();
        navigateToSearch();
        clickChipAndWaitForResults("Chef");

        UiObject resultContext = device.findObject(
            new UiSelector().resourceId(PACKAGE + ":id/tvSearchQueryContext"));
        assertTrue("tvSearchQueryContext missing", resultContext.exists());
        assertTrue("Header should contain 'Chef'", resultContext.getText().contains("Chef"));
    }

    @Test
    public void testChipClickSearch_DataScience_AC1() throws Exception {
        loginAsEmployee();
        navigateToSearch();
        clickChipAndWaitForResults("Data Science");

        UiObject resultContext = device.findObject(
            new UiSelector().resourceId(PACKAGE + ":id/tvSearchQueryContext"));
        assertTrue("tvSearchQueryContext missing", resultContext.exists());
        assertTrue("Header should contain 'Data Science'",
            resultContext.getText().contains("Data Science"));
    }

    @Test
    public void testEmptyStateDisplayed_AC5() throws Exception {
        loginAsEmployee();
        navigateToSearch();
        dumpHierarchy("before_empty_search");
        typeAndSubmitSearch("xyznonexistent999");

        assertTrue("Empty state layout not visible",
            device.wait(Until.hasObject(By.res(PACKAGE, "layoutEmptyState")), FIREBASE_TIMEOUT));

        UiObject emptyTitle = device.findObject(
            new UiSelector().resourceId(PACKAGE + ":id/tvEmptyStateTitle"));
        assertTrue("tvEmptyStateTitle missing", emptyTitle.waitForExists(UI_TIMEOUT));
        assertTrue("Empty state title should contain the query",
            emptyTitle.getText().contains("xyznonexistent999"));
    }

    /**
     * Tests that the custom back ImageButton (which calls finish()) returns to EmployeeActivity.
     *
     * Additional debugging: dump hierarchy after clicking to confirm what is on screen.
     */
    @Test
    public void testBackButtonReturnsToDashboard() throws Exception {
        loginAsEmployee();
        navigateToSearch();

        dumpHierarchy("before_back_button");

        assertTrue("imageButton not found",
            device.findObject(new UiSelector().resourceId(PACKAGE + ":id/imageButton")).exists());

        // Click the custom back button which calls SearchActivity.finish()
        device.findObject(new UiSelector().resourceId(PACKAGE + ":id/imageButton")).click();
        device.waitForIdle();

        // Give it a moment and dump what's on screen
        Thread.sleep(1000);
        dumpHierarchy("after_back_button");

        // Wait for SearchActivity to disappear, then for EmployeeActivity to appear
        boolean searchGone = device.wait(Until.gone(By.res(PACKAGE, "searchView")), UI_TIMEOUT);
        assertTrue("SearchActivity did not finish (searchView still visible)", searchGone);

        assertTrue("Failed to return to EmployeeActivity dashboard",
            device.wait(Until.hasObject(By.res(PACKAGE, "btnSearchJobs")), UI_TIMEOUT));
    }

    @Test
    public void testSearchResultsShowJobDetails_AC2_AC4() throws Exception {
        loginAsEmployee();
        navigateToSearch();

        clickChipAndWaitForResults("AI Development Co-op");

        boolean hasResults =
            device.wait(Until.hasObject(By.res(PACKAGE, "tvJobTitle")), FIREBASE_TIMEOUT);
        boolean hasEmptyState =
            device.findObject(new UiSelector().resourceId(PACKAGE + ":id/layoutEmptyState")).exists();

        assertTrue("Neither job results nor empty state appeared", hasResults || hasEmptyState);

        if (hasResults) {
            assertTrue("tvCompanyName missing",
                device.findObject(new UiSelector().resourceId(PACKAGE + ":id/tvCompanyName")).exists());
            assertTrue("tvLocation missing",
                device.findObject(new UiSelector().resourceId(PACKAGE + ":id/tvLocation")).exists());
            assertTrue("tagChipGroup missing",
                device.findObject(new UiSelector().resourceId(PACKAGE + ":id/tagChipGroup")).exists());
        }
    }
}

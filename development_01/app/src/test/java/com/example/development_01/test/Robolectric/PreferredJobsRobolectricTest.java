package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.EmployeeActivity;
import com.example.development_01.core.ui.PostDescription;
import com.example.development_01.core.ui.PreferredJobsActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

/**
 * Robolectric UI tests for the Job Preference feature.
 *
 * Covers three surfaces:
 *  1. PostDescription  – star (prefer) button existence, visibility, and toggle
 *  2. EmployeeActivity – "Preferred Jobs" button navigates to PreferredJobsActivity
 *  3. PreferredJobsActivity – layout components and empty-state behaviour
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {30})
public class PreferredJobsRobolectricTest {

    @Before
    public void initFirebase() {
        Context context = ApplicationProvider.getApplicationContext();
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:1234567890:android:abcdef123456")
                        .setApiKey("fake-api-key")
                        .setProjectId("fake-project-id")
                        .setDatabaseUrl("https://fake-project-id-default-rtdb.firebaseio.com")
                        .build();
                FirebaseApp.initializeApp(context, options);
            }
        } catch (Exception ignored) {}
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Intent postDescriptionIntent(String jobTitle) {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(), PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_TITLE, jobTitle);
        intent.putExtra(PostDescription.EXTRA_JOB_ID,    "job_001");
        return intent;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. PostDescription – star button
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The prefer-job star button must be present and visible on the job-detail screen.
     */
    @Test
    public void testPreferJobButtonIsVisible() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(postDescriptionIntent("Software Developer"))) {
            scenario.onActivity(activity -> {
                View btn = activity.findViewById(R.id.preferJobBtn);
                assertNotNull("preferJobBtn must exist in activity_job_detail layout", btn);
                assertEquals(View.VISIBLE, btn.getVisibility());
            });
        }
    }

    /**
     * The star button must have a click listener attached (isClickable returns true).
     */
    @Test
    public void testPreferJobButtonIsClickable() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(postDescriptionIntent("Software Developer"))) {
            scenario.onActivity(activity -> {
                View btn = activity.findViewById(R.id.preferJobBtn);
                assertNotNull(btn);
                assertTrue("preferJobBtn must be clickable", btn.isClickable());
            });
        }
    }

    /**
     * Before any interaction the star must be in the un-preferred state.
     * The implementation signals state via the button's tag:
     *   Boolean.FALSE  →  not preferred (unfilled star)
     *   Boolean.TRUE   →  preferred     (filled star)
     */
    @Test
    public void testPreferJobButtonInitialStateIsNotPreferred() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(postDescriptionIntent("Software Developer"))) {
            scenario.onActivity(activity -> {
                ImageButton btn = activity.findViewById(R.id.preferJobBtn);
                assertNotNull(btn);
                assertEquals("Star must start in un-preferred (false) state",
                        Boolean.FALSE, btn.getTag());
            });
        }
    }

    /**
     * Clicking the star once must switch its state to preferred (tag = true).
     */
    @Test
    public void testClickingPreferJobButtonTogglesStateToPreferred() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(postDescriptionIntent("Software Developer"))) {
            scenario.onActivity(activity -> {
                ImageButton btn = activity.findViewById(R.id.preferJobBtn);
                assertNotNull(btn);

                btn.performClick();

                assertEquals("After first click the star must be in preferred (true) state",
                        Boolean.TRUE, btn.getTag());
            });
        }
    }

    /**
     * Clicking the star a second time must toggle it back to un-preferred (tag = false).
     */
    @Test
    public void testClickingPreferJobButtonAgainTogglesStateBackToNotPreferred() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(postDescriptionIntent("Software Developer"))) {
            scenario.onActivity(activity -> {
                ImageButton btn = activity.findViewById(R.id.preferJobBtn);
                assertNotNull(btn);

                btn.performClick(); // → preferred
                btn.performClick(); // → un-preferred

                assertEquals("After two clicks the star must return to un-preferred (false) state",
                        Boolean.FALSE, btn.getTag());
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. EmployeeActivity – navigation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The "Preferred Jobs" button must be present and visible on the employee dashboard.
     */
    @Test
    public void testPreferredJobsButtonExistsOnDashboard() {
        try (ActivityScenario<EmployeeActivity> scenario =
                     ActivityScenario.launch(EmployeeActivity.class)) {
            scenario.onActivity(activity -> {
                View btn = activity.findViewById(R.id.btnPreferredJobs);
                assertNotNull("btnPreferredJobs must exist in activity_employee layout", btn);
                assertEquals(View.VISIBLE, btn.getVisibility());
            });
        }
    }

    /**
     * Clicking "Preferred Jobs" on the dashboard must start PreferredJobsActivity.
     */
    @Test
    public void testClickingPreferredJobsButtonOpensPreferredJobsActivity() {
        try (ActivityScenario<EmployeeActivity> scenario =
                     ActivityScenario.launch(EmployeeActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btnPreferredJobs).performClick();

                Intent started = shadowOf(activity).getNextStartedActivity();
                assertNotNull("A new activity must be started on button click", started);
                assertEquals(
                        "PreferredJobsActivity must be the target",
                        PreferredJobsActivity.class.getName(),
                        started.getComponent().getClassName());
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. PreferredJobsActivity – layout and empty-state behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The RecyclerView for displaying preferred jobs must exist in the layout.
     */
    @Test
    public void testPreferredJobsActivityHasRecyclerView() {
        try (ActivityScenario<PreferredJobsActivity> scenario =
                     ActivityScenario.launch(PreferredJobsActivity.class)) {
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.recyclerViewPreferredJobs);
                assertNotNull("recyclerViewPreferredJobs must exist in the layout", rv);
            });
        }
    }

    /**
     * The empty-state layout must exist in the layout file.
     */
    @Test
    public void testPreferredJobsActivityHasEmptyStateLayout() {
        try (ActivityScenario<PreferredJobsActivity> scenario =
                     ActivityScenario.launch(PreferredJobsActivity.class)) {
            scenario.onActivity(activity -> {
                View emptyState = activity.findViewById(R.id.layoutEmptyState);
                assertNotNull("layoutEmptyState must exist in the layout", emptyState);
            });
        }
    }

    /**
     * The progress bar must exist in the layout file.
     */
    @Test
    public void testPreferredJobsActivityHasProgressBar() {
        try (ActivityScenario<PreferredJobsActivity> scenario =
                     ActivityScenario.launch(PreferredJobsActivity.class)) {
            scenario.onActivity(activity -> {
                View progressBar = activity.findViewById(R.id.progressBar);
                assertNotNull("progressBar must exist in the layout", progressBar);
            });
        }
    }

    /**
     * The back button must exist in the layout file.
     */
    @Test
    public void testPreferredJobsActivityHasBackButton() {
        try (ActivityScenario<PreferredJobsActivity> scenario =
                     ActivityScenario.launch(PreferredJobsActivity.class)) {
            scenario.onActivity(activity -> {
                View backBtn = activity.findViewById(R.id.btnBack);
                assertNotNull("btnBack must exist in the layout", backBtn);
            });
        }
    }

    /**
     * When no user is logged in (test environment), preferences cannot be fetched,
     * so the empty-state view must be shown instead of the RecyclerView.
     */
    @Test
    public void testPreferredJobsActivityShowsEmptyStateWhenNoUserLoggedIn() {
        try (ActivityScenario<PreferredJobsActivity> scenario =
                     ActivityScenario.launch(PreferredJobsActivity.class)) {
            scenario.onActivity(activity -> {
                ShadowLooper.idleMainLooper();

                View emptyState = activity.findViewById(R.id.layoutEmptyState);
                assertEquals(
                        "Empty state must be VISIBLE when there is no logged-in user",
                        View.VISIBLE, emptyState.getVisibility());

                View recyclerView = activity.findViewById(R.id.recyclerViewPreferredJobs);
                assertEquals(
                        "RecyclerView must be GONE when empty state is showing",
                        View.GONE, recyclerView.getVisibility());
            });
        }
    }

    /**
     * Clicking the back button must finish the activity.
     */
    @Test
    public void testPreferredJobsBackButtonFinishesActivity() {
        try (ActivityScenario<PreferredJobsActivity> scenario =
                     ActivityScenario.launch(PreferredJobsActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.btnBack).performClick();
                assertTrue("Activity must finish when the back button is clicked",
                        activity.isFinishing());
            });
        }
    }
}

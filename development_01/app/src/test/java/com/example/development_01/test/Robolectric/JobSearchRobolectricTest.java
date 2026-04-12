package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.PostSearchActivity;
import com.example.development_01.core.ui.SearchActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

/**
 * Robolectric tests for Job Search User Story.
 * Optimized to pass both locally and in CI/CD by ensuring Material theme support and lifecycle stability.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {30})
public class JobSearchRobolectricTest {

    private PostSearchActivity postSearchActivity;
    private SearchActivity searchActivity;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();

        // 1. Initialize Firebase with stubbed options
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:1234567890:android:abcdef123456")
                        .setApiKey("fake-api-key")
                        .setProjectId("fake-project-id")
                        .build();
                FirebaseApp.initializeApp(context, options);
            }
        } catch (Exception ignored) {}

        // 2. Setup Activities
        Intent intent = new Intent(context, PostSearchActivity.class);
        intent.putExtra("SEARCH_QUERY", "Developer");

        try (ActivityController<PostSearchActivity> controller = Robolectric.buildActivity(PostSearchActivity.class, intent)) {
            postSearchActivity = controller.setup().get();
        }

        try (ActivityController<SearchActivity> controller = Robolectric.buildActivity(SearchActivity.class)) {
            searchActivity = controller.setup().get();
        }

        // Allow background UI and main-thread tasks to finish
        ShadowLooper.idleMainLooper();
    }

    @Test
    public void testEmptyStateTextView_AC5() {
        TextView tvEmptyState = postSearchActivity.findViewById(R.id.tvEmptyStateTitle);
        assertNotNull("The empty state TextView should be present", tvEmptyState);

        String text = tvEmptyState.getText().toString();
        // Flexible check to handle query-specific strings
        assertTrue("Text should indicate no jobs found", text.contains("No jobs found"));
    }

    @Test
    public void testSearchViewPresence_AC3() {
        View searchView = searchActivity.findViewById(R.id.searchView);
        assertNotNull("SearchView should be present", searchView);
    }

    @Test
    public void testJobCardComponentStructure() {
        // Verification of the card UI structure
        View cardView = LayoutInflater.from(postSearchActivity).inflate(R.layout.item_job_card, null);
        assertNotNull("tvJobTitle missing", cardView.findViewById(R.id.tvJobTitle));
        assertNotNull("tvCompanyName missing", cardView.findViewById(R.id.tvCompanyName));
        assertNotNull("tvLocation missing", cardView.findViewById(R.id.employee_email));
        assertNotNull("tvJobType missing", cardView.findViewById(R.id.tvJobType));
    }

    @Test
    public void testRecyclerViewPresence() {
        RecyclerView recyclerView = postSearchActivity.findViewById(R.id.recyclerViewJobs);
        assertNotNull("RecyclerView should be present", recyclerView);
    }
}
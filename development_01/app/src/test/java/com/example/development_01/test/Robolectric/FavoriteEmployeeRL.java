package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.JobApplicationsActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Robolectric tests for the favorite-employee feature.
 *
 * Verifies that the UI components required by the like-button handler are
 * present and correctly wired, without needing a live Firebase connection.
 */
@RunWith(RobolectricTestRunner.class)
public class FavoriteEmployeeRL {

    private JobApplicationsActivity activity;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();

        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setApiKey("fake-api-key")
                    .setProjectId("fake-project-id")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }

        Intent intent = new Intent();
        intent.putExtra("JOB_ID", "job_test_01");
        intent.putExtra("JOB_TITLE", "Android Developer");
        intent.putExtra("JOB_DESCRIPTION", "Build mobile apps");
        intent.putExtra("JOB_PAY", 30.0);
        intent.putExtra("JOB_LOCATION", "Halifax");

        activity = Robolectric.buildActivity(JobApplicationsActivity.class, intent)
                .create().start().resume().get();
    }

    // ── Test 1: likeButton exists in the application card layout ─────────────

    @Test
    public void testLikeButtonExistsInCardLayout() {
        // Inflate the same card layout the adapter uses and confirm the like
        // button (R.id.likeButton) is present — this is the view the feature
        // attaches its OnClickListener to.
        View card = LayoutInflater.from(activity)
                .inflate(R.layout.item_application_card, null, false);
        View likeButton = card.findViewById(R.id.likeButton);
        assertNotNull("likeButton must exist in item_application_card layout", likeButton);
    }

    // ── Test 2: likeButton is visible (not hidden or gone) by default ─────────

    @Test
    public void testLikeButtonIsVisibleByDefault() {
        // The button should be VISIBLE so that employers can tap it.
        // If it were GONE or INVISIBLE the feature would be inaccessible.
        View card = LayoutInflater.from(activity)
                .inflate(R.layout.item_application_card, null, false);
        View likeButton = card.findViewById(R.id.likeButton);
        assertEquals(View.VISIBLE, likeButton.getVisibility());
    }
}

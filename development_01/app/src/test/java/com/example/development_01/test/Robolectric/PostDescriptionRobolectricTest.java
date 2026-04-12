package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.PostDescription;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Robolectric tests for {@link PostDescription}.
 *
 * Requires the Android framework (ActivityScenario, View, Intent, ChipGroup).
 * Covers every behaviour that can only be verified by launching the real Activity:
 *
 *  - All five data fields render correctly into their Views
 *  - Null / missing extras fall back to the correct placeholder strings
 *  - Apply button and alreadyAppliedIndicator visibility driven by EXTRA_ALREADY_APPLIED
 *  - tagChipGroup child count and chip texts after tag inflation
 *  - Back button calls finish()
 *  - Apply button fires startActivityForResult with correct Intent extras
 *  - onActivityResult toggles button/indicator visibility correctly
 *  - Wrong requestCode in onActivityResult is ignored
 *  - RESULT_CANCELED in onActivityResult leaves state unchanged
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PostDescriptionRobolectricTest {

    // ─── Helper: fully-populated launch Intent ────────────────────────────────

    private Intent fullIntent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_TITLE,       "Head Chef");
        intent.putExtra(PostDescription.EXTRA_JOB_DESCRIPTION, "Manage the kitchen brigade.");
        intent.putExtra(PostDescription.EXTRA_LOCATION,         "Toronto, ON");
        intent.putExtra(PostDescription.EXTRA_PAY,              "25.5");
        intent.putExtra(PostDescription.EXTRA_TAGS,             "Cooking, Leadership, Food Safety");
        intent.putExtra(PostDescription.EXTRA_JOB_ID,           "job_abc123");
        intent.putExtra(PostDescription.EXTRA_ALREADY_APPLIED,  false);
        return intent;
    }

    // ─── Field rendering ──────────────────────────────────────────────────────

    /**
     * EXTRA_JOB_TITLE is displayed verbatim in jobTitleText.
     */
    @Test
    public void testJobTitleIsDisplayed() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.jobTitleText);
                assertNotNull("jobTitleText must exist in layout", tv);
                assertEquals("Head Chef", tv.getText().toString());
            });
        }
    }

    /**
     * EXTRA_JOB_DESCRIPTION is displayed verbatim in jobDescriptionText.
     */
    @Test
    public void testJobDescriptionIsDisplayed() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.jobDescriptionText);
                assertNotNull("jobDescriptionText must exist in layout", tv);
                assertEquals("Manage the kitchen brigade.", tv.getText().toString());
            });
        }
    }

    /**
     * EXTRA_LOCATION is displayed in jobLocationText with "Location: " prefix.
     */
    @Test
    public void testLocationIsPrefixedAndDisplayed() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.jobLocationText);
                assertNotNull("jobLocationText must exist in layout", tv);
                assertEquals("Location: Toronto, ON", tv.getText().toString());
            });
        }
    }

    /**
     * EXTRA_PAY is displayed in payText with "Pay: " prefix.
     */
    @Test
    public void testPayIsPrefixedAndDisplayed() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.payText);
                assertNotNull("payText must exist in layout", tv);
                assertEquals("Pay: 25.5", tv.getText().toString());
            });
        }
    }

    // ─── Tag chip inflation ───────────────────────────────────────────────────

    /**
     * "Cooking, Leadership, Food Safety" inflates 3 child chips in tagChipGroup.
     */
    @Test
    public void testTagsInflateCorrectNumberOfChips() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                ChipGroup chipGroup = activity.findViewById(R.id.tagChipGroup);
                assertNotNull("tagChipGroup must exist in layout", chipGroup);
                assertEquals("Three comma-separated tags should produce 3 chips",
                        3, chipGroup.getChildCount());
            });
        }
    }

    /**
     * Each chip text matches the trimmed tag value in order.
     */
    @Test
    public void testChipTextsMatchTagValues() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                ChipGroup chipGroup = activity.findViewById(R.id.tagChipGroup);
                String[] expected = {"Cooking", "Leadership", "Food Safety"};
                for (int i = 0; i < expected.length; i++) {
                    Chip chip = (Chip) chipGroup.getChildAt(i);
                    assertEquals("Chip " + i + " text mismatch",
                            expected[i], chip.getText().toString());
                }
            });
        }
    }

    /**
     * A single tag with no commas produces exactly one chip with the correct text.
     */
    @Test
    public void testSingleTagProducesOneChip() {
        Intent intent = fullIntent();
        intent.putExtra(PostDescription.EXTRA_TAGS, "Cooking");

        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ChipGroup chipGroup = activity.findViewById(R.id.tagChipGroup);
                assertEquals("One tag should produce exactly one chip",
                        1, chipGroup.getChildCount());
                assertEquals("Cooking",
                        ((Chip) chipGroup.getChildAt(0)).getText().toString());
            });
        }
    }

    /**
     * When EXTRA_TAGS is absent, tagChipGroup contains zero chips.
     */
    @Test
    public void testAbsentTagsProducesEmptyChipGroup() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_TITLE, "Chef");
        // Intentionally omit EXTRA_TAGS

        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ChipGroup chipGroup = activity.findViewById(R.id.tagChipGroup);
                assertEquals("No chips should appear when EXTRA_TAGS is absent",
                        0, chipGroup.getChildCount());
            });
        }
    }

    // ─── Null / missing extras → placeholder strings ──────────────────────────

    /**
     * When EXTRA_JOB_TITLE is absent, jobTitleText shows the fallback "Job Title".
     */
    @Test
    public void testAbsentTitleFallsBackToPlaceholder() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_DESCRIPTION, "Some description");

        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.jobTitleText);
                assertEquals("Job Title", tv.getText().toString());
            });
        }
    }

    /**
     * When EXTRA_JOB_DESCRIPTION is absent, jobDescriptionText shows the fallback.
     */
    @Test
    public void testAbsentDescriptionFallsBackToPlaceholder() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_TITLE, "Chef");
        // Intentionally omit EXTRA_JOB_DESCRIPTION

        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.jobDescriptionText);
                assertEquals("No description available.", tv.getText().toString());
            });
        }
    }

    /**
     * When EXTRA_LOCATION is absent, jobLocationText shows "Location: N/A".
     */
    @Test
    public void testAbsentLocationFallsBackToPlaceholder() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_TITLE, "Chef");
        // Intentionally omit EXTRA_LOCATION

        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.jobLocationText);
                assertEquals("Location: N/A", tv.getText().toString());
            });
        }
    }

    /**
     * When EXTRA_PAY is absent, payText shows "Pay: N/A".
     */
    @Test
    public void testAbsentPayFallsBackToPlaceholder() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_TITLE, "Chef");
        // Intentionally omit EXTRA_PAY

        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                TextView tv = activity.findViewById(R.id.payText);
                assertEquals("Pay: N/A", tv.getText().toString());
            });
        }
    }

    // ─── Apply button / already-applied indicator visibility ─────────────────

    /**
     * EXTRA_ALREADY_APPLIED = false → applyBtn VISIBLE, alreadyAppliedIndicator GONE.
     */
    @Test
    public void testApplyButtonVisibleWhenNotYetApplied() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                View applyBtn  = activity.findViewById(R.id.applyBtn);
                View indicator = activity.findViewById(R.id.alreadyAppliedIndicator);
                assertEquals("applyBtn should be VISIBLE",
                        View.VISIBLE, applyBtn.getVisibility());
                assertEquals("alreadyAppliedIndicator should be GONE",
                        View.GONE, indicator.getVisibility());
            });
        }
    }

    /**
     * EXTRA_ALREADY_APPLIED = true → applyBtn GONE, alreadyAppliedIndicator VISIBLE.
     */
    @Test
    public void testAlreadyAppliedIndicatorVisibleWhenAlreadyApplied() {
        Intent intent = fullIntent();
        intent.putExtra(PostDescription.EXTRA_ALREADY_APPLIED, true);

        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                View applyBtn  = activity.findViewById(R.id.applyBtn);
                View indicator = activity.findViewById(R.id.alreadyAppliedIndicator);
                assertEquals("applyBtn should be GONE when already applied",
                        View.GONE, applyBtn.getVisibility());
                assertEquals("alreadyAppliedIndicator should be VISIBLE when already applied",
                        View.VISIBLE, indicator.getVisibility());
            });
        }
    }

    // ─── Back button ─────────────────────────────────────────────────────────

    /**
     * Clicking jobDetailBackButton calls finish() — activity is marked as finishing.
     */
    @Test
    public void testBackButtonFinishesActivity() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.jobDetailBackButton).performClick();
                assertTrue("Activity should be finishing after back button click",
                        activity.isFinishing());
            });
        }
    }

    // ─── Apply button → startActivityForResult ────────────────────────────────

    /**
     * Clicking applyBtn fires startActivityForResult with JOB_ID and JOB_TITLE extras.
     * Captured via Robolectric's ShadowActivity.
     */
    @Test
    public void testApplyButtonLaunchesApplyJobActivityWithCorrectExtras() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.applyBtn).performClick();

                Intent started = shadowOf(activity).getNextStartedActivity();
                assertNotNull("applyBtn must trigger startActivityForResult", started);
                assertEquals("JOB_ID extra must be forwarded",
                        "job_abc123", started.getStringExtra("JOB_ID"));
                assertEquals("JOB_TITLE extra must be forwarded",
                        "Head Chef", started.getStringExtra("JOB_TITLE"));
            });
        }
    }

    // ─── onActivityResult ────────────────────────────────────────────────────

    /**
     * RESULT_OK + APPLIED=true → applyBtn GONE, alreadyAppliedIndicator VISIBLE.
     */
    @Test
    public void testOnActivityResult_AppliedTrue_HidesApplyButton() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                Intent resultData = new Intent();
                resultData.putExtra("APPLIED", true);
                activity.onActivityResult(100, Activity.RESULT_OK, resultData);

                assertEquals("applyBtn should be GONE",
                        View.GONE,
                        activity.findViewById(R.id.applyBtn).getVisibility());
                assertEquals("alreadyAppliedIndicator should be VISIBLE",
                        View.VISIBLE,
                        activity.findViewById(R.id.alreadyAppliedIndicator).getVisibility());
            });
        }
    }

    /**
     * RESULT_OK + APPLIED=false → applyBtn stays VISIBLE, indicator stays GONE.
     */
    @Test
    public void testOnActivityResult_AppliedFalse_KeepsApplyButtonVisible() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                Intent resultData = new Intent();
                resultData.putExtra("APPLIED", false);
                activity.onActivityResult(100, Activity.RESULT_OK, resultData);

                assertEquals("applyBtn should remain VISIBLE",
                        View.VISIBLE,
                        activity.findViewById(R.id.applyBtn).getVisibility());
                assertEquals("indicator should remain GONE",
                        View.GONE,
                        activity.findViewById(R.id.alreadyAppliedIndicator).getVisibility());
            });
        }
    }

    /**
     * RESULT_CANCELED with null data → no visibility change (applyBtn stays VISIBLE).
     */
    @Test
    public void testOnActivityResult_ResultCanceled_NoChange() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                activity.onActivityResult(100, Activity.RESULT_CANCELED, null);

                assertEquals("applyBtn should remain VISIBLE on RESULT_CANCELED",
                        View.VISIBLE,
                        activity.findViewById(R.id.applyBtn).getVisibility());
                assertEquals("indicator should remain GONE on RESULT_CANCELED",
                        View.GONE,
                        activity.findViewById(R.id.alreadyAppliedIndicator).getVisibility());
            });
        }
    }

    /**
     * Unrecognised requestCode (≠ 100) → onActivityResult is ignored entirely.
     */
    @Test
    public void testOnActivityResult_WrongRequestCode_Ignored() {
        try (ActivityScenario<PostDescription> scenario =
                     ActivityScenario.launch(fullIntent())) {
            scenario.onActivity(activity -> {
                Intent resultData = new Intent();
                resultData.putExtra("APPLIED", true);
                activity.onActivityResult(999, Activity.RESULT_OK, resultData);

                assertEquals("applyBtn must not change for unrecognised requestCode",
                        View.VISIBLE,
                        activity.findViewById(R.id.applyBtn).getVisibility());
            });
        }
    }
}
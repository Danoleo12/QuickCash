package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.example.development_01.core.data.firebase.PreferenceRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class PostDescription extends AppCompatActivity {

    public static final String EXTRA_JOB_TITLE = "JOB_TITLE";
    public static final String EXTRA_JOB_DESCRIPTION = "JOB_DESCRIPTION";
    public static final String EXTRA_LOCATION = "LOCATION";
    public static final String EXTRA_PAY = "PAY";
    public static final String EXTRA_TAGS = "TAGS";
    public static final String EXTRA_ALREADY_APPLIED = "ALREADY_APPLIED";
    public static final String EXTRA_JOB_ID = "JOB_ID";

    private static final int REQUEST_APPLY = 100;

    private TextView jobTitleText;
    private TextView jobDescriptionText;
    private TextView locationText;
    private TextView payText;
    private ChipGroup tagChipGroup;
    private TextView alreadyAppliedIndicator;

    private Button applyBtn;
    private Button jobDetailBackButton;
    private ImageButton preferJobBtn;

    private String jobId;
    private String jobTitle;

    private PreferenceRepository preferenceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        jobTitleText = findViewById(R.id.jobTitleText);
        jobDescriptionText = findViewById(R.id.jobDescriptionText);
        locationText = findViewById(R.id.jobLocationText);
        payText = findViewById(R.id.payText);
        tagChipGroup = findViewById(R.id.tagChipGroup);
        alreadyAppliedIndicator = findViewById(R.id.alreadyAppliedIndicator);

        applyBtn = findViewById(R.id.applyBtn);
        jobDetailBackButton = findViewById(R.id.jobDetailBackButton);
        preferJobBtn = findViewById(R.id.preferJobBtn);

        preferenceRepository = new PreferenceRepository(
                FirebaseDatabase.getInstance().getReference("users"));

        jobTitle = getIntent().getStringExtra(EXTRA_JOB_TITLE);
        String jobDescription = getIntent().getStringExtra(EXTRA_JOB_DESCRIPTION);
        String location = getIntent().getStringExtra(EXTRA_LOCATION);
        String pay = getIntent().getStringExtra(EXTRA_PAY);
        String tags = getIntent().getStringExtra(EXTRA_TAGS);
        jobId = getIntent().getStringExtra(EXTRA_JOB_ID);
        boolean alreadyApplied = getIntent().getBooleanExtra(EXTRA_ALREADY_APPLIED, false);

        jobTitleText.setText(jobTitle != null ? jobTitle : "Job Title");
        jobDescriptionText.setText(jobDescription != null ? jobDescription : "No description available.");
        locationText.setText(location != null ? "Location: " + location : "Location: N/A");
        payText.setText(pay != null ? "Pay: " + pay : "Pay: N/A");

        tagChipGroup.removeAllViews();

        if (tags != null && !tags.isEmpty()) {

            String[] tagList = tags.split(",");

            for (String tag : tagList) {
                Chip chip = new Chip(this);
                chip.setText(tag.trim());
                chip.setClickable(false);
                chip.setCheckable(false);
                tagChipGroup.addView(chip);
            }
        }

        updateApplyButton(alreadyApplied);
        setupPreferButton();

        applyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostDescription.this, ApplyJobActivity.class);
            intent.putExtra("JOB_ID", jobId);
            intent.putExtra("JOB_TITLE", jobTitle);
            startActivityForResult(intent, REQUEST_APPLY);
        });

        jobDetailBackButton.setOnClickListener(v -> finish());
    }

    private void setupPreferButton() {
        // Default to not preferred until Firebase responds
        preferJobBtn.setTag(Boolean.FALSE);
        preferJobBtn.setImageResource(android.R.drawable.btn_star_big_off);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && jobTitle != null) {
            preferenceRepository.isPreferred(currentUser.getUid(), jobTitle, isPreferred -> {
                preferJobBtn.setTag(isPreferred);
                preferJobBtn.setSelected(isPreferred);
                preferJobBtn.setImageResource(isPreferred
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off);
            });
        }

        preferJobBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            boolean currentlyPreferred = Boolean.TRUE.equals(preferJobBtn.getTag());

            if (currentlyPreferred) {
                preferJobBtn.setTag(Boolean.FALSE);
                preferJobBtn.setSelected(false);
                preferJobBtn.setImageResource(android.R.drawable.btn_star_big_off);
                if (user != null && jobTitle != null) {
                    preferenceRepository.removePreference(user.getUid(), jobTitle, null);
                }
            } else {
                preferJobBtn.setTag(Boolean.TRUE);
                preferJobBtn.setSelected(true);
                preferJobBtn.setImageResource(android.R.drawable.btn_star_big_on);
                if (user != null && jobTitle != null) {
                    preferenceRepository.addPreference(user.getUid(), jobTitle, null);
                }
            }
        });
    }

    private void updateApplyButton(boolean alreadyApplied) {
        if (alreadyApplied) {
            alreadyAppliedIndicator.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.GONE);
        } else {
            alreadyAppliedIndicator.setVisibility(View.GONE);
            applyBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_APPLY && resultCode == RESULT_OK && data != null) {
            boolean applied = data.getBooleanExtra("APPLIED", false);
            updateApplyButton(applied);
        }
    }
}
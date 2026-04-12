package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.development_01.R;
import com.example.development_01.core.core.DBSearch;
import com.example.development_01.core.core.Job;
import com.example.development_01.core.core.JobAdapter;
import com.example.development_01.core.data.firebase.PreferenceRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PreferredJobsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View layoutEmptyState;
    private ProgressBar progressBar;
    private JobAdapter jobAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_jobs);

        recyclerView     = findViewById(R.id.recyclerViewPreferredJobs);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        progressBar      = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobAdapter = new JobAdapter();
        recyclerView.setAdapter(jobAdapter);

        jobAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(this, PostDescription.class);
            intent.putExtra(PostDescription.EXTRA_JOB_TITLE,       job.getTitle());
            intent.putExtra(PostDescription.EXTRA_JOB_DESCRIPTION, job.getDescription());
            intent.putExtra(PostDescription.EXTRA_LOCATION,        job.getLocation());
            intent.putExtra(PostDescription.EXTRA_PAY,             job.getPay() != null ? String.valueOf(job.getPay()) : "");
            intent.putExtra(PostDescription.EXTRA_JOB_ID,          job.getId());
            startActivity(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadPreferredJobs();
    }

    private void loadPreferredJobs() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        showLoading();

        String uid = currentUser.getUid();
        PreferenceRepository preferenceRepository = new PreferenceRepository(
                FirebaseDatabase.getInstance().getReference("users"));

        preferenceRepository.fetchPreferences(uid, titles -> {
            if (titles.isEmpty()) {
                showEmptyState();
                return;
            }

            DBSearch dbSearch = new DBSearch();
            dbSearch.searchByTitles(titles, new DBSearch.JobSearchCallback() {
                @Override
                public void onCallback(List<Job> jobList) {
                    if (jobList.isEmpty()) {
                        showEmptyState();
                    } else {
                        showJobs(jobList);
                    }
                }

                @Override
                public void onError(Exception e) {
                    showEmptyState();
                }
            });
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showJobs(List<Job> jobs) {
        progressBar.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        jobAdapter.setJobList(jobs);
    }
}

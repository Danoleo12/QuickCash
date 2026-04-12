package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.development_01.R;
import com.example.development_01.core.core.Job;
import com.example.development_01.core.core.DBSearch;
import com.example.development_01.core.core.JobAdapter;

import java.util.List;

public class PostSearchActivity extends AppCompatActivity {

    private TextView tvSearchQueryContext, tvEmptyStateTitle;
    private RecyclerView recyclerViewJobs;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private Button btnLoadMore, btnClearSearch;

    private JobAdapter jobAdapter;
    private DBSearch dbSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);

        tvSearchQueryContext = findViewById(R.id.tvSearchQueryContext);
        recyclerViewJobs = findViewById(R.id.recyclerViewJobs);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyStateTitle = findViewById(R.id.tvEmptyStateTitle);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnLoadMore = findViewById(R.id.btnLoadMore);

        dbSearch = new DBSearch();
        jobAdapter = new JobAdapter();

        jobAdapter.setOnJobClickListener(job -> {
            Intent intent = new Intent(PostSearchActivity.this, PostDescription.class);

            intent.putExtra(PostDescription.EXTRA_JOB_ID, job.getId());
            intent.putExtra(PostDescription.EXTRA_JOB_TITLE, job.getTitle());
            intent.putExtra(PostDescription.EXTRA_JOB_DESCRIPTION, job.getDescription());
            intent.putExtra(PostDescription.EXTRA_LOCATION, job.getLocation());
            intent.putExtra(PostDescription.EXTRA_PAY, String.valueOf(job.getPay()));

            String tagsText = "";
            if (job.getTags() != null && !job.getTags().isEmpty()) {
                tagsText = android.text.TextUtils.join(", ", job.getTags());
            }
            intent.putExtra(PostDescription.EXTRA_TAGS, tagsText);

            startActivity(intent);
        });

        recyclerViewJobs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewJobs.setAdapter(jobAdapter);

        String searchQuery = getIntent().getStringExtra("SEARCH_QUERY");

        if (searchQuery != null && !searchQuery.isEmpty()) {
            tvSearchQueryContext.setText("Results for '" + searchQuery + "'");
            performSearch(searchQuery);
        } else {
            tvSearchQueryContext.setText("Recent Jobs");
            performSearch("");
        }

        btnClearSearch.setOnClickListener(v -> finish());
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewJobs.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        btnLoadMore.setVisibility(View.GONE);

        dbSearch.searchByTitle(query, new DBSearch.JobSearchCallback() {
            @Override
            public void onCallback(List<Job> jobList) {
                progressBar.setVisibility(View.GONE);

                if (jobList.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyStateTitle.setText("No jobs found for '" + query + "'");
                } else {
                    jobAdapter.setJobList(jobList);
                    recyclerViewJobs.setVisibility(View.VISIBLE);

                    if (jobList.size() >= 10) {
                        btnLoadMore.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PostSearchActivity.this, "Error connecting to database.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
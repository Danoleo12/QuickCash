package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.development_01.R;
import com.example.development_01.core.core.Job;
import com.example.development_01.core.core.JobAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmployerJobViewActivity extends AppCompatActivity {

    protected RecyclerView rvJobs;
    protected JobAdapter adapter;
    protected TextView tvNoJobs;
    protected String employerEmail;
    protected FirebaseFirestore db;
    protected ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_job_list);

        db = FirebaseFirestore.getInstance();
        employerEmail = getIntent().getStringExtra("email");

        rvJobs = findViewById(R.id.rvEmployerJobs);
        tvNoJobs = findViewById(R.id.tvNoJobs);
        btnBack = findViewById(R.id.btnBack);

        initRecycler();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadJobs();
    }

    private void initRecycler() {
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter();
        rvJobs.setAdapter(adapter);

        adapter.setOnJobClickListener(new JobAdapter.OnJobClickListener() {
            @Override
            public void onJobClick(Job job) {
                Intent intent = new Intent(EmployerJobViewActivity.this, JobApplicationsActivity.class);
                intent.putExtra("JOB_ID", job.getId());
                intent.putExtra("JOB_TITLE", job.getTitle());
                intent.putExtra("JOB_DESCRIPTION", job.getDescription());
                intent.putExtra("JOB_PAY", job.getPay());
                intent.putExtra("JOB_LOCATION", job.getLocation());
                intent.putStringArrayListExtra("JOB_TAGS", job.getTags());
                startActivity(intent);
            }
        });
    }

    private void loadJobs() {
        if (employerEmail == null) return;

        // get jobs for this employer
        db.collection("jobs")
                .whereEqualTo("employerEmail", employerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> jobList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        jobList.add(job);
                    }

                    if (jobList.isEmpty()) {
                        tvNoJobs.setVisibility(View.VISIBLE);
                        rvJobs.setVisibility(View.GONE);
                    } else {
                        tvNoJobs.setVisibility(View.GONE);
                        rvJobs.setVisibility(View.VISIBLE);
                        adapter.setJobList(jobList);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
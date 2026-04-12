package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.development_01.R;
import com.example.development_01.core.data.Employee;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class JobApplicationsActivity extends AppCompatActivity {

    protected TextView tvTitle, tvDescription, tvPay, tvLocation, tvNoApplications, tvHeaderTitle;
    protected ChipGroup tagGroup;
    protected RecyclerView rvApplicants;
    protected ApplicationAdapter adapter;
    protected FirebaseFirestore db;
    protected String jobId;
    protected ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_applications);

        db = FirebaseFirestore.getInstance();
        jobId = getIntent().getStringExtra("JOB_ID");

        this.initViews();
        this.setJobInfo();
        this.initRecycler();
        this.fetchApplicants();
    }

    protected void initViews() {
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvPay = findViewById(R.id.tvDetailPay);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvNoApplications = findViewById(R.id.tvNoApplications);
        tvHeaderTitle = findViewById(R.id.tvHeaderJobTitle);
        tagGroup = findViewById(R.id.detailTagGroup);
        rvApplicants = findViewById(R.id.rvApplicants);
        btnBack = findViewById(R.id.btnBackApplications);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * set the job details from the intent
     */
    protected void setJobInfo() {
        String title = getIntent().getStringExtra("JOB_TITLE");
        tvTitle.setText(title);
        tvHeaderTitle.setText(title);

        tvDescription.setText(getIntent().getStringExtra("JOB_DESCRIPTION"));
        
        double pay = getIntent().getDoubleExtra("JOB_PAY", 0.0);
        tvPay.setText(getString(R.string.pay_format, pay));
        
        String location = getIntent().getStringExtra("JOB_LOCATION");
        tvLocation.setText(getString(R.string.location_format, location));

        ArrayList<String> tags = getIntent().getStringArrayListExtra("JOB_TAGS");
        if (tags != null) {
            for (String tag : tags) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                tagGroup.addView(chip);
            }
        }
    }

    protected void initRecycler() {
        rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter();
        rvApplicants.setAdapter(adapter);
    }

    /**
     * get applicants from firebase and sort by time applied
     */
    protected void fetchApplicants() {
        if (jobId == null) return;

        db.collection("applications")
                .whereEqualTo("postId", jobId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ApplicationItem> items = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String applicantName = doc.getString("applicantName");
                        String applicantEmail = doc.getString("applicantEmail");
                        String resumeUrl = doc.getString("resume");
                        Timestamp timeApplied = doc.getTimestamp("timeApplied");
                        String jobStatus = doc.getString("Job Status");
                        items.add(new ApplicationItem(doc.getId(), applicantName, applicantEmail, resumeUrl, timeApplied, jobStatus));
                    }

                    Collections.sort(items, (a, b) -> {
                        if (a.getTimeApplied() == null || b.getTimeApplied() == null) return 0;
                        return a.getTimeApplied().compareTo(b.getTimeApplied());
                    });

                    if (items.isEmpty()) {
                        tvNoApplications.setVisibility(View.VISIBLE);
                        rvApplicants.setVisibility(View.GONE);
                    } else {
                        tvNoApplications.setVisibility(View.GONE);
                        rvApplicants.setVisibility(View.VISIBLE);
                        adapter.setApplications(items);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    protected static class ApplicationItem {
        private String id;
        private String applicantName;
        private String applicantEmail;
        private String resumeUrl;
        private Timestamp timeApplied;
        private String jobStatus;

        public ApplicationItem(String id, String applicantName, String applicantEmail, String resumeUrl, Timestamp timeApplied, String jobStatus) {
            this.id = id;
            this.applicantName = applicantName;
            this.applicantEmail = applicantEmail;
            this.resumeUrl = resumeUrl;
            this.timeApplied = timeApplied;
            this.jobStatus = jobStatus;
        }

        public String getId() { return id; }
        public String getApplicantName() { return applicantName; }
        public String getApplicantEmail() { return applicantEmail; }
        public String getResumeUrl() { return resumeUrl; }
        public Timestamp getTimeApplied() { return timeApplied; }
        public String getJobStatus() { return jobStatus; }
        public void setJobStatus(String jobStatus) { this.jobStatus = jobStatus; }
    }

    protected class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {
        private List<ApplicationItem> applications = new ArrayList<>();

        public void setApplications(List<ApplicationItem> applications) {
            this.applications = applications;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ApplicationItem item = applications.get(position);
            holder.tvName.setText(item.getApplicantName());
            
            if (item.getTimeApplied() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String dateStr = sdf.format(item.getTimeApplied().toDate());
                holder.tvTime.setText(getString(R.string.applied_format, dateStr));
            }

            holder.btnViewResume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(JobApplicationsActivity.this, ResumeViewerActivity.class);
                    intent.putExtra("RESUME_URL", item.getResumeUrl());
                    intent.putExtra("APPLICANT_NAME", item.getApplicantName());
                    startActivity(intent);
                }
            });

            boolean isHired = "Hired".equals(item.getJobStatus());
            holder.btnHireApplicant.setEnabled(!isHired);
            holder.btnHireApplicant.setAlpha(isHired ? 0.4f : 1.0f);

            holder.btnHireApplicant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("applications")
                            .document(item.getId())
                            .update("Job Status", "Hired")
                            .addOnSuccessListener(aVoid -> {
                                item.setJobStatus("Hired");
                                holder.btnHireApplicant.setEnabled(false);
                                holder.btnHireApplicant.setAlpha(0.4f);
                                Toast.makeText(JobApplicationsActivity.this, "Employee hired successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(JobApplicationsActivity.this, EmployerPayPal.class);
                                intent.putExtra("JOB_ID", item.getId());
                                intent.putExtra("JOB_TITLE", tvTitle.getText().toString());
                                intent.putExtra("LOCATION", tvLocation.getText().toString());
                                intent.putExtra("APPLICANT_NAME", item.getApplicantName());
                                intent.putExtra("APPLICANT_EMAIL", item.getApplicantEmail());
                                intent.putExtra("JOB_PAY", getIntent().getDoubleExtra("JOB_PAY", 0.0));
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(JobApplicationsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            });

            holder.btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String sanitizedEmail = item.getApplicantEmail().replace(".", ",");
                    Employee emp = new Employee(item.getApplicantName(), item.getApplicantEmail(), "Employee");
                    FirebaseDatabase.getInstance().getReference("favorites")
                            .child(uid).child(sanitizedEmail).setValue(emp)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(JobApplicationsActivity.this, item.getApplicantName() + " added to favorites", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(JobApplicationsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        }

        @Override
        public int getItemCount() { return applications.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvTime;
            Button btnViewResume, btnHireApplicant, btnLike;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvApplicantName);
                tvTime = itemView.findViewById(R.id.tvTimeApplied);
                btnViewResume = itemView.findViewById(R.id.btnViewResume);
                btnHireApplicant = itemView.findViewById(R.id.btnHireApplicant);
                btnLike = itemView.findViewById(R.id.likeButton);
            }
        }
    }
}

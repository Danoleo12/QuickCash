package com.example.development_01.core.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ApplyJobActivity extends AppCompatActivity {

    private EditText applicantName;
    private EditText emailInput;
    private EditText phoneInput;
    private Button uploadResumeBtn;
    private TextView uploadStatus;
    private Button submitApplicationBtn;
    private Button nextBtn;

    private String jobId;
    private String jobTitle;
    private boolean isResumeUploaded = false;
    private String resumeUrl = "";   // store resume download URL

    private View pageUserInfo;
    private View pageResume;
    private TextView step1;
    private TextView step2;

    private FirebaseFirestore db;
    private DatabaseReference realtimeDb;
    private FirebaseStorage storage;

    public static boolean testMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_job);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference("users");

        jobId = getIntent().getStringExtra("JOB_ID");
        jobTitle = getIntent().getStringExtra("JOB_TITLE");

        applicantName = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        uploadResumeBtn = findViewById(R.id.uploadResumeBtn);
        uploadStatus = findViewById(R.id.uploadStatus);
        submitApplicationBtn = findViewById(R.id.submitApplicationBtn);
        nextBtn = findViewById(R.id.nextBtn);

        pageUserInfo = findViewById(R.id.pageUserInfo);
        pageResume = findViewById(R.id.pageResume);
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);

        nextBtn.setOnClickListener(v -> {
            String name = applicantName.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            pageUserInfo.setVisibility(View.GONE);
            pageResume.setVisibility(View.VISIBLE);

            step1.setBackgroundResource(R.drawable.step_circle_inactive);
            step2.setBackgroundResource(R.drawable.step_circle_active);
        });

        uploadResumeBtn.setOnClickListener(v -> {

            if (testMode) {
                uploadStatus.setText("Resume Uploaded Successfully!");
                isResumeUploaded = true;
                return;
            }

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(Intent.createChooser(intent, "Select Resume"), 100);

        });

        submitApplicationBtn.setOnClickListener(v -> {

            if (!isResumeUploaded) {
                Toast.makeText(this, "Please upload your resume", Toast.LENGTH_SHORT).show();
                return;
            }

            saveApplication();

        });
    }

    private void uploadResumeToFirebase(Uri fileUri) {

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        // Create a StorageReference for user Resume
        StorageReference resumeRef = storage.getReference()
                .child("resumes")
                .child(jobId)
                .child(userId + ".pdf");

        resumeRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        resumeRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            resumeUrl = uri.toString();
                            isResumeUploaded = true;

                            uploadStatus.setText("Resume Uploaded Successfully!");

                            Toast.makeText(this, "File successfully uploaded.", Toast.LENGTH_SHORT).show();

                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveApplication() {

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        // Create a DocumentReference for the user
        DocumentReference userRef = db.collection("users").document(userId);

        // 1. Create Application in 'applications' collection
        Map<String, Object> application = new HashMap<>();
        application.put("applicantName", applicantName.getText().toString().trim());
        application.put("applicantEmail", emailInput.getText().toString().trim());
        application.put("applicantPhone", phoneInput.getText().toString().trim());
        application.put("jobTitle", jobTitle != null ? jobTitle : "Unknown Job");
        application.put("postId", jobId);
        application.put("user", userRef); // Storing as a DocumentReference
        application.put("applicantName", applicantName.getText().toString().trim());
        application.put("applicantEmail", emailInput.getText().toString().trim());
        application.put("applicantPhone", phoneInput.getText().toString().trim());
        application.put("timeApplied", FieldValue.serverTimestamp());
        application.put("resume", resumeUrl); // store URL instead of StorageReference
        application.put("Job Status", "Pending");

        db.collection("applications").add(application)
                .addOnSuccessListener(documentReference -> {

                    // 2. Update 'jobs' collection to add applicant ID to array
                    updateJobApplicants(userId);

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    private void updateJobApplicants(String userId) {

        if (jobId == null) return;

        db.collection("jobs").document(jobId)
                .update("applicants", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_LONG).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("APPLIED", true);
                    setResult(RESULT_OK, resultIntent);

                    finish();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this, "Application saved, but failed to update job record.", Toast.LENGTH_SHORT).show();
                    finish();

                });

        realtimeDb.child("appliedJobs").push().setValue(jobId)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Application saved, but failed to update job record.", Toast.LENGTH_SHORT).show());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            Uri fileUri = data.getData();

            uploadResumeToFirebase(fileUri);

        }
    }
}

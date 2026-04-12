package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.example.development_01.core.core.Job;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;


public class JobDescription extends AppCompatActivity implements View.OnClickListener {


    private EditText titleInput;
    private EditText payInput;
    private EditText descriptionInput;
    private EditText locationInput;
    private EditText tagsInputs;
    private EditText companyTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_description);

        setupPublishButton();
    }

    /**
     * Retrieves the job criteria from the current input then initializes a job object
     * Get user's input for:
     *      the job title as a string
     *      the pay as an integer
     *      the description as String
     *      and the tags as a Sting
     * Then creates a Job object.
     */
    public Job initializeJob(){
        titleInput = findViewById(R.id.jobTitle);
        String title = titleInput.getText().toString();

        companyTitle = findViewById(R.id.companyTitle);
        String companyName = companyTitle.getText().toString();

        payInput = findViewById(R.id.jobPay);
        String payValue = payInput.getText().toString();
        if(payValue.trim().equals("")){
            payValue="0.00";
        }
        double pay = Double.parseDouble(payValue);

        descriptionInput = findViewById(R.id.jobDescription);
        String description = descriptionInput.getText().toString();

        locationInput = findViewById(R.id.jobLocation);
        String location = locationInput.getText().toString();

        tagsInputs = findViewById(R.id.jobTags);
        String tag = tagsInputs.getText().toString();

        Boolean validForm = isFormValid(title, companyName, payValue, location, tag, description);


        if(validForm){
            String[] tags = tag.split("\\s*,\\s*");
            ArrayList<String> tagList = new ArrayList<>(Arrays.asList(tags));
            // Employer info
            String employerEmail = getIntent().getStringExtra("email");
            Job job = new Job(title, companyName, description, location, pay, tagList, employerEmail);
            System.out.println("Job Object Created");
            return job;
        }else{
            System.out.println("Job Object Not Created");
            return null;
        }
    }

    /**
     * Validates all required form fields before submission.
     * This method checks that the title, pay amount, location, tags,
     * and description fields are not empty (or zero for pay).
     * If any field is invalid, it sets an appropriate error message
     * on the corresponding input view and returns false.
     * Returns true only when all fields contain valid values.
     *
     * @param title        the job title entered by the user
     * @param companyName  the company name entered by the user
     * @param payValue          the pay amount; must be greater than zero
     * @param location     the job location entered by the user
     * @param tags         the tags or categories associated with the job
     * @param description  the job description text
     * @return true if all fields are valid; false otherwise
     */

    public boolean isFormValid(String title, String companyName, String payValue, String location, String tags, String description){

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is mandatory", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (companyName.isEmpty()) {
            Toast.makeText(this, "Company Name is mandatory", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isPayValid(payValue)) {
            Toast.makeText(this, "Pay is mandatory and can't be Null", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "Location is mandatory", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (tags.isEmpty()) {
            Toast.makeText(this, "Tags are mandatory", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Description is mandatory", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public boolean isPayValid(String payValue){
        if(payValue.equals("") || payValue.equals("0") || payValue.equals("0.0") || payValue.equals("0.00")){
            return false;
        }
        return true;
    }

    private void setupPublishButton() {
        Button publishBtn = findViewById(R.id.publishBtn);
        publishBtn.setOnClickListener(this);
    }
    private void findEmployerNode(
            String employerEmail,
            String jobId,
            Runnable onNotFound
    ) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("role").equalTo("Employer")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.d("findEmployerNode", "Employer snapshot count: " + snapshot.getChildrenCount());

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String email = userSnapshot.child("email").getValue(String.class);
                            Log.d("findEmployerNode", "Checking email: " + email + " against: " + employerEmail);

                            if (employerEmail.equals(email)) {
                                Log.d("findEmployerNode", "Match found, updating jobsPosted...");
                                appendJobToEmployer(userSnapshot.getRef(), jobId, employerEmail);
                                return;
                            }
                        }
                        onNotFound.run();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("findEmployerNode", "Query cancelled: " + error.getMessage());
                    }
                });
    }

    /**
     * Appends a jobId to the employer's "jobsPosted" list.
     * push() generates a unique key to avoid array index conflicts.
     */
    private void appendJobToEmployer(DatabaseReference employerRef, String jobId, String employerEmail) {
        employerRef.child("jobsPosted")
                .push()
                .setValue(jobId)
                .addOnSuccessListener(aVoid ->
                        Log.d("findEmployerNode", "jobsPosted updated successfully for: " + employerEmail))
                .addOnFailureListener(e ->
                        Log.e("findEmployerNode", "Failed to update jobsPosted: " + e.getMessage()));
    }

    @Override
    public void onClick(View v) {
        Job job = initializeJob();
        if(job == null){
            Toast.makeText(this, "Failed to validate form: ", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore firebaseManager = FirebaseFirestore.getInstance();

        // Also add the employer name in the future
        firebaseManager.collection("jobs")
                .add(job)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                    String jobId = documentReference.getId();
                    documentReference.update("id", jobId);

                    findEmployerNode(
                            job.getEmployerEmail(),
                            jobId,                    // ← pass jobId directly, NOT job.getId()
                            () -> Log.w("JobDescription", "No matching employer found for: " + job.getEmployerEmail())
                    );

                    Toast.makeText(this, "Job Posted Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(this, "Failed to post job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
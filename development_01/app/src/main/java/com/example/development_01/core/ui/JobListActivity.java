package com.example.development_01.core.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.development_01.R;

public class JobListActivity extends AppCompatActivity {

    Button exitJobsButton;
    LinearLayout jobsContainer;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_list);

        exitJobsButton = findViewById(R.id.exitJobsButton);
        jobsContainer = findViewById(R.id.jobsContainer);

        exitJobsButton.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        displayJobs();
    }

    private void displayJobs() {

        db.collection("jobs").get().addOnSuccessListener(queryDocumentSnapshots -> {

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                String title = doc.getString("title");
                String description = doc.getString("description");
                String location = doc.getString("location");
                Long pay = doc.getLong("pay");

                Button jobButton = new Button(this);
                jobButton.setText(title);

                jobButton.setOnClickListener(v ->
                        jobPopups(title, description, location, pay)
                );

                jobsContainer.addView(jobButton);
            }

        });
    }

    private void jobPopups(String title, String description, String location, Long pay) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(
                        "Description: " + description + "\n\n" +
                                "Location: " + location + "\n\n" +
                                "Pay: $" + pay
                )
                .setPositiveButton("Exit", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
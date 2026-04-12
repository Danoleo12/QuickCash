package com.example.development_01.core.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.example.development_01.core.data.Employee;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etComments;
    private Button btnSubmitRating;
    private String employeeEmail;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        employeeEmail = getIntent().getStringExtra("APPLICANT_EMAIL");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        ratingBar = findViewById(R.id.ratingBar);
        etComments = findViewById(R.id.etComments);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);

        btnSubmitRating.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        float ratingValue = ratingBar.getRating();
        if (ratingValue == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (employeeEmail == null || employeeEmail.isEmpty()) {
            Toast.makeText(this, "Employee email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search for the user with the given email
        usersRef.orderByChild("email").equalTo(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        Employee employee = userSnapshot.getValue(Employee.class);
                        if (employee != null) {
                            employee.addRating(ratingValue);
                            userSnapshot.getRef().setValue(employee)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RatingActivity.this, "Rating submitted successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RatingActivity.this, "Failed to submit rating", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    Toast.makeText(RatingActivity.this, "Employee not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RatingActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
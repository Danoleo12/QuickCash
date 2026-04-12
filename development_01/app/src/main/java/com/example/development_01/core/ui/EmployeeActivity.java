package com.example.development_01.core.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.development_01.R;

public class EmployeeActivity extends AppCompatActivity implements View.OnClickListener{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        this.showEmail();
        this.showName();
        this.showRating();
        this.setupLogOutButton();
        this.setupSearchButton();
        this.setupPreferredJobsButton();
        this.setupViewMapButton();
        this.setupPreferredJobsButton();
        this.setupPaymentSettingsButton();
        this.setupEarningsButton();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        ContextCompat.startForegroundService(this, new Intent(this, JobAlertService.class));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });

    }

    protected void setupLogOutButton() {
        Button logOutButton = findViewById(R.id.employeeLogOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Clear the session preferences (sharedPreferences, Firebase, etc)
                clearUserSession();

                stopService(new Intent(EmployeeActivity.this, JobAlertService.class));

                //2. Create intent to go back to login page
                Intent intent = new Intent(EmployeeActivity.this, LoginActivity.class);

                //3. Clear the entire history session aka Clears the back button stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //4. Redirect the user to the logIn page
                startActivity(intent);

                //5. End the current activity
                finish();
            }
        });
    }

    protected void setupSearchButton() {
        Button searchButton = findViewById(R.id.btnSearchJobs);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void setupPreferredJobsButton() {
        Button preferredJobsButton = findViewById(R.id.btnPreferredJobs);
        preferredJobsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeActivity.this, PreferredJobsActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void setupViewMapButton() {
        Button viewMapButton = findViewById(R.id.btnViewMap);
        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void setupPaymentSettingsButton() {
        Button paymentButton = findViewById(R.id.btnPaymentSettings);
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeActivity.this, PaymentSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void setupEarningsButton() {
        Button earningsButton = findViewById(R.id.btnEarnings);
        earningsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeActivity.this, EarningsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {}

    protected void showEmail(){
        TextView email = findViewById(R.id.employeeEmail);
        String dbEmail = getIntent().getStringExtra("email");
        email.setText(dbEmail);
    }

    protected void showName(){
        TextView name = findViewById(R.id.employeeName);
        String dbName = getIntent().getStringExtra("name");
        name.setText(dbName);
    }

    protected void showRating() {
        TextView ratingTv = findViewById(R.id.employeeRating);
        double rating = getIntent().getDoubleExtra("rating", 0.0);
        ratingTv.setText(String.format("%.1f / 5.0", rating));
    }

    //This should clear the Firebase session
    protected void clearUserSession(){
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); //Removes all saved Data
        editor.apply();
    }
}
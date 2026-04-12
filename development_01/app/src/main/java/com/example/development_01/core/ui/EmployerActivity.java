package com.example.development_01.core.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;

public class EmployerActivity extends AppCompatActivity implements View.OnClickListener{
    String employerEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer);
        employerEmail = getIntent().getStringExtra("email");
        this.showEmail();
        this.showName();

        this.setupLogOutButton();
        this.setupPostJobButton();
        this.setupViewApplicationsButton();
        this.setupPayPalButton();
        this.setUpViewFavoritesButton();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }

    @Override
    public void onClick(View v) {}

    /**
     * Set up the logOut button and redirect the user to the login activity
     * Clears all user related in the current session
     * Start a new activity and prevents the user from returning to this activity by
     * pressing the back button then ends the session.
     */
    protected void setupLogOutButton() {
        Button logOutButton = findViewById(R.id.employerLogOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Clear the session preferences (sharedPreferences, Firebase, etc)
                clearUserSession();

                //2. Create intent to go back to login page
                Intent intent = new Intent(EmployerActivity.this, LoginActivity.class);

                //3. Clear the entire history session
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //4. Start the new Activity
                startActivity(intent);

                //5. End the current activity
                finish();
            }
        });
    }


    /**
     * Set up the PayPal button to navigate to the hired employees list.
     */
    protected void setupPayPalButton() {
        Button payPalButton = findViewById(R.id.btnPayPal);
        payPalButton.setOnClickListener(v -> {
            Intent intent = new Intent(EmployerActivity.this, HiredEmployeesActivity.class);
            intent.putExtra("email", employerEmail);
            startActivity(intent);
        });
    }

    /**
     * Set up the button to view favorite employees and redirect the employer to the favorite employee activity
     * Start a new activity
     */
    protected void setUpViewFavoritesButton() {
        Button postJob = findViewById(R.id.viewFavorites);
        postJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployerActivity.this, Favorite_Employee.class);
                startActivity(intent);
            }
        });
    }



    /**
     * Set up the button to add job and redirect the employer to the job description activity
     * Start a new activity
     */
    protected void setupPostJobButton() {
        Button postJob = findViewById(R.id.addJob);
        postJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. Create intent to go back to login page
                Intent intent = new Intent(EmployerActivity.this, JobDescription.class);
                intent.putExtra("email", employerEmail);
                //2. Start the new Activity
                startActivity(intent);
            }
        });
    }

    /**
     * Set up the button to view applications and redirect the employer to the job list activity
     */
    protected void setupViewApplicationsButton() {
        Button viewApps = findViewById(R.id.seeAppls);
        viewApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployerActivity.this, EmployerJobViewActivity.class);
                intent.putExtra("email", employerEmail);
                startActivity(intent);
            }
        });
    }

    /**
     * Retrieves the employee's email address from the launching Intent
     * and displays it in the employeeEmail TextView within this Activity.
     * This updates the UI with the email value passed from the previous screen.
     */
    protected void showEmail(){
        TextView email = findViewById(R.id.employerEmail);
        String dbEmail = getIntent().getStringExtra("email");
        email.setText(dbEmail);
    }

    /**
     * Retrieves the employee's name from the launching Intent
     * and displays it in the employeeName TextView within this Activity.
     * This updates the UI with the email value passed from the previous screen.
     */
    protected void showName(){
        TextView name = findViewById(R.id.employerName);
        String dbName = getIntent().getStringExtra("name");
        name.setText(dbName);
    }

    /**
     * Clears all user-related data stored in the UserPrefs SharedPreferences.
     * This removes the entire saved session, including any authentication
     * or profile information, and commits the change asynchronously.
     * This prevents the user from staying loggedIn when they click
     */
    protected void clearUserSession(){
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); //Removes all saved Data
        editor.apply();
    }
}
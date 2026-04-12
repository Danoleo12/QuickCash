package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.example.development_01.core.data.firebase.FirebaseCRUD;
import com.example.development_01.core.validation.EmailValidator;
import com.example.development_01.core.validation.PasswordValidator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, resetButton, signupButton;
    private ImageButton backButton;
    private TextView statusTextView;
    private FirebaseCRUD firebase;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI (null checks defensive)
        emailEditText = findViewById(R.id.emailAddress);
        passwordEditText = findViewById(R.id.userPassword);
        loginButton = findViewById(R.id.loginBtn);
        statusTextView = findViewById(R.id.statusTextView);
        resetButton = findViewById(R.id.forgotPWBtn);
        backButton = findViewById(R.id.backBtn);
        signupButton = findViewById(R.id.signupBtn);

        firebase = new FirebaseCRUD();
        auth = FirebaseAuth.getInstance();

        Log.d("LoginActivity", "onCreate finished");

        // AC4: Session persistence
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            showSuccessMessageWithUsername(current.getUid(), current.getEmail());
        } else if (statusTextView != null) {
            statusTextView.setVisibility(View.INVISIBLE);
        }

        loginButton.setOnClickListener(this::handleLoginClick);
        resetButton.setOnClickListener(this::handleResetClick);
        
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (signupButton != null) {
            signupButton.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            });
        }
    }

    private void handleResetClick(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    protected void handleLoginClick(View view) {
        String email = getEmail().trim();
        String password = getPassword().trim();

        // AC3: Empty fields validation
        if (EmailValidator.isEmpty(email) || PasswordValidator.isEmpty(password)) {
            showErrorSnackbar(view, "Enter detail");
            return;
        }

        // AC2: Perform Firebase authentication
        performFirebaseLogin(email, password, view);
    }

    protected void performFirebaseLogin(String email, String password, View view) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    showSuccessMessageWithUsername(user.getUid(), user.getEmail());
                    String uid = user.getUid();
                    firebase.fetchUserProfile(uid, new FirebaseCRUD.UserProfileCallback() {
                        @Override
                        public void onSuccess(String name, String role, Double rating) {
                            moveToDashboard(name, role, email, rating, view);
                        }
                        @Override
                        public void onError(String error) {
                            showErrorSnackbar(view, "Failed to load profile: " + error);
                        }
                    });
                } else {
                    showSuccessMessageFallback(email);
                }
            } else {
                // AC2: Invalid credentials
                showErrorSnackbar(view, "Invalid Credential");
            }
        });
    }


    private void moveToDashboard(String name, String role, String email, Double rating, View view) {
        Intent intent;
        if ("Employee".equals(role)) {
            intent = new Intent(this, EmployeeActivity.class);
        } else if ("Employer".equals(role)) {
            intent = new Intent(this, EmployerActivity.class);
        } else {
            showErrorSnackbar(view, "Role not found");
            return;
        }
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("rating", rating);
        startActivity(intent);
        finish();
    }

    protected String getEmail() {
        return emailEditText != null ? emailEditText.getText().toString() : "";
    }

    protected String getPassword() {
        return passwordEditText != null ? passwordEditText.getText().toString() : "";
    }

    protected void showErrorSnackbar(View anchorView, String message) {
        View target = anchorView != null ? anchorView : findViewById(android.R.id.content);
        if (target == null) return;
        Snackbar.make(target, message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccessMessageWithUsername(String uid, String fallbackEmail) {
        firebase.fetchUserNameByUid(uid, new FirebaseCRUD.UserNameCallback() {
            @Override
            public void onSuccess(String userName) {
                String resolved = (userName != null && !userName.trim().isEmpty()) ? userName.trim() :
                        (fallbackEmail != null ? fallbackEmail.trim() : "Unknown");
                setSuccessText("Valid Credential: " + resolved);
            }
            @Override
            public void onError(String error) {
                String resolved = (fallbackEmail != null && !fallbackEmail.trim().isEmpty()) ? fallbackEmail.trim() : "Unknown";
                setSuccessText("Valid Credential: " + resolved);
            }
        });
    }

    private void showSuccessMessageFallback(String fallbackEmail) {
        String resolved = (fallbackEmail != null && !fallbackEmail.trim().isEmpty()) ? fallbackEmail.trim() : "Unknown";
        setSuccessText("Valid Credential: " + resolved);
    }

    protected void setSuccessText(String text) {
        if (statusTextView != null) {
            statusTextView.setText(text);
            statusTextView.setVisibility(View.VISIBLE);
        }
    }

}
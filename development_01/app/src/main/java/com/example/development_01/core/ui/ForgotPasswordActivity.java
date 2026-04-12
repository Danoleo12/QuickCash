package com.example.development_01.core.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.example.development_01.core.validation.EmailValidator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private EditText emailEditText;
    private Button submitBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpw);

        emailEditText = findViewById(R.id.emailAddress);
        submitBtn = findViewById(R.id.submitBtn);
        auth = FirebaseAuth.getInstance();

        if (emailEditText == null || submitBtn == null) {
            Log.e(TAG, "Missing required view(s) in activity_forgotpw.xml");
            return;
        }

        submitBtn.setOnClickListener(this::onSubmitClicked);
    }

    private void onSubmitClicked(View v) {
        String email = emailEditText.getText() == null ? "" : emailEditText.getText().toString().trim();

        // AC2/AC4: Email validation
        if (EmailValidator.isEmpty(email)) {
            showSnackbar(v, "Enter detail");
            return;
        }
        if (!EmailValidator.isValid(email)) {
            showSnackbar(v, "Invalid email");
            return;
        }

        // AC3: Perform Firebase password reset
        performPasswordReset(email, v);
    }

    private void performPasswordReset(String email, View view) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSnackbar(view, "Password reset email sent");
                    } else {
                        Exception ex = task.getException();
                        Log.w(TAG, "Password reset failed", ex);
                        if (ex instanceof FirebaseAuthInvalidUserException) {
                            showSnackbar(view, "Email not found");
                        } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                            showSnackbar(view, "Invalid email");
                        } else {
                            showSnackbar(view, "Password reset failed");
                        }
                    }
                });
    }


    private void showSnackbar(View anchor, String message) {
        View target = anchor != null ? anchor : findViewById(android.R.id.content);
        if (target == null) return;
        Snackbar.make(target, message, Snackbar.LENGTH_LONG).show();
    }
}

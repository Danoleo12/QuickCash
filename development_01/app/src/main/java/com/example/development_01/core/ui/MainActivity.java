package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.example.development_01.core.data.firebase.FirebaseCRUD;
import com.example.development_01.core.validation.EmailValidator;
import com.example.development_01.core.validation.PasswordValidator;
import com.example.development_01.core.validation.RoleValidator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseCRUD firebaseManager;
    private boolean isTestMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Removed auto-login check to ensure app always starts on Signup page as requested

        setContentView(R.layout.activity_main);

        setupRegistrationButton();
        setupSignInButton();

        if (!isTestMode) {
            firebaseManager = new FirebaseCRUD(FirebaseDatabase.getInstance());
        }
    }

    private void setupRegistrationButton() {
        Button registerBtn = findViewById(R.id.validateBtn);
        if (registerBtn != null) registerBtn.setOnClickListener(this);
    }

    private void setupSignInButton() {
        Button signInBtn = findViewById(R.id.signinBtn);
        if (signInBtn != null) signInBtn.setOnClickListener(this);
    }

    private void moveToSignIn() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signinBtn) {
            moveToSignIn();
            return;
        }

        String email = getEmailAddress();
        String password = getPassword();
        String role = getRole();
        String userName = getUserName();

        String errorMessage = validateInput(email, password, role, userName);
        if (errorMessage.isEmpty()) {
            if (firebaseManager != null) {
                registerUser(userName, email, password, role);
            }
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser(String userName, String email, String password, String role) {
        firebaseManager.registerUser(userName, email, password, role, new FirebaseCRUD.RegistrationCallback() {
            @Override
            public void onSuccess(String userId) {
                move2WelcomeScreen("Registration successful!", role);
            }
            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserName() {
        EditText userNameBox = findViewById(R.id.userNameBox);
        return userNameBox != null ? userNameBox.getText().toString() : "";
    }

    private String getEmailAddress() {
        EditText emailBox = findViewById(R.id.emailAddressBox);
        return emailBox != null ? emailBox.getText().toString() : "";
    }

    private String getPassword() {
        EditText passwordBox = findViewById(R.id.userPasswordBox);
        return passwordBox != null ? passwordBox.getText().toString() : "";
    }

    private String getRole() {
        RadioGroup roleGroup = findViewById(R.id.roleRadioGroup);
        if (roleGroup == null) return "";
        int id = roleGroup.getCheckedRadioButtonId();
        return id == R.id.employerRadioBtn ? "Employer" : id == R.id.employeeRadioBtn ? "Employee" : "";
    }

    private String validateInput(String email, String password, String role, String userName) {
        if (userName == null || userName.trim().isEmpty()) return "Username required";
        if (email == null || !EmailValidator.isValid(email)) return "Invalid Email";
        if (password == null || password.isEmpty()) return "Password required";
        if (role == null || role.isEmpty()) return "Select a role";
        return "";
    }

    private void move2WelcomeScreen(String message, String role) {
        Intent intent = "Employee".equalsIgnoreCase(role) ?
                new Intent(this, EmployeeActivity.class) : new Intent(this, EmployerActivity.class);
        intent.putExtra("name", getUserName());
        intent.putExtra("email", getEmailAddress());
        startActivity(intent);
        finish();
    }
}
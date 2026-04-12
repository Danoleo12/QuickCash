package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.example.development_01.core.data.PaymentInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentSettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView tvVerificationStatus;
    private EditText etAccountName, etBankName, etAccountNumber, etRoutingNumber;
    private Button btnSavePaymentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvVerificationStatus = findViewById(R.id.tvVerificationStatus);
        etAccountName = findViewById(R.id.etAccountName);
        etBankName = findViewById(R.id.etBankName);
        etAccountNumber = findViewById(R.id.etAccountNumber);
        etRoutingNumber = findViewById(R.id.etRoutingNumber);
        btnSavePaymentInfo = findViewById(R.id.btnSavePaymentInfo);

        checkUserVerification();
        loadPaymentInfo();

        btnSavePaymentInfo.setOnClickListener(v -> savePaymentInfo());
    }

    private void checkUserVerification() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please log in to access payment settings.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        //status to Not Verified as rrequested
        tvVerificationStatus.setText("Status: Not Verified");
        tvVerificationStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        btnSavePaymentInfo.setEnabled(true);
    }

    private void loadPaymentInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .collection("payment")
                    .document("details")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            PaymentInfo info = documentSnapshot.toObject(PaymentInfo.class);
                            if (info != null) {
                                etAccountName.setText(info.getAccountName());
                                etBankName.setText(info.getBankName());
                                etAccountNumber.setText(info.getAccountNumber());
                                etRoutingNumber.setText(info.getRoutingNumber());
                                
                                //if info is loaded then update status to verified
                                setStatusVerified();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    private void savePaymentInfo() {
        String accountName = etAccountName.getText().toString().trim();
        String bankName = etBankName.getText().toString().trim();
        String accountNumber = etAccountNumber.getText().toString().trim();
        String routingNumber = etRoutingNumber.getText().toString().trim();

        if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(bankName) ||
                TextUtils.isEmpty(accountNumber) || TextUtils.isEmpty(routingNumber)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentInfo info = new PaymentInfo(accountName, accountNumber, routingNumber, bankName);
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            db.collection("users").document(user.getUid())
                    .collection("payment")
                    .document("details")
                    .set(info)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Payment information saved!", Toast.LENGTH_SHORT).show();
                        //update status to verified based on successful save
                        setStatusVerified();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void setStatusVerified() {
        tvVerificationStatus.setText("Status: Verified");
        tvVerificationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

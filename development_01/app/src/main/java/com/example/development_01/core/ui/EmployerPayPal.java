package com.example.development_01.core.ui;

import android.content.Intent;
import java.math.BigDecimal;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

public class EmployerPayPal extends AppCompatActivity {
    private String jobID;
    private String jobTitle;
    private String location;
    private String applicantName;
    private String applicantEmail;
    private double jobPay;
    private TextView textView;
    private PayPalConfiguration payPalConfig;
    private FirebaseFirestore db;
    ActivityResultLauncher<Intent> activityLauncher;

    private Button btnPayWorker;
    private Button btnMarkCompleted;
    private Button btnRateWorker;
    private View cardStatusBanner;
    private TextView tvTotalDue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_pay_pal);

        db = FirebaseFirestore.getInstance();

        jobID          = getIntent().getStringExtra("JOB_ID");
        jobTitle       = getIntent().getStringExtra("JOB_TITLE");
        location       = getIntent().getStringExtra("LOCATION");
        applicantName  = getIntent().getStringExtra("APPLICANT_NAME");
        applicantEmail = getIntent().getStringExtra("APPLICANT_EMAIL");
        jobPay         = getIntent().getDoubleExtra("JOB_PAY", 0.0);

        findViewById(R.id.btnBackJobComplete).setOnClickListener(v -> finish());

        init();
        configPayPal();
        setListener();
        initActivityLauncher();
        setupRatingButton();
        startPayPalService();
    }

    private void startPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void init() {
        TextView tvJobTitle  = findViewById(R.id.tvCompleteJobTitle);
        TextView tvLocation  = findViewById(R.id.tvCompleteJobLocation);
        TextView tvWorkerName  = findViewById(R.id.tvWorkerName);
        TextView tvWorkerEmail = findViewById(R.id.tvWorkerEmail);
        TextView tvAgreedPay   = findViewById(R.id.tvAgreedPay);

        tvTotalDue       = findViewById(R.id.tvTotalDue);
        btnPayWorker     = findViewById(R.id.btnPayWorker);
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted);
        btnRateWorker    = findViewById(R.id.btnRateWorker);
        cardStatusBanner = findViewById(R.id.cardStatusBanner);
        textView         = findViewById(R.id.textView);

        tvJobTitle.setText(jobTitle);
        tvLocation.setText(location);
        tvWorkerName.setText(applicantName);
        tvWorkerEmail.setText(applicantEmail);
        tvAgreedPay.setText(getString(R.string.pay_format, jobPay));
        tvTotalDue.setText(getString(R.string.pay_format, jobPay));
    }

    private void configPayPal() {
        // PayPal Client ID commented for security:
        // String PAYPAL_CLIENT_ID = "ASGHQrl-XJxlD9l6rRLDyZqTFF75viCBWEqkxT26XBfCBU6Q7lOaz5U7dgPhn3z-T0Uc5DgXsSQJoFzY";
        String PAYPAL_CLIENT_ID = "REPLACE_WITH_YOUR_PAYPAL_CLIENT_ID";
        payPalConfig = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(PAYPAL_CLIENT_ID);
    }

    private void setListener() {
        btnMarkCompleted.setOnClickListener(v -> {
            cardStatusBanner.setVisibility(View.VISIBLE);
            btnPayWorker.setEnabled(true);
            btnPayWorker.setAlpha(1.0f);
            btnRateWorker.setEnabled(true);
            btnRateWorker.setAlpha(1.0f);
            updateFireStore("Completed");
        });

        btnPayWorker.setOnClickListener(v -> processPayment());
    }

    protected void setupRatingButton() {
        Button btnRateWorker = findViewById(R.id.btnRateWorker);
        btnRateWorker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployerPayPal.this, RatingActivity.class);
                intent.putExtra("APPLICANT_EMAIL", applicantEmail);
                startActivity(intent);
            }
        });
    }

    private void updateFireStore(String status) {
        if (jobID == null || jobID.isEmpty()) {
            Toast.makeText(this, "Invalid job ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("applications").document(jobID)
                .update("Job Status", status)
                .addOnSuccessListener(v -> Toast.makeText(this, "Job marked as " + status, Toast.LENGTH_SHORT).show());
    }

    private void initActivityLauncher() {
        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        PaymentConfirmation confirmation =
                                result.getData().getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                        if (confirmation != null) {
                            try {
                                String payID = confirmation.toJSONObject()
                                        .getJSONObject("response").getString("id");
                                String state = confirmation.toJSONObject()
                                        .getJSONObject("response").getString("state");
                                textView.setText("Payment ID: " + payID + "\nState: " + state);
                                Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void processPayment() {
        PayPalPayment payPalPayment = new PayPalPayment(
                BigDecimal.valueOf(jobPay),
                "CAD",
                "Job Payment",
                PayPalPayment.PAYMENT_INTENT_SALE
        );

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        activityLauncher.launch(intent);
    }
}
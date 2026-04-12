package com.example.development_01.core.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.development_01.R;

public class EarningsActivity extends AppCompatActivity {
    private TextView tvJobStatus;
    private Button btnGetPaidNow;
    private TextView tvPaymentStatus;
    private String jobStatus = "Completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_earnings);

            tvJobStatus = findViewById(R.id.tvJobStatus);
            btnGetPaidNow = findViewById(R.id.btnGetPaidNow);
            tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
            
            if (tvJobStatus == null || btnGetPaidNow == null || tvPaymentStatus == null) {
                Log.e("EarningsActivity", "One or more views are null. Check activity_earnings.xml IDs.");
                Toast.makeText(this, "UI Error: Required views not found.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            setupGetPaidButton();
        } catch (Exception e) {
            Log.e("EarningsActivity", "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Failed to load Earnings page.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupGetPaidButton() {
        if ("Completed".equalsIgnoreCase(jobStatus)) {
            btnGetPaidNow.setEnabled(true);
            tvJobStatus.setText("Latest Job Status: Completed");
        } else {
            btnGetPaidNow.setEnabled(false);
            tvJobStatus.setText("Latest Job Status: " + jobStatus);
            Toast.makeText(this, "Job has to be completed to get paid.", Toast.LENGTH_SHORT).show();
        }

        btnGetPaidNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });
    }

    private void processPayment() {
        btnGetPaidNow.setEnabled(false);
        tvPaymentStatus.setText("Processing payment...");
        
        tvPaymentStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvPaymentStatus.setText("Payment of $150.00 processed successfully!");
                tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                Toast.makeText(EarningsActivity.this, "Payment sent to your account!", Toast.LENGTH_LONG).show();
            }
        }, 2000);
    }
}

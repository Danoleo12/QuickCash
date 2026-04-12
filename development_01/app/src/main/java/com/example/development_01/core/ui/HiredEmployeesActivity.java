package com.example.development_01.core.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.development_01.R;
import com.example.development_01.core.core.HiredEmployee;
import com.example.development_01.core.core.HiredEmployeeAdapter;
import com.example.development_01.core.core.HiredEmployeesRepository;

import java.util.ArrayList;
import java.util.List;

public class HiredEmployeesActivity extends AppCompatActivity {

    private HiredEmployeesRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hired_employees);

        findViewById(R.id.btnBackHired).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerViewHiredEmployees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        HiredEmployeeAdapter adapter = new HiredEmployeeAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnEmployeeClickListener(emp -> {
            Intent intent = new Intent(this, EmployerPayPal.class);
            intent.putExtra("JOB_ID",         emp.getJobId());
            intent.putExtra("JOB_TITLE",      emp.getJobTitle());
            intent.putExtra("LOCATION",        emp.getLocation());
            intent.putExtra("APPLICANT_NAME",  emp.getName());
            intent.putExtra("APPLICANT_EMAIL", emp.getEmail());
            intent.putExtra("JOB_PAY",         emp.getJobPay());
            startActivity(intent);
        });

        String employerEmail = getIntent().getStringExtra("email");

        repository = new HiredEmployeesRepository();
        repository.fetchHiredEmployees(employerEmail, new HiredEmployeesRepository.HiredEmployeesCallback() {
            @Override
            public void onResult(List<HiredEmployee> employees) {
                adapter.updateList(employees);
                if (employees.isEmpty()) {
                    findViewById(R.id.layoutHiredEmptyState).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layoutHiredEmptyState).setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HiredEmployeesActivity.this,
                        "Failed to load hired employees", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

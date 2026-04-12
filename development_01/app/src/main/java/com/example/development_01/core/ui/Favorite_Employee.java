package com.example.development_01.core.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.development_01.R;
import com.example.development_01.core.core.FavoriteEmployeeAdapter;
import com.example.development_01.core.data.Employee;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Favorite_Employee extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavoriteEmployeeAdapter adapter;
    private List<Employee> favoriteList;
    private DatabaseReference favRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_employee);

        recyclerView = findViewById(R.id.recyclerViewEmployee);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoriteList = new ArrayList<>();
        adapter = new FavoriteEmployeeAdapter(favoriteList);
        recyclerView.setAdapter(adapter);

        setupFirebaseListener();
    }

    private void setupFirebaseListener() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favRef = FirebaseDatabase.getInstance().getReference("favorites").child(uid);

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Employee emp = data.getValue(Employee.class);
                    if (emp != null) favoriteList.add(emp);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Favorite_Employee.this, "Error loading favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
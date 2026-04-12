package com.example.development_01.core.core;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.development_01.R;
import com.example.development_01.core.data.Employee;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class FavoriteEmployeeAdapter extends RecyclerView.Adapter<FavoriteEmployeeAdapter.ViewHolder> {
    private List<Employee> employeeList;

    public FavoriteEmployeeAdapter(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_employee_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee emp = employeeList.get(position);
        holder.tvName.setText(emp.getName());
        holder.tvEmail.setText(emp.getEmail());

        holder.btnRemove.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String sanitizedEmail = emp.getEmail().replace(".", ",");
            FirebaseDatabase.getInstance().getReference("favorites")
                    .child(uid).child(sanitizedEmail).removeValue();
        });

        holder.btnHire.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emp.getEmail()});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Job Hiring for " + emp.getName());
            intent.putExtra(Intent.EXTRA_TEXT, "Hello " + emp.getName() + ",\n\nWe are interested in hiring you for a position...");

            try {
                v.getContext().startActivity(Intent.createChooser(intent, "Send email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(v.getContext(), "No email client found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() { return employeeList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button btnRemove, btnHire;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.employee_name);
            tvEmail = itemView.findViewById(R.id.employee_email);
            btnRemove = itemView.findViewById(R.id.removeButton);
            btnHire = itemView.findViewById(R.id.hireButton);
        }
    }
}
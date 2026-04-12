package com.example.development_01.core.core;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.development_01.R;

import java.util.List;

public class HiredEmployeeAdapter extends RecyclerView.Adapter<HiredEmployeeAdapter.ViewHolder> {

    public interface OnEmployeeClickListener {
        void onEmployeeClick(HiredEmployee employee);
    }

    private final List<HiredEmployee> employees;
    private OnEmployeeClickListener listener;

    public HiredEmployeeAdapter(List<HiredEmployee> employees) {
        this.employees = employees;
    }

    public void setOnEmployeeClickListener(OnEmployeeClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<HiredEmployee> newEmployees) {
        employees.clear();
        employees.addAll(newEmployees);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hired_employee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HiredEmployee emp = employees.get(position);
        holder.tvName.setText(emp.getName());
        holder.tvEmail.setText(emp.getEmail());
        holder.tvJobTitle.setText(emp.getJobTitle());
        holder.tvPay.setText(String.format("$%.2f", emp.getJobPay()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEmployeeClick(emp);
        });
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvEmail;
        final TextView tvJobTitle;
        final TextView tvPay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvHiredEmployeeName);
            tvEmail    = itemView.findViewById(R.id.tvHiredEmployeeEmail);
            tvJobTitle = itemView.findViewById(R.id.tvHiredEmployeeJobTitle);
            tvPay      = itemView.findViewById(R.id.tvHiredEmployeePay);
        }
    }
}

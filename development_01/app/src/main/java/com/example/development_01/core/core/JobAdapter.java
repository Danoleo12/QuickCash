package com.example.development_01.core.core;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.development_01.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList = new ArrayList<>();

    public interface OnJobClickListener {
        void onJobClick(Job job);
    }

    private OnJobClickListener onJobClickListener;

    public void setOnJobClickListener(OnJobClickListener listener) {
        this.onJobClickListener = listener;
    }

    public void setJobList(List<Job> newJobList) {
        this.jobList = newJobList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_card, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {

        if (position >= jobList.size()) return;

        Job currentJob = jobList.get(position);
        holder.tvJobTitle.setText(currentJob.getTitle());
        holder.tvLocation.setText(currentJob.getLocation());

        holder.tagGroup.removeAllViews();

        if (currentJob.getTags() != null && !currentJob.getTags().isEmpty()) {

            holder.tagGroup.setVisibility(View.VISIBLE);

            for (String tag : currentJob.getTags()) {

                Chip chip = new Chip(holder.itemView.getContext());
                chip.setText(tag);

                chip.setClickable(false);
                chip.setCheckable(false);

                holder.tagGroup.addView(chip);
            }

        } else {
            holder.tagGroup.setVisibility(View.GONE);
        }

        holder.tvCompanyName.setText(currentJob.getCompanyName());

        holder.itemView.setOnClickListener(v -> {
            if (onJobClickListener != null) {
                onJobClickListener.onJobClick(currentJob);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList == null ? 0 : jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {

        public TextView tvJobTitle;
        public TextView tvCompanyName;
        public TextView tvLocation;
        TextView tvTimePosted;
        public ChipGroup tagGroup;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);

            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvLocation = itemView.findViewById(R.id.employee_email);
            tvTimePosted = itemView.findViewById(R.id.tvTimePosted);

            tagGroup = itemView.findViewById(R.id.tagChipGroup);
        }
    }
}
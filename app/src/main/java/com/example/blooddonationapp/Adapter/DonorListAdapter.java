package com.example.blooddonationapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DonorListAdapter extends RecyclerView.Adapter<DonorListAdapter.DonorViewHolder> {
    private List<String> donorEmails;

    public DonorListAdapter(List<String> donorEmails) {
        this.donorEmails = donorEmails;
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        holder.emailTextView.setText(donorEmails.get(position));
    }

    @Override
    public int getItemCount() {
        return donorEmails.size();
    }

    static class DonorViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;

        public DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}


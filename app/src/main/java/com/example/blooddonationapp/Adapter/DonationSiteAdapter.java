package com.example.blooddonationapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DonationSiteAdapter extends RecyclerView.Adapter<DonationSiteAdapter.SiteViewHolder> {

    private List<DonationSite> siteList;
    private List<String> siteIds; // List of Firestore document IDs
    private Context context;

    public DonationSiteAdapter(List<DonationSite> siteList, Context context) {
        this.siteList = siteList;
        this.context = context;
    }

    public void setSiteIds(List<String> siteIds) {
        this.siteIds = siteIds;
    }

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_site, parent, false);
        return new SiteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        DonationSite site = siteList.get(position);
        holder.siteName.setText(site.getName());
        holder.siteLocation.setText(site.getLocation());

        String siteId = siteIds.get(position); // Get corresponding document ID

        holder.volunteerButton.setOnClickListener(v -> {
            String managerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Use siteId to update the correct Firestore document
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("donation_sites").document(siteId)
                    .update("volunteers", FieldValue.arrayUnion(managerId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "You are now a volunteer!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to volunteer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }

    static class SiteViewHolder extends RecyclerView.ViewHolder {
        TextView siteName, siteLocation, siteDate;
        Button volunteerButton;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            siteName = itemView.findViewById(R.id.siteName);
            siteLocation = itemView.findViewById(R.id.siteLocation);
            siteDate = itemView.findViewById(R.id.siteDate);
            volunteerButton = itemView.findViewById(R.id.volunteerButton);
        }
    }
}



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
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Check if the user is already a volunteer
        firestore.collection("donation_sites").document(siteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> volunteers = (List<String>) documentSnapshot.get("volunteers");

                    if (volunteers != null && volunteers.contains(currentUserId)) {
                        holder.volunteerButton.setText("Volunteered");
                        holder.volunteerButton.setOnClickListener(v -> showLeaveConfirmationDialog(siteId, currentUserId, holder.volunteerButton));
                    } else {
                        holder.volunteerButton.setText("Volunteer");
                        holder.volunteerButton.setOnClickListener(v -> joinDonationSite(siteId, currentUserId, holder.volunteerButton));
                    }
                });
    }

    private void joinDonationSite(String siteId, String userId, Button button) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("donation_sites").document(siteId)
                .update("volunteers", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "You are now a volunteer!", Toast.LENGTH_SHORT).show();
                    button.setText("Volunteered");
                    button.setOnClickListener(v -> showLeaveConfirmationDialog(siteId, userId, button));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to volunteer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLeaveConfirmationDialog(String siteId, String userId, Button button) {
        // Create a confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Leave Donation Site")
                .setMessage("Are you sure you want to stop volunteering for this site?")
                .setPositiveButton("Yes", (dialog, which) -> leaveDonationSite(siteId, userId, button))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void leaveDonationSite(String siteId, String userId, Button button) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("donation_sites").document(siteId)
                .update("volunteers", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "You have left the donation site.", Toast.LENGTH_SHORT).show();
                    button.setText("Volunteer");
                    button.setOnClickListener(v -> joinDonationSite(siteId, userId, button));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to leave: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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



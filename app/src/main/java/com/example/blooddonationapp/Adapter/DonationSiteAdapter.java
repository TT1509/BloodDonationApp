package com.example.blooddonationapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blooddonationapp.DonationSiteDetailActivity;
import com.example.blooddonationapp.Fragment.addOtherDonorsFragment;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DonationSiteAdapter extends RecyclerView.Adapter<DonationSiteAdapter.SiteViewHolder> {

    public static final int MODE_SITE_MANAGER = 0;
    public static final int MODE_DONOR = 1;


    private List<DonationSite> siteList;
    private List<String> siteIds;
    private Context context;
    private int mode;
    private String donorBloodType;

    public DonationSiteAdapter(List<DonationSite> siteList, Context context, int mode, @Nullable String donorBloodType) {
        this.siteList = siteList;
        this.context = context;
        this.mode = mode;
        this.donorBloodType = donorBloodType;
    }


    public void setSiteIds(List<String> siteIds) {
        this.siteIds = siteIds;
    }

    public void setDonorBloodType(String donorBloodType) {
        this.donorBloodType = donorBloodType;
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
        holder.siteLocation.setText(site.getAddress());

        // Reset all buttons and visibility to default state
        holder.volunteerButton.setVisibility(View.VISIBLE);
        holder.volunteerButton.setEnabled(true);
        holder.volunteerButton.setText("Volunteer");

        holder.donorButton.setVisibility(View.GONE);
        holder.donorButton.setEnabled(true);
        holder.donorButton.setText("Donate");

        holder.othersDonorButton.setVisibility(View.GONE);
        holder.finishDonationButton.setVisibility(View.GONE);

        // Set a click listener to open the detail activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DonationSiteDetailActivity.class);
            intent.putExtra("donationSite", site);
            context.startActivity(intent);
        });

        String siteId = siteIds.get(position);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Check the state of the donation site
        if ("Done".equals(site.getState())) {
            holder.volunteerButton.setText("Closed");
            holder.volunteerButton.setEnabled(false);
            holder.donorButton.setText("Closed");
            holder.donorButton.setEnabled(false);
            holder.othersDonorButton.setVisibility(View.GONE);
            holder.finishDonationButton.setVisibility(View.GONE);
            return;
        }

        // Fetch data for the specific site
        firestore.collection("donation_sites").document(siteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> volunteers = (List<String>) documentSnapshot.get("volunteers");
                    List<String> donors = (List<String>) documentSnapshot.get("donors");

                    // Setup buttons based on user mode
                    setupVolunteerButton(holder, siteId, currentUserId, volunteers);

                    if (mode == MODE_DONOR) {
                        holder.volunteerButton.setVisibility(View.GONE);
                        holder.finishDonationButton.setVisibility(View.GONE);
                        holder.donorButton.setVisibility(View.VISIBLE);
                        holder.othersDonorButton.setVisibility(View.VISIBLE);
                        holder.othersDonorButton.setText("Add Others");
                        setupDonorButton(holder, siteId, currentUserId, donors, documentSnapshot.get("requiredBloodTypes"), donorBloodType);

                        holder.othersDonorButton.setOnClickListener(v -> {
                            addOtherDonorsFragment fragment = new addOtherDonorsFragment();

                            // Pass siteId as an argument
                            Bundle args = new Bundle();
                            args.putString("siteId", siteId);
                            fragment.setArguments(args);

                            // Show the fragment
                            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "addOtherDonorsFragment");
                        });


                    } else {
                        holder.volunteerButton.setVisibility(View.VISIBLE);
                        holder.donorButton.setVisibility(View.GONE);
                        holder.othersDonorButton.setVisibility(View.GONE);
                        holder.finishDonationButton.setVisibility(View.VISIBLE);
                        holder.finishDonationButton.setOnClickListener(v -> {
                            // Update the state of the site to "Done"
                            firestore.collection("donation_sites").document(siteId)
                                    .update("state", "Done")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Donation site marked as Done.", Toast.LENGTH_SHORT).show();
                                        site.setState("Done");
                                        notifyItemChanged(position);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to mark as Done: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DonationSiteAdapter", "Error fetching site data", e);
                    Toast.makeText(context, "Failed to load site details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void setupVolunteerButton(SiteViewHolder holder, String siteId, String userId, List<String> volunteers) {
        if (volunteers != null && volunteers.contains(userId)) {
            holder.volunteerButton.setText("Volunteered");
            holder.volunteerButton.setOnClickListener(v ->
                    showLeaveConfirmationDialog(siteId, userId, holder.volunteerButton, false)
            ); // Pass false for "volunteer" role
        } else {
            holder.volunteerButton.setText("Volunteer");
            holder.volunteerButton.setOnClickListener(v -> joinAsVolunteer(siteId, userId, holder.volunteerButton));
        }
    }

    private void setupDonorButton(SiteViewHolder holder, String siteId, String userId, List<String> donors, Object requiredBloodTypesObj, String donorBloodType) {
        List<String> requiredBloodTypes = (List<String>) requiredBloodTypesObj;

        if (donors != null && donors.contains(userId)) {
            holder.donorButton.setText("Joined");
            holder.donorButton.setOnClickListener(v ->
                    showLeaveConfirmationDialog(siteId, userId, holder.donorButton, true)
            ); // Pass true for "donor" role
        } else {
            holder.donorButton.setText("Join as Donor");
            holder.donorButton.setOnClickListener(v -> {
                if (requiredBloodTypes != null && requiredBloodTypes.contains(donorBloodType)) {
                    joinAsDonor(siteId, userId, holder.donorButton);
                } else {
                    Toast.makeText(context, "Your blood type is not needed for this site.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void joinAsVolunteer(String siteId, String userId, Button button) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("donation_sites").document(siteId)
                .update("volunteers", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "You are now a volunteer!", Toast.LENGTH_SHORT).show();
                    button.setText("Volunteered");
                    button.setOnClickListener(v ->
                            showLeaveConfirmationDialog(siteId, userId, button, false)
                    ); // Pass false for "volunteer" role
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to volunteer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void joinAsDonor(String siteId, String userId, Button button) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("donation_sites").document(siteId)
                .update("donors", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "You have successfully joined as a donor!", Toast.LENGTH_SHORT).show();
                    button.setText("Joined");
                    button.setOnClickListener(v ->
                            showLeaveConfirmationDialog(siteId, userId, button, true)
                    ); // Pass true for "donor" role
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to join as a donor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void showLeaveConfirmationDialog(String siteId, String userId, Button button, boolean isDonor) {
        // Determine the appropriate title and message based on the user role
        String title = isDonor ? "Leave as Donor" : "Leave as Volunteer";
        String message = isDonor
                ? "Are you sure you want to stop being a donor for this site?"
                : "Are you sure you want to stop volunteering for this site?";

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> leaveDonationSite(siteId, userId, button, isDonor))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void leaveDonationSite(String siteId, String userId, Button button, boolean isDonor) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String field = isDonor ? "donors" : "volunteers"; // Determine the appropriate Firestore field

        firestore.collection("donation_sites").document(siteId)
                .update(field, FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    String successMessage = isDonor
                            ? "You have left the donation site as a donor."
                            : "You have left the donation site as a volunteer.";
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show();

                    String buttonText = isDonor ? "Join as Donor" : "Volunteer";
                    button.setText(buttonText);

                    // Set appropriate click behavior
                    if (isDonor) {
                        button.setOnClickListener(v -> joinAsDonor(siteId, userId, button));
                    } else {
                        button.setOnClickListener(v -> joinAsVolunteer(siteId, userId, button));
                    }
                })
                .addOnFailureListener(e -> {
                    String failureMessage = isDonor
                            ? "Failed to leave as a donor: " + e.getMessage()
                            : "Failed to leave as a volunteer: " + e.getMessage();
                    Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public int getItemCount() {
        return siteList.size();
    }

    static class SiteViewHolder extends RecyclerView.ViewHolder {
        TextView siteName, siteLocation, siteDate;
        Button volunteerButton, donorButton, othersDonorButton, finishDonationButton;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            siteName = itemView.findViewById(R.id.siteName);
            siteLocation = itemView.findViewById(R.id.siteLocation);
            siteDate = itemView.findViewById(R.id.siteDate);
            volunteerButton = itemView.findViewById(R.id.volunteerButton);
            donorButton = itemView.findViewById(R.id.donorButton);
            othersDonorButton = itemView.findViewById(R.id.othersDonorButton);
            finishDonationButton = itemView.findViewById(R.id.finishDonationButton);
        }
    }
}



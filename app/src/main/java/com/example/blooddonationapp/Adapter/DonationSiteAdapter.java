package com.example.blooddonationapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blooddonationapp.DonationSiteDetailActivity;
import com.example.blooddonationapp.Fragment.addOtherDonorsFragment;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonationSiteAdapter extends RecyclerView.Adapter<DonationSiteAdapter.SiteViewHolder> {

    public static final int MODE_SITE_MANAGER = 0;
    public static final int MODE_DONOR = 1;
    public static final int MODE_SUPER_USER = 2;


    private List<DonationSite> siteList;
    private List<String> siteIds;
    private Context context;
    private int mode;
    private String donorBloodType;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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
            holder.generateReportButton.setVisibility(View.VISIBLE);
            holder.generateReportButton.setOnClickListener(v -> generateReport(siteId));
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
                        holder.viewDonorsButton.setVisibility(View.GONE);
                        holder.viewVolunteersButton.setVisibility(View.GONE);
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


                    } else if (mode == MODE_SITE_MANAGER) {
                        holder.volunteerButton.setVisibility(View.VISIBLE);
                        holder.donorButton.setVisibility(View.GONE);
                        holder.othersDonorButton.setVisibility(View.GONE);

                        holder.finishDonationButton.setVisibility(View.VISIBLE);
                        holder.finishDonationButton.setOnClickListener(v -> {
                            // Fetch the site data to get the donors and defaultBloodValue
                            firestore.collection("donation_sites").document(siteId)
                                    .get()
                                    .addOnSuccessListener(managerSnapshot -> {
                                        if (managerSnapshot.exists()) {
                                            List<String> managerDonors = (List<String>) managerSnapshot.get("donors");
                                            double defaultBloodValue = managerSnapshot.getDouble("defaultBloodVolume");

                                            if (managerDonors == null || managerDonors.isEmpty()) {
                                                Toast.makeText(context, "No donors to update for this site.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Update donationCount for each donor
                                            for (String donorId : managerDonors) {
                                                firestore.collection("users").document(donorId)
                                                        .update("donationCount", FieldValue.increment(defaultBloodValue))
                                                        .addOnSuccessListener(aVoid -> Log.d("DonationSiteAdapter", "Donation count updated for donor: " + donorId))
                                                        .addOnFailureListener(e -> Log.e("DonationSiteAdapter", "Failed to update donation count for donor: " + donorId, e));
                                            }

                                            // Mark the site as "Done"
                                            firestore.collection("donation_sites").document(siteId)
                                                    .update("state", "Done")
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(context, "Donation site marked as Done and donors updated.", Toast.LENGTH_SHORT).show();
                                                        site.setState("Done");
                                                        notifyItemChanged(position);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Failed to mark as Done: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to fetch site data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });


                        holder.viewDonorsButton.setVisibility(View.VISIBLE);
                        holder.viewDonorsButton.setOnClickListener(v -> {
                            firestore.collection("donation_sites").document(siteId)
                                    .get()
                                    .addOnSuccessListener(managerSnapshot -> {
                                        if (managerSnapshot.exists()) {
                                            List<String> currentDonors = (List<String>) managerSnapshot.get("donors");
                                            if (currentDonors == null || currentDonors.isEmpty()) {
                                                showAlertDialog("Donors List", "No donors available for this site.");
                                            } else {
                                                fetchUserDetails(currentDonors, "Donors List", true);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to fetch donors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });

                        holder.viewVolunteersButton.setVisibility(View.VISIBLE);
                        holder.viewVolunteersButton.setOnClickListener(v -> {
                            firestore.collection("donation_sites").document(siteId)
                                    .get()
                                    .addOnSuccessListener(managerSnapshot -> {
                                        if (managerSnapshot.exists()) {
                                            List<String> currentVolunteers = (List<String>) managerSnapshot.get("volunteers");
                                            if (currentVolunteers == null || currentVolunteers.isEmpty()) {
                                                showAlertDialog("Volunteers List", "No volunteers available for this site.");
                                            } else {
                                                fetchUserDetails(currentVolunteers, "Volunteers List", false);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to fetch volunteers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });

                        holder.downloadDonorsButton.setVisibility(View.VISIBLE);
                        holder.downloadDonorsButton.setOnClickListener(v -> {
                            firestore.collection("donation_sites").document(siteId)
                                    .get()
                                    .addOnSuccessListener(managerSnapshot -> {
                                        if (managerSnapshot.exists()) {
                                            List<String> donorIds = (List<String>) managerSnapshot.get("donors");
                                            if (donorIds == null || donorIds.isEmpty()) {
                                                Toast.makeText(context, "No donors available for this site.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                downloadDonorsList(siteId, donorIds);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to fetch site donors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        });


                    } else {
                        holder.volunteerButton.setVisibility(View.GONE);
                        holder.donorButton.setVisibility(View.GONE);
                        holder.othersDonorButton.setVisibility(View.GONE);
                        holder.finishDonationButton.setVisibility(View.GONE);
                        holder.viewDonorsButton.setVisibility(View.GONE);
                        holder.viewVolunteersButton.setVisibility(View.GONE);

                        if ("Done".equals(site.getState())) {
                            holder.generateReportButton.setVisibility(View.VISIBLE);
                            holder.generateReportButton.setOnClickListener(v -> generateReport(siteId));
                        } else {
                            holder.generateReportButton.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DonationSiteAdapter", "Error fetching site data", e);
                    Toast.makeText(context, "Failed to load site details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void downloadDonorsList(String siteId, List<String> donorIds) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Name,Gender,Date of Birth,Email,Phone Number,Blood Type,Height (cm),Weight (kg)\n");

        for (String donorId : donorIds) {
            firestore.collection("users").document(donorId)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            String name = userSnapshot.getString("name");
                            String gender = userSnapshot.getString("gender");
                            Date dateOfBirth = userSnapshot.getDate("dateOfBirth");
                            String email = userSnapshot.getString("email");
                            Integer phoneNumber = userSnapshot.getLong("phoneNumber") != null
                                    ? userSnapshot.getLong("phoneNumber").intValue()
                                    : null;
                            String bloodType = userSnapshot.getString("bloodType");
                            Double height = userSnapshot.getDouble("height");
                            Double weight = userSnapshot.getDouble("weight");

                            csvBuilder.append(name).append(",")
                                    .append(gender).append(",")
                                    .append(dateOfBirth != null ? dateOfBirth.toString() : "").append(",")
                                    .append(email).append(",")
                                    .append(phoneNumber != null ? phoneNumber : "").append(",")
                                    .append(bloodType).append(",")
                                    .append(height != null ? height : "").append(",")
                                    .append(weight != null ? weight : "").append("\n");
                        }

                        if (csvBuilder.toString().split("\n").length == donorIds.size() + 1) {
                            saveAndShareCSV(siteId, csvBuilder.toString());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to fetch donor details: " + donorId, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveAndShareCSV(String siteId, String csvData) {
        try {
            String fileName = "Donors_List_" + siteId + ".csv";
            File file = new File(context.getExternalFilesDir(null), fileName);

            FileWriter writer = new FileWriter(file);
            writer.write(csvData);
            writer.close();

            // Share the file
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            context.startActivity(Intent.createChooser(shareIntent, "Share Donors List"));

            Toast.makeText(context, "File saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchUserDetails(List<String> userIds, String title, boolean isDonor) {
        StringBuilder detailsBuilder = new StringBuilder();

        for (String userId : userIds) {
            firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            String name = userSnapshot.getString("name");
                            String email = userSnapshot.getString("email");
                            Integer phoneNumber = userSnapshot.getLong("phoneNumber") != null
                                    ? userSnapshot.getLong("phoneNumber").intValue()
                                    : null;
                            Date dateOfBirth = userSnapshot.getDate("dateOfBirth");
                            String gender = userSnapshot.getString("gender");

                            if (isDonor) {
                                String bloodType = userSnapshot.getString("bloodType");
                                Integer donationCount = userSnapshot.getLong("donationCount") != null
                                        ? userSnapshot.getLong("donationCount").intValue()
                                        : null;
                                Double height = userSnapshot.getDouble("height");
                                Double weight = userSnapshot.getDouble("weight");

                                detailsBuilder.append("Name: ").append(name).append("\n")
                                        .append("Gender: ").append(gender).append("\n")
                                        .append("Date of Birth: ").append(dateOfBirth != null ? dateOfBirth.toString() : "N/A").append("\n")
                                        .append("Email: ").append(email).append("\n")
                                        .append("Phone: ").append(phoneNumber != null ? phoneNumber : "N/A").append("\n")
                                        .append("Blood Type: ").append(bloodType).append("\n")
                                        .append("Donation Count: ").append(donationCount != null ? donationCount : "N/A").append("\n")
                                        .append("Height: ").append(height != null ? height + " cm" : "N/A").append("\n")
                                        .append("Weight: ").append(weight != null ? weight + " kg" : "N/A").append("\n\n");
                            } else {
                                detailsBuilder.append("Name: ").append(name).append("\n")
                                        .append("Gender: ").append(gender).append("\n")
                                        .append("Date of Birth: ").append(dateOfBirth != null ? dateOfBirth.toString() : "N/A").append("\n")
                                        .append("Email: ").append(email).append("\n")
                                        .append("Phone: ").append(phoneNumber != null ? phoneNumber : "N/A").append("\n\n");
                            }

                            // Show the dialog when all users are processed
                            if (detailsBuilder.toString().split("\n\n").length == userIds.size()) {
                                showAlertDialog(title, detailsBuilder.toString());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to fetch user details for ID: " + userId, Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }



    private void setupVolunteerButton(SiteViewHolder holder, String siteId, String userId, List<String> volunteers) {
        if (volunteers != null && volunteers.contains(userId)) {
            holder.volunteerButton.setText("Volunteered");
            holder.volunteerButton.setOnClickListener(v ->
                    showLeaveConfirmationDialog(siteId, userId, holder.volunteerButton, false)
            );
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

    private void generateReport(String siteId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("donation_sites").document(siteId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetching site data
                        List<String> donors = (List<String>) documentSnapshot.get("donors");
                        List<String> requiredBloodTypes = (List<String>) documentSnapshot.get("requiredBloodTypes");
                        double defaultBloodVolume = documentSnapshot.getDouble("defaultBloodVolume");

                        if (donors == null || donors.isEmpty()) {
                            Toast.makeText(context, "No donors available for this site.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Initialize report variables
                        int totalDonors = donors.size();
                        double totalVolumeCollected = totalDonors * defaultBloodVolume;

                        Map<String, Double> bloodTypeVolumes = new HashMap<>();
                        for (String bloodType : requiredBloodTypes) {
                            bloodTypeVolumes.put(bloodType, 0.0);
                        }

                        // Fetch donor details from users collection
                        firestore.collection("users").get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (String donorId : donors) {
                                        querySnapshot.getDocuments().stream()
                                                .filter(doc -> doc.getId().equals(donorId))
                                                .findFirst()
                                                .ifPresent(donorDoc -> {
                                                    String bloodType = donorDoc.getString("bloodType");
                                                    if (bloodTypeVolumes.containsKey(bloodType)) {
                                                        double currentVolume = bloodTypeVolumes.get(bloodType);
                                                        bloodTypeVolumes.put(bloodType, currentVolume + defaultBloodVolume);
                                                    }
                                                });
                                    }

                                    // Build and display the report
                                    StringBuilder report = new StringBuilder();
                                    report.append("Report for Site ID: ").append(siteId).append("\n");
                                    report.append("Total Donors: ").append(totalDonors).append("\n");
                                    report.append("Total Blood Volume Collected: ").append(totalVolumeCollected).append(" mL\n");
                                    report.append("Blood Volume Collected by Type:\n");
                                    for (Map.Entry<String, Double> entry : bloodTypeVolumes.entrySet()) {
                                        report.append(entry.getKey()).append(": ").append(entry.getValue()).append(" mL\n");
                                    }

                                    new androidx.appcompat.app.AlertDialog.Builder(context)
                                            .setTitle("Donation Site Report")
                                            .setMessage(report.toString())
                                            .setPositiveButton("OK", null)
                                            .show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "No data available for this site.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to fetch site data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public int getItemCount() {
        return siteList.size();
    }

    static class SiteViewHolder extends RecyclerView.ViewHolder {
        TextView siteName, siteLocation, siteDate;
        Button volunteerButton, donorButton, othersDonorButton, finishDonationButton, generateReportButton, viewDonorsButton, viewVolunteersButton, downloadDonorsButton;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            siteName = itemView.findViewById(R.id.siteName);
            siteLocation = itemView.findViewById(R.id.siteLocation);
            siteDate = itemView.findViewById(R.id.siteDate);
            volunteerButton = itemView.findViewById(R.id.volunteerButton);
            donorButton = itemView.findViewById(R.id.donorButton);
            othersDonorButton = itemView.findViewById(R.id.othersDonorButton);
            finishDonationButton = itemView.findViewById(R.id.finishDonationButton);
            generateReportButton = itemView.findViewById(R.id.generateReportButton);
            viewDonorsButton = itemView.findViewById(R.id.viewDonorsButton);
            viewVolunteersButton = itemView.findViewById(R.id.viewVolunteersButton);
            downloadDonorsButton = itemView.findViewById(R.id.downloadDonorsButton);
        }
    }
}



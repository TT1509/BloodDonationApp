package com.example.blooddonationapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.Model.SiteManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateSiteActivity extends AppCompatActivity {

    private EditText siteNameInput, addressInput, contactInput;
    private Button dateButton, timeButton, saveSiteButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Date siteDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_site);

        siteNameInput = findViewById(R.id.siteNameInput);
        addressInput = findViewById(R.id.addressInput);
        contactInput = findViewById(R.id.contactInput);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        saveSiteButton = findViewById(R.id.saveSiteButton);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Calendar calendar = Calendar.getInstance();

        // Date picker
        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        updateSiteDateTime(calendar);
                        dateButton.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Time picker
        timeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateSiteDateTime(calendar);
                        timeButton.setText(String.format("%02d:%02d", hourOfDay, minute));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        saveSiteButton.setOnClickListener(v -> createDonationSite());
    }

    private void updateSiteDateTime(Calendar calendar) {
        siteDateTime = calendar.getTime();
    }

    private void createDonationSite() {
        String siteName = siteNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();

        if (siteName.isEmpty() || address.isEmpty() || contact.isEmpty() || siteDateTime == null) {
            Toast.makeText(this, "Please fill in all fields and select date/time", Toast.LENGTH_SHORT).show();
            return;
        }

        String managerId = auth.getCurrentUser().getUid();

        DonationSite site = new DonationSite(
                managerId,
                siteName,
                address,
                siteDateTime,
                contact
        );

        firestore.collection("donation_sites")
                .add(site)
                .addOnSuccessListener(documentReference -> {
                    // After the site is created, get the created site's ID
                    String donationSiteId = documentReference.getId();
                    // Call the method to add this site ID to the SiteManager's managedSites list
                    addDonationSiteToManager(managerId, donationSiteId);

                    Toast.makeText(CreateSiteActivity.this, "Site created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateSiteActivity.this, "Failed to create site: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addDonationSiteToManager(String managerId, String donationSiteId) {
        // Reference to the SiteManager document in Firestore
        DocumentReference managerDocRef = firestore.collection("users").document(managerId);

        // Get the current SiteManager data and add the new site ID to their managedSites list
        managerDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                SiteManager siteManager = documentSnapshot.toObject(SiteManager.class);

                if (siteManager != null) {
                    // Initialize the managedSites list if it's null
                    if (siteManager.getManagedSites() == null) {
                        siteManager.setManagedSites(new ArrayList<>());
                    }

                    // Add the donation site ID to the manager's list of managed sites
                    siteManager.addManagedSite(donationSiteId);

                    // Update the SiteManager document with the new managedSites list
                    managerDocRef.update("managedSites", siteManager.getManagedSites())
                            .addOnSuccessListener(aVoid -> {
                                Log.d("CreateSiteActivity", "Site ID added to manager's managedSites list.");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CreateSiteActivity", "Failed to update manager's managedSites list: " + e.getMessage());
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("CreateSiteActivity", "Error fetching manager data: " + e.getMessage());
        });
    }
}
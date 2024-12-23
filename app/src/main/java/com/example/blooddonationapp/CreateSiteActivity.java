package com.example.blooddonationapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.Model.SiteManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CreateSiteActivity extends AppCompatActivity {
    private EditText siteNameInput, addressInput, contactInput, defaultVolumeInput;
    private Button dateButton, timeButton, saveSiteButton, selectLocationButton, selectBloodTypesButton;
    private LinearLayout bloodTypeLayout;
    private ArrayList<String> selectedBloodTypes = new ArrayList<>();
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Date siteDateTime;
    private double selectedLatitude, selectedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_site);

        siteNameInput = findViewById(R.id.siteNameInput);
        addressInput = findViewById(R.id.addressInput);
        contactInput = findViewById(R.id.contactInput);
        defaultVolumeInput = findViewById(R.id.defaultVolumeInput);
        selectBloodTypesButton = findViewById(R.id.selectBloodTypesButton);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        saveSiteButton = findViewById(R.id.saveSiteButton);
        selectLocationButton = findViewById(R.id.selectLocationButton);
        bloodTypeLayout = findViewById(R.id.bloodTypeLayout);

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

        // Select location
        selectLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateSiteActivity.this, LocationPickerActivity.class);
            locationPickerLauncher.launch(intent);
        });


        // Handle the button for blood type selection
        selectBloodTypesButton.setOnClickListener(v -> toggleBloodTypeSelection());

        // Add checkboxes for each blood type
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (String bloodType : bloodTypes) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(bloodType);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedBloodTypes.add(bloodType);
                } else {
                    selectedBloodTypes.remove(bloodType);
                }
                updateSelectedBloodTypesButton();
            });
            bloodTypeLayout.addView(checkBox);
        }

        // Save donation site
        saveSiteButton.setOnClickListener(v -> createDonationSite());
    }

    private void updateSiteDateTime(Calendar calendar) {
        siteDateTime = calendar.getTime();
    }

    private void toggleBloodTypeSelection() {
        if (bloodTypeLayout.getVisibility() == View.VISIBLE) {
            bloodTypeLayout.setVisibility(View.GONE);
            selectBloodTypesButton.setText("Select Blood Types");
        } else {
            bloodTypeLayout.setVisibility(View.VISIBLE);
            selectBloodTypesButton.setText("Hide Blood Types");
        }
    }

    private void updateSelectedBloodTypesButton() {
        if (selectedBloodTypes.isEmpty()) {
            selectBloodTypesButton.setText("Select Blood Types");
        } else {
            selectBloodTypesButton.setText(String.join(", ", selectedBloodTypes));
        }
    }

    private final ActivityResultLauncher<Intent> locationPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        selectedLatitude = data.getDoubleExtra("latitude", 0.0);
                        selectedLongitude = data.getDoubleExtra("longitude", 0.0);
                        Toast.makeText(this, "Location selected: (" + selectedLatitude + ", " + selectedLongitude + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void createDonationSite() {
        String siteName = siteNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        String defaultVolumeStr = defaultVolumeInput.getText().toString().trim();

        if (siteName.isEmpty() || address.isEmpty() || contact.isEmpty() || siteDateTime == null ||
                (selectedLatitude == 0.0 && selectedLongitude == 0.0) || selectedBloodTypes.isEmpty() ||
                defaultVolumeStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields, select date/time, and choose a location.", Toast.LENGTH_SHORT).show();
            return;
        }

        int defaultBloodVolume;
        try {
            defaultBloodVolume = Integer.parseInt(defaultVolumeStr);
            if (defaultBloodVolume < 350 || defaultBloodVolume > 500) {
                Toast.makeText(this, "Default blood volume must be between 350 and 500 ml.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid blood volume.", Toast.LENGTH_SHORT).show();
            return;
        }

        String managerId = auth.getCurrentUser().getUid();

        DonationSite site = new DonationSite(
                managerId,
                siteName,
                address,
                siteDateTime,
                contact,
                selectedBloodTypes,
                selectedLatitude,
                selectedLongitude,
                defaultBloodVolume
        );

        firestore.collection("donation_sites")
                .add(site)
                .addOnSuccessListener(documentReference -> {
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
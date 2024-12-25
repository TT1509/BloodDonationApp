package com.example.blooddonationapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UpdateDonationSiteActivity extends AppCompatActivity {

    private EditText siteNameInput, addressInput, contactInput, defaultVolumeInput;
    private Button selectBloodTypesButton, dateButton, startTimeButton, endTimeButton, saveSiteButton, selectLocationButton;
    private LinearLayout bloodTypeLayout;
    private ArrayList<String> selectedBloodTypes = new ArrayList<>();
    private double selectedLatitude, selectedLongitude;
    private String siteId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_donation_site);

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize Views
        siteNameInput = findViewById(R.id.siteNameInput);
        addressInput = findViewById(R.id.addressInput);
        contactInput = findViewById(R.id.contactInput);
        defaultVolumeInput = findViewById(R.id.defaultVolumeInput);
        selectBloodTypesButton = findViewById(R.id.selectBloodTypesButton);
        dateButton = findViewById(R.id.dateButton);
        startTimeButton = findViewById(R.id.startTimeButton);
        endTimeButton = findViewById(R.id.endTimeButton);
        saveSiteButton = findViewById(R.id.saveSiteButton);
        selectLocationButton = findViewById(R.id.selectLocationButton);
        bloodTypeLayout = findViewById(R.id.bloodTypeLayout);

        // Get site ID and data passed via intent
        siteId = getIntent().getStringExtra("donationSiteId");
        populateFields();

        // Set up button actions
        selectBloodTypesButton.setOnClickListener(v -> toggleBloodTypeSelection());
        dateButton.setOnClickListener(v -> showDatePicker());
        startTimeButton.setOnClickListener(v -> showTimePicker(startTimeButton));
        endTimeButton.setOnClickListener(v -> showTimePicker(endTimeButton));
        selectLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateDonationSiteActivity.this, LocationPickerActivity.class);
            locationPickerLauncher.launch(intent);
        });
        saveSiteButton.setOnClickListener(v -> saveUpdates());

        // Add checkboxes for blood types
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
    }

    private void populateFields() {
        // Load site data into fields
        firestore.collection("donation_sites").document(siteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        siteNameInput.setText(documentSnapshot.getString("name"));
                        addressInput.setText(documentSnapshot.getString("address"));
                        contactInput.setText(documentSnapshot.getString("contact"));
                        defaultVolumeInput.setText(String.valueOf(documentSnapshot.getDouble("defaultBloodVolume")));

                        selectedLatitude = documentSnapshot.getDouble("latitude");
                        selectedLongitude = documentSnapshot.getDouble("longitude");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load site data.", Toast.LENGTH_SHORT).show());
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

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            dateButton.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(Button button) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
            button.setText(selectedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void saveUpdates() {
        Map<String, Object> updatedData = new HashMap<>();

        // Update site name if not empty
        String siteName = siteNameInput.getText().toString().trim();
        if (!siteName.isEmpty()) {
            updatedData.put("name", siteName);
        }

        // Update address if not empty
        String address = addressInput.getText().toString().trim();
        if (!address.isEmpty()) {
            updatedData.put("address", address);
        }

        // Update contact if not empty
        String contact = contactInput.getText().toString().trim();
        if (!contact.isEmpty()) {
            updatedData.put("contact", contact);
        }

        // Update default blood volume if valid and not empty
        String defaultVolumeStr = defaultVolumeInput.getText().toString().trim();
        if (!defaultVolumeStr.isEmpty()) {
            try {
                int defaultBloodVolume = Integer.parseInt(defaultVolumeStr);
                if (defaultBloodVolume >= 350 && defaultBloodVolume <= 500) {
                    updatedData.put("defaultBloodVolume", defaultBloodVolume);
                } else {
                    Toast.makeText(this, "Default blood volume must be between 350 and 500 ml.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid blood volume.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Update latitude and longitude if valid
        if (selectedLatitude != 0.0 && selectedLongitude != 0.0) {
            updatedData.put("latitude", selectedLatitude);
            updatedData.put("longitude", selectedLongitude);
        }

        // Update blood types if not empty
        if (!selectedBloodTypes.isEmpty()) {
            updatedData.put("requiredBloodTypes", selectedBloodTypes);
        }

        // Check if there's any data to update
        if (updatedData.isEmpty()) {
            Toast.makeText(this, "No changes to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore
        firestore.collection("donation_sites").document(siteId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Donation site updated successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update site: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}

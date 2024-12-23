package com.example.blooddonationapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blooddonationapp.Fragment.MapsFragment;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.Model.User;
import com.example.blooddonationapp.Utils.FirestoreUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DonationSiteDetailActivity extends AppCompatActivity {

    private TextView siteName, siteLocation, siteDescription, siteDate, siteStartTime, siteEndTime, siteContact;
    private TextView managerNameTextView;
    private TextView managerEmailTextView;
    private TextView managerPhoneTextView;

    private FirebaseFirestore firestore;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_site_detail);

        siteName = findViewById(R.id.siteName);
        siteLocation = findViewById(R.id.siteLocation);
        siteDescription = findViewById(R.id.siteDescription);
        siteDate = findViewById(R.id.siteDate);
        siteStartTime = findViewById(R.id.siteStartTime);
        siteEndTime = findViewById(R.id.siteEndTime);
        siteContact = findViewById(R.id.siteContact);
        managerNameTextView = findViewById(R.id.manager_name);
        managerEmailTextView = findViewById(R.id.manager_email);
        managerPhoneTextView = findViewById(R.id.manager_phone);


        DonationSite site = (DonationSite) getIntent().getSerializableExtra("donationSite");
        if (site != null) {
            siteName.setText("Donation Site Name: " + site.getName());
            siteLocation.setText("Address: " + site.getAddress());
            siteDescription.setText("Required Blood Types: " + site.getRequiredBloodTypes());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            siteDate.setText("Date: " + dateFormat.format(site.getDate()));
            siteStartTime.setText("Start Time: " + timeFormat.format(site.getStartTime()));
            siteEndTime.setText("End Time: " + timeFormat.format(site.getEndTime()));
            siteContact.setText("Contact Info: " + site.getContactInfo());
            fetchAndDisplayManagerDetails(site.getManagerId());

            // Initialize MapsFragment and pass the latitude and longitude
            MapsFragment mapsFragment = new MapsFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", site.getLatitude());
            bundle.putDouble("longitude", site.getLongitude());
            bundle.putString("siteName", site.getName());
            mapsFragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapFragmentContainer, mapsFragment)
                    .commit();
        } else {
            Toast.makeText(this, "Error loading site details", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndDisplayManagerDetails(String managerId) {
        FirestoreUtils.fetchManagerDetails(managerId, new FirestoreUtils.ManagerDetailsCallback() {
            @Override
            public void onSuccess(User manager) {
                managerNameTextView.setText("Site Manager: " + manager.getName());
                managerEmailTextView.setText("Site Manager's Email: " + manager.getEmail());
                managerPhoneTextView.setText("Site Manager's PhoneNumber: " + String.valueOf(manager.getPhoneNumber()));
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(DonationSiteDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

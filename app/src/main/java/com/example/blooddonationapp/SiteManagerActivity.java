package com.example.blooddonationapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blooddonationapp.Adapter.DonationSiteAdapter;
import com.example.blooddonationapp.Model.DonationSite;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SiteManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_manager);

        Button createSiteButton = findViewById(R.id.createSiteButton);
        createSiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(SiteManagerActivity.this, CreateSiteActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        siteList = new ArrayList<>();
        adapter = new DonationSiteAdapter(siteList);
        recyclerView.setAdapter(adapter);

        loadDonationSites();
    }

    private void loadDonationSites() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("donation_sites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonationSite> sites = queryDocumentSnapshots.toObjects(DonationSite.class);
                    siteList.clear();
                    siteList.addAll(sites);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SiteManagerActivity.this, "Failed to load sites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DonationSiteList", "Error loading sites", e);
                });
    }


}
package com.example.blooddonationapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blooddonationapp.Adapter.DonationSiteAdapter;
import com.example.blooddonationapp.CreateSiteActivity;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SiteManagerFragment extends Fragment {

    private RecyclerView recyclerView;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_site_manager, container, false);

        Button createSiteButton = view.findViewById(R.id.createSiteButton);
        createSiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateSiteActivity.class);
            startActivity(intent);
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        siteList = new ArrayList<>();
        adapter = new DonationSiteAdapter(siteList, getContext());
        recyclerView.setAdapter(adapter);

        loadDonationSites();
        return view;
    }

    private void loadDonationSites() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("donation_sites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    siteList.clear();
                    List<String> siteIds = new ArrayList<>(); // Store document IDs

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        DonationSite site = doc.toObject(DonationSite.class);
                        siteIds.add(doc.getId()); // Save Firestore document ID
                        siteList.add(site);
                    }

                    // Pass siteIds to the adapter along with siteList
                    adapter.setSiteIds(siteIds);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to load sites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DonationSiteList", "Error loading sites", e);
                });
    }

}


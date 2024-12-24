package com.example.blooddonationapp.Fragment;

import static android.content.ContentValues.TAG;

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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blooddonationapp.Adapter.DonationSiteAdapter;
import com.example.blooddonationapp.CreateSiteActivity;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.example.blooddonationapp.Utils.FirestoreUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SiteManagerFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList;
    private List<DonationSite> filteredList = new ArrayList<>();
    private static final String TAG = "SiteManagerFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_site_manager, container, false);

        Button createSiteButton = view.findViewById(R.id.createSiteButton);
        createSiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateSiteActivity.class);
            startActivity(intent);
        });

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        siteList = new ArrayList<>();
        int mode = DonationSiteAdapter.MODE_SITE_MANAGER;
        adapter = new DonationSiteAdapter(filteredList, getContext(), mode, null); // Use filteredList here
        recyclerView.setAdapter(adapter);

        loadDonationSites();
        setupSearchView();
        return view;
    }

    private void loadDonationSites() {
        FirestoreUtils.loadDonationSites(getContext(), (siteList, siteIds) -> {
            this.siteList.clear();
            this.siteList.addAll(siteList);

            filteredList.clear();
            filteredList.addAll(siteList);

            if (adapter != null) {
                adapter.setSiteIds(siteIds);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSites(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSites(newText);
                return true;
            }
        });
    }

    private void filterSites(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(siteList);
        } else {
            for (DonationSite site : siteList) {
                if (site.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(site);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "Adapter is null, cannot update filtered results.");
        }
    }
}



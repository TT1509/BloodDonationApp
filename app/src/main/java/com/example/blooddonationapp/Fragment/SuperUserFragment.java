package com.example.blooddonationapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;

import com.example.blooddonationapp.Adapter.DonationSiteAdapter;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.example.blooddonationapp.Utils.FirestoreUtils;

import java.util.ArrayList;
import java.util.List;

public class SuperUserFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList;
    private List<DonationSite> filteredList = new ArrayList<>();
    private static final String TAG = "SuperUserFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_super_user, container, false);

        // Initialize SearchView and RecyclerView
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize site list and adapter
        siteList = new ArrayList<>();
        adapter = new DonationSiteAdapter(filteredList, getContext(), DonationSiteAdapter.MODE_SUPER_USER, null);
        recyclerView.setAdapter(adapter);

        // Load donation sites and setup search
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

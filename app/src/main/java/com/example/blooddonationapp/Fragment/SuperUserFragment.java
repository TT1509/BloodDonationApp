package com.example.blooddonationapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.blooddonationapp.Adapter.DonationSiteAdapter;
import com.example.blooddonationapp.CreateSiteActivity;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.example.blooddonationapp.Utils.FirestoreUtils;

import java.util.ArrayList;
import java.util.List;

public class SuperUserFragment extends Fragment {

    private RecyclerView recyclerView;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_super_user, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize site list and adapter
        siteList = new ArrayList<>();
        adapter = new DonationSiteAdapter(siteList, getContext(), DonationSiteAdapter.MODE_SUPER_USER, null);
        recyclerView.setAdapter(adapter);

        // Load donation sites
        loadDonationSites();

        return view;
    }

    private void loadDonationSites() {
        FirestoreUtils.loadDonationSites(getContext(), (siteList, siteIds) -> {
            this.siteList.clear();
            this.siteList.addAll(siteList);

            if (adapter != null) {
                adapter.setSiteIds(siteIds);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
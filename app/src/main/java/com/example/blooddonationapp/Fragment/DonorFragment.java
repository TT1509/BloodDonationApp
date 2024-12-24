package com.example.blooddonationapp.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.example.blooddonationapp.Adapter.DonationSiteAdapter;
import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;
import com.example.blooddonationapp.Utils.FirestoreUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DonorFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList = new ArrayList<>();
    private List<DonationSite> filteredList = new ArrayList<>();
    private View filterContainer;
    private ImageView filterIcon;
    private CheckBox checkBoxA, checkBoxB, checkBoxO, checkBoxAB, checkBoxAMinute, checkBoxBMinute, checkBoxOMinute, checkBoxABMinute;
    private RadioGroup sortGroup;
    private static final String TAG = "DonorFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donor, container, false);

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        filterContainer = view.findViewById(R.id.filterContainer);
        filterIcon = view.findViewById(R.id.filterIcon);
        checkBoxA = view.findViewById(R.id.checkBoxA);
        checkBoxB = view.findViewById(R.id.checkBoxB);
        checkBoxO = view.findViewById(R.id.checkBoxO);
        checkBoxAB = view.findViewById(R.id.checkBoxAB);
        checkBoxAMinute = view.findViewById(R.id.checkBoxAMinute);
        checkBoxBMinute = view.findViewById(R.id.checkBoxBMinute);
        checkBoxOMinute = view.findViewById(R.id.checkBoxOMinute);
        checkBoxABMinute = view.findViewById(R.id.checkBoxABMinute);
        sortGroup = view.findViewById(R.id.sortGroup);

        filterContainer.setVisibility(View.GONE);

        filterIcon.setOnClickListener(v -> {
            if (filterContainer.getVisibility() == View.GONE) {
                filterContainer.setVisibility(View.VISIBLE);
            } else {
                filterContainer.setVisibility(View.GONE);
            }
        });

        setupAdapter();
        loadDonationSites();
        setupSearchView();
        setupFilterActions();

        return view;
    }

    private void setupAdapter() {
        adapter = new DonationSiteAdapter(filteredList, getContext(), DonationSiteAdapter.MODE_DONOR, null);
        recyclerView.setAdapter(adapter);
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
                filterBySearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBySearch(newText);
                return true;
            }
        });
    }

    private void filterBySearch(String query) {
        List<DonationSite> tempFilteredList = new ArrayList<>();
        for (DonationSite site : siteList) {
            if (site.getName().toLowerCase().contains(query.toLowerCase())) {
                tempFilteredList.add(site);
            }
        }
        filteredList.clear();
        filteredList.addAll(tempFilteredList);
        adapter.notifyDataSetChanged();
    }

    private void setupFilterActions() {
        View.OnClickListener filterListener = v -> applyFilters();
        checkBoxA.setOnClickListener(filterListener);
        checkBoxB.setOnClickListener(filterListener);
        checkBoxO.setOnClickListener(filterListener);
        checkBoxAB.setOnClickListener(filterListener);
        checkBoxAMinute.setOnClickListener(filterListener);
        checkBoxBMinute.setOnClickListener(filterListener);
        checkBoxOMinute.setOnClickListener(filterListener);
        checkBoxABMinute.setOnClickListener(filterListener);
        sortGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private void applyFilters() {
        filteredList.clear();

        // Collect selected blood types
        List<String> selectedBloodTypes = new ArrayList<>();
        if (checkBoxA.isChecked()) selectedBloodTypes.add("A+");
        if (checkBoxB.isChecked()) selectedBloodTypes.add("B+");
        if (checkBoxO.isChecked()) selectedBloodTypes.add("O+");
        if (checkBoxAB.isChecked()) selectedBloodTypes.add("AB+");
        if (checkBoxAMinute.isChecked()) selectedBloodTypes.add("A-");
        if (checkBoxBMinute.isChecked()) selectedBloodTypes.add("B-");
        if (checkBoxOMinute.isChecked()) selectedBloodTypes.add("O-");
        if (checkBoxABMinute.isChecked()) selectedBloodTypes.add("AB-");

        // Filter sites by selected blood types
        for (DonationSite site : siteList) {
            // Check if there is any overlap between requiredBloodTypes and selectedBloodTypes
            boolean matchesBloodType = selectedBloodTypes.isEmpty() ||
                    site.getRequiredBloodTypes().stream().anyMatch(selectedBloodTypes::contains);

            if (matchesBloodType) {
                filteredList.add(site);
            }
        }

        // Apply sorting
        int checkedSort = sortGroup.getCheckedRadioButtonId();
        if (checkedSort == R.id.radioEarliest) {
            Collections.sort(filteredList, (a, b) -> a.getDate().compareTo(b.getDate()));
        } else if (checkedSort == R.id.radioLatest) {
            Collections.sort(filteredList, (a, b) -> b.getDate().compareTo(a.getDate()));
        }

        // Notify adapter of changes
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "Adapter is null, cannot update filtered results.");
        }
    }

}

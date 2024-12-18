package com.example.blooddonationapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.blooddonationapp.Adapter.TabViewAdapter;
import com.example.blooddonationapp.Fragment.AccountSettingsFragment;
import com.example.blooddonationapp.Fragment.SiteManagerFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class SiteManagerActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_manager_tab);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        setupTabs();
    }

    private void setupTabs() {
        List<String> tabTitles = new ArrayList<>();
        tabTitles.add("Main Page");
        tabTitles.add("Account Settings");

        List<Class<?>> fragmentClasses = new ArrayList<>();
        fragmentClasses.add(SiteManagerFragment.class);
        fragmentClasses.add(AccountSettingsFragment.class);

        adapter = new TabViewAdapter(this, fragmentClasses);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))).attach();
    }
}

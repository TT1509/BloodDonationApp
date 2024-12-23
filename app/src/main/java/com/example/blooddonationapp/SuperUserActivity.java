package com.example.blooddonationapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.blooddonationapp.Adapter.TabViewAdapter;
import com.example.blooddonationapp.Fragment.AccountSettingsFragment;
import com.example.blooddonationapp.Fragment.SiteManagerFragment;
import com.example.blooddonationapp.Fragment.SuperUserFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class SuperUserActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);

        setupTabs();
    }

    private void setupTabs() {
        List<String> tabTitles = new ArrayList<>();
        tabTitles.add("Main Page");
        tabTitles.add("Account Settings");

        List<Class<?>> fragmentClasses = new ArrayList<>();
        fragmentClasses.add(SuperUserFragment.class);
        fragmentClasses.add(AccountSettingsFragment.class);

        adapter = new TabViewAdapter(this, fragmentClasses);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))).attach();
    }
}
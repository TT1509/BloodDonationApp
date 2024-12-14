package com.example.blooddonationapp.Adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class TabViewAdapter extends FragmentStateAdapter {

    private final List<Class<?>> fragmentClasses;

    public TabViewAdapter(@NonNull FragmentActivity fragmentActivity, List<Class<?>> fragmentClasses) {
        super(fragmentActivity);
        this.fragmentClasses = fragmentClasses;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            return (Fragment) fragmentClasses.get(position).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate fragment: " + fragmentClasses.get(position), e);
        }
    }

    @Override
    public int getItemCount() {
        return fragmentClasses.size();
    }
}


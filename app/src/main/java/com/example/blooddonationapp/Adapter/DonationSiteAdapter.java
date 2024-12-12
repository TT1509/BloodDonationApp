package com.example.blooddonationapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blooddonationapp.Model.DonationSite;
import com.example.blooddonationapp.R;

import java.util.List;

public class DonationSiteAdapter extends RecyclerView.Adapter<DonationSiteAdapter.SiteViewHolder> {

    private List<DonationSite> siteList;

    public DonationSiteAdapter(List<DonationSite> siteList) {
        this.siteList = siteList;
    }

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_site, parent, false);
        return new SiteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        DonationSite site = siteList.get(position);
        holder.siteName.setText(site.getName());
        holder.siteLocation.setText(site.getLocation());
        holder.siteDate.setText(site.getDateTime().toString());
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }

    static class SiteViewHolder extends RecyclerView.ViewHolder {
        TextView siteName, siteLocation, siteDate;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            siteName = itemView.findViewById(R.id.siteName);
            siteLocation = itemView.findViewById(R.id.siteLocation);
            siteDate = itemView.findViewById(R.id.siteDate);
        }
    }
}


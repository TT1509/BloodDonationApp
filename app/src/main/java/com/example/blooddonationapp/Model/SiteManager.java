package com.example.blooddonationapp.Model;

import java.util.ArrayList;
import java.util.List;

public class SiteManager extends User {
    private List<String> managedSites;

    // Default constructor for Firebase
    public SiteManager() {
        this.managedSites = new ArrayList<>();
    }

    // Parameterized constructor
    public SiteManager(String name, String email, Integer phoneNumber,
                       String role) {
        super(name, email, phoneNumber, role);
        this.managedSites = new ArrayList<>();
    }

    public List<String> getManagedSites() {
        return managedSites;
    }

    public void setManagedSites(List<String> managedSites) {
        this.managedSites = managedSites;
    }

    public void addManagedSite(String siteId) {
        this.managedSites.add(siteId);
    }
}

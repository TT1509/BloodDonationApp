package com.example.blooddonationapp.Model;

import java.util.List;

public class SuperUser extends User {
    private boolean canGenerateReports;
    private List<String> donationSitesList;

    public SuperUser() {
        super();
    }

    public SuperUser(String name, String email, Integer phoneNumber,
                     String role, boolean canGenerateReports, List<String> donationSitesList) {
        super(name, email, phoneNumber, role);
        this.canGenerateReports = canGenerateReports;
        this.donationSitesList = donationSitesList;
    }

    public boolean isCanGenerateReports() {
        return canGenerateReports;
    }

    public void setCanGenerateReports(boolean canGenerateReports) {
        this.canGenerateReports = canGenerateReports;
    }

    public List<String> getDonationSitesList() {
        return donationSitesList;
    }

    public void setDonationSitesList(List<String> donationSitesList) {
        this.donationSitesList = donationSitesList;
    }

}


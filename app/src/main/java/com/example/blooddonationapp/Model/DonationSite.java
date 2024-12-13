package com.example.blooddonationapp.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DonationSite {
    private String managerId;
    private String name;
    private String location;
    private Date dateTime;
    private String contactInfo;
    private List<String> volunteers;
    private List<String> donors;

    // Default constructor for Firebase
    public DonationSite() {
        this.volunteers = new ArrayList<>();
        this.donors = new ArrayList<>();
    }

    // Parameterized constructor
    public DonationSite(String managerId, String name, String location, Date dateTime, String contactInfo) {
        this.managerId = managerId;
        this.name = name;
        this.location = location;
        this.dateTime = dateTime;
        this.contactInfo = contactInfo;
        this.volunteers = new ArrayList<>();
        this.donors = new ArrayList<>();
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public List<String> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<String> volunteers) {
        this.volunteers = volunteers;
    }

    public List<String> getDonors() {
        return donors;
    }

    public void setDonors(List<String> donors) {
        this.donors = donors;
    }
}

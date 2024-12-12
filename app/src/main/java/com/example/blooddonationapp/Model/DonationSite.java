package com.example.blooddonationapp.Model;

import java.util.Date;

public class DonationSite {
    private String managerId;
    private String name;
    private String location;
    private Date dateTime;
    private String contactInfo;

    // Default constructor for Firebase
    public DonationSite() {
    }

    // Parameterized constructor
    public DonationSite(String managerId, String name, String location, Date dateTime, String contactInfo) {
        this.managerId = managerId;
        this.name = name;
        this.location = location;
        this.dateTime = dateTime;
        this.contactInfo = contactInfo;
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
}

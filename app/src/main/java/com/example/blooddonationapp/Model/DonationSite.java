package com.example.blooddonationapp.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DonationSite {
    private String managerId;
    private String name;
    private String address;
    private Date dateTime;
    private String contactInfo;
    private List<String> requiredBloodTypes;
    private double latitude;
    private double longitude;
    private List<String> volunteers;
    private List<String> donors;

    // Default constructor
    public DonationSite() {
        this.volunteers = new ArrayList<>();
        this.donors = new ArrayList<>();
        this.requiredBloodTypes = new ArrayList<>();
    }

    // Parameterized constructor
    public DonationSite(String managerId, String name, String address, Date dateTime, String contactInfo, List<String> requiredBloodTypes, double latitude, double longitude) {
        this.managerId = managerId;
        this.name = name;
        this.address = address;
        this.dateTime = dateTime;
        this.contactInfo = contactInfo;
        this.requiredBloodTypes = requiredBloodTypes;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public List<String> getRequiredBloodTypes() {
        return requiredBloodTypes;
    }

    public void setRequiredBloodTypes(List<String> requiredBloodTypes) {
        this.requiredBloodTypes = requiredBloodTypes;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

package com.example.blooddonationapp.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DonationSite implements Serializable {
    private String managerId;
    private String name;
    private String address;
    private Date date;
    private String startTime;
    private String endTime;
    private String contactInfo;
    private List<String> requiredBloodTypes;
    private double latitude;
    private double longitude;
    private List<String> volunteers;
    private List<String> donors;
    private int defaultBloodVolume;
    private String state;

    // Default constructor
    public DonationSite() {
        this.volunteers = new ArrayList<>();
        this.donors = new ArrayList<>();
        this.requiredBloodTypes = new ArrayList<>();
        this.state = "Ongoing";
    }

    // Parameterized constructor
    public DonationSite(String managerId, String name, String address, Date date, String startTime, String endTime, String contactInfo, List<String> requiredBloodTypes, double latitude, double longitude, int defaultBloodVolume, String state) {
        this.managerId = managerId;
        this.name = name;
        this.address = address;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contactInfo = contactInfo;
        this.requiredBloodTypes = requiredBloodTypes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.volunteers = new ArrayList<>();
        this.donors = new ArrayList<>();
        this.defaultBloodVolume = defaultBloodVolume;
        this.state = state;
    }

    // Getters and Setters
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public int getDefaultBloodVolume() {
        return defaultBloodVolume;
    }

    public void setDefaultBloodVolume(int defaultBloodVolume) {
        this.defaultBloodVolume = defaultBloodVolume;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

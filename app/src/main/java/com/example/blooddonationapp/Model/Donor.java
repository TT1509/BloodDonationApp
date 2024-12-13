package com.example.blooddonationapp.Model;

public class Donor extends User {
    private String bloodType;
    private int donationCount;

    public Donor() {
        super();
        this.donationCount = 0;
    }

    public Donor(String name, String email, Integer phoneNumber, String role, String bloodType, int donationCount) {
        super(name, email, phoneNumber, role);
        this.bloodType = bloodType;
        this.donationCount = donationCount;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        if (bloodType.matches("^(A|B|AB|O)[+-]$")) {
            this.bloodType = bloodType;
        } else {
            throw new IllegalArgumentException("Invalid blood type");
        }
    }

    public int getDonationCount() {
        return donationCount;
    }

    public void setDonationCount(int donationCount) {
        this.donationCount = donationCount;
    }
}

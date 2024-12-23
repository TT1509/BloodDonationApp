package com.example.blooddonationapp.Model;

import java.util.Date;

public class Donor extends User {
    private String bloodType;
    private int donationCount;
    private double height;
    private double weight;

    public Donor() {
        super();
        this.donationCount = 0;
    }

    public Donor(String name, String email, Integer phoneNumber, String role, Date dateOfBirth, String gender, String bloodType, int donationCount, double height, double weight) {
        super(name, email, phoneNumber, role, dateOfBirth, gender);
        this.bloodType = bloodType;
        this.donationCount = donationCount;
        setHeight(height);
        setWeight(weight);
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

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (height <= 0 || height > 300) {
            throw new IllegalArgumentException("Height must be greater than 0 and below 300 cm");
        }
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        if (weight <= 50) {
            throw new IllegalArgumentException("Weight must be above 50 kg");
        }
        this.weight = weight;
    }
}

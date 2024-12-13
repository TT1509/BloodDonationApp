package com.example.blooddonationapp.Model;

import java.util.Date;

public class User {
    private String name;         // User's full name
    private String email;
    private Integer phoneNumber;
    private String role;         // Role in the system ("donor", "site_manager", "admin")

    // Default constructor for Firebase
    public User() {}

    // Parameterized constructor
    public User(String name, String email, Integer phoneNumber,
                String role) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


}

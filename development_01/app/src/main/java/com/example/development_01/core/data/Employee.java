package com.example.development_01.core.data;

import com.google.firebase.database.PropertyName;

public class Employee {
    private String userName;
    private String email;
    private String role;
    private Double rating = 0.0;
    private int numRatings = 0;

    public Employee() {} // Required for Firebase getValue()

    public Employee(String userName, String email, String role) {
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.rating = 0.0;
        this.numRatings = 0;
    }

    @PropertyName("userName")
    public String getName() { return userName; }

    @PropertyName("userName")
    public void setName(String name) { this.userName = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public int getNumRatings() { return numRatings; }
    public void setNumRatings(int numRatings) { this.numRatings = numRatings; }

    public void addRating(double newRating) {
        if (this.rating == null) this.rating = 0.0;
        double totalRating = this.rating * this.numRatings;
        this.numRatings++;
        this.rating = (totalRating + newRating) / this.numRatings;
    }
}
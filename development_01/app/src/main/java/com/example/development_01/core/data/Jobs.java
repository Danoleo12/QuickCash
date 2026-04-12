package com.example.development_01.core.data;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Jobs implements Serializable {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private double pay;
    private String location;
    private ArrayList<String> tags;
    private String companyName;

    // Required Empty Constructor for Firestore
    public Jobs() {}

    // Parameterized Constructor
    public Jobs(String title, String description, double pay, String location,
                ArrayList<String> tags, String companyName) {
        this.title = title;
        this.description = description;
        this.pay = pay;
        this.location = location;
        this.tags = tags;
        this.companyName = companyName;
    }

    public <T> Jobs(String number, String title, String description, int pay, String location, List<String> tags, Object o, String companyName) {
        this.id=number;
        this.title = title;
        this.description = description;
        this.pay = pay;
        this.location = location;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.companyName = companyName;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPay() { return pay; }
    public String getLocation() { return location; }
    public ArrayList<String> getTags() { return tags; }
    public String getCompanyName() { return companyName; }

    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPay(double pay) { this.pay = pay; }
    public void setLocation(String location) { this.location = location; }
    public void setTags(ArrayList<String> tags) { this.tags = tags; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}
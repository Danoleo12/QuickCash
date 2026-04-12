package com.example.development_01.core.core;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;

public class Job {
    private String employerEmail;
    private String id;
    private String title;
    private String description;
    private Double pay;
    private String location;
    private String companyName;
    private ArrayList<String> tags;
    private ArrayList<String> applicants;

    @ServerTimestamp
    private Timestamp timeCreated;

    public Job() {
        // Required for Firestore
    }

    public Job(String title, String companyName, String description, String location, double pay, ArrayList<String> tags, String employerEmail) {
        this.title = title;
        this.companyName = companyName;
        this.description = description;
        this.location = location;
        this.pay = pay;
        this.tags = tags;
        this.applicants = new ArrayList<>();
        this.employerEmail = employerEmail;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCompanyName() { return companyName; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Double getPay() { return pay; }
    public ArrayList<String> getTags() { return tags; }
    public ArrayList<String> getApplicants() { return applicants; }
    public Timestamp getTimeCreated() { return timeCreated; }
    public String getEmployerEmail(){return employerEmail;}
    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setPay(Double pay) { this.pay = pay; }
    public void setTags(ArrayList<String> tags) { this.tags = tags; }
    public void addTag(String tag) {
        if (this.tags == null) this.tags = new ArrayList<>();
        this.tags.add(tag);
    }
    public void setApplicants(ArrayList<String> applicants) { this.applicants = applicants; }
    public void setTimeCreated(Timestamp timeCreated) { this.timeCreated = timeCreated; }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", pay=" + pay +
                ", tags=" + tags.toString() +
                '}';
    }
}

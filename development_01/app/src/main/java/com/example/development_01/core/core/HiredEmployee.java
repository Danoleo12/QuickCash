package com.example.development_01.core.core;

public class HiredEmployee {
    private final String name;
    private final String email;
    private final String jobTitle;
    private final String location;
    private final double jobPay;
    private final String jobId;

    public HiredEmployee(String name, String email, String jobTitle, String location, double jobPay, String jobId) {
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
        this.location = location;
        this.jobPay = jobPay;
        this.jobId = jobId;
    }

    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getJobTitle() { return jobTitle; }
    public String getLocation() { return location; }
    public double getJobPay()   { return jobPay; }
    public String getJobId()    { return jobId; }
}

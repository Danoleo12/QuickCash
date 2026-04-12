package com.example.development_01.core.data;

public class JobApplication {
    private String applicationId;
    private String jobId;
    private String applicantId;
    private String applicantName;
    private String resumeUrl; // Simplified for this task

    public JobApplication() {}

    public JobApplication(String applicationId, String jobId, String applicantId, String applicantName, String resumeUrl) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.resumeUrl = resumeUrl;
    }

    public String getApplicationId() { return applicationId; }
    public String getJobId() { return jobId; }
    public String getApplicantId() { return applicantId; }
    public String getApplicantName() { return applicantName; }
    public String getResumeUrl() { return resumeUrl; }
}

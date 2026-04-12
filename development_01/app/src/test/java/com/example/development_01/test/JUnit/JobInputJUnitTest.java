package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.example.development_01.core.core.Job;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JobInputJUnitTest {
    String jobTitle = "Dev Intern";
    String jobDescription = "desc";
    double jobPay = 20;
    ArrayList<String> tags = new ArrayList<>(
            List.of("full-time", "outdoor", "contract")
    );
    String jobLocation = "1234 University Avenue, Halifax NS, XXX XXX";

    @Test
    public void jobTitleIsNotEmpty() {
        Job job = new Job();
        job.setTitle(jobTitle);
        assertNotNull(job.getTitle());
        assertFalse(job.getTitle().isEmpty());
    }

    @Test
    public void jobDescriptionIsNotEmpty() {
        Job job = new Job();
        job.setDescription(jobDescription);
        assertNotNull(job.getDescription());
        assertFalse(job.getDescription().isEmpty());
    }

    @Test
    public void jobPayIsNotEmpty() {
        Job job = new Job();
        job.setPay(jobPay);
        assertNotEquals(0, job.getPay());
    }

    @Test
    public void jobTagsIsNotEmpty() {
        Job job = new Job();
        job.setTags(tags);
        assertNotNull(job.getTags());
        assertFalse(job.getTags().isEmpty());
    }

    @Test
    public void jobCanHaveMultipleTags() {
        Job job = new Job();
        job.setTags(tags);
        job.addTag("remote");
        job.addTag("part-time");
        assertEquals(5, job.getTags().size());
    }

    @Test
    public void jobLocationIsNotEmpty() {
        Job job = new Job();
        job.setLocation(jobLocation);
        assertNotNull(job.getLocation());
        assertFalse(job.getLocation().isEmpty());
    }
}

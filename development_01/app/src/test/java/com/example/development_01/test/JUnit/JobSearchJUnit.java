package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.development_01.core.data.Jobs;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JUnit tests for Job Search filtering logic (AC1 - AC5).
 * Optimized for CI/CD environments by ensuring null-safety and using standard Java features.
 */
public class JobSearchJUnit {

    private List<Jobs> mockJobList;

    @Before
    public void setUp() {
        mockJobList = new ArrayList<>();

        // Ensure mock data matches the structure expected by Firestore and model
        Jobs job1 = new Jobs("1", "AI Developer", "Working with AI systems", 45,
                "Halifax", Arrays.asList("Full-time", "Indoor", "AI", "Coding"), null, "TechCorp");

        Jobs job2 = new Jobs("2", "Gardener", "Maintaining gardens", 20,
                "Dartmouth", Arrays.asList("Part-time", "Outdoor", "Plants", "Manual"), null, "GreenThumb");

        Jobs job3 = new Jobs("3", "Data Scientist", "Analyzing data", 55,
                "Halifax", Arrays.asList("Full-time", "Indoor", "Data", "Analysis"), null, "DataInc");

        Jobs job4 = new Jobs("4", "Snow Shoveler", "Clearing snow from driveways", 25,
                "Bedford", Arrays.asList("Part-time", "Outdoor", "Winter", "Manual"), null, "WinterFix");

        mockJobList.add(job1);
        mockJobList.add(job2);
        mockJobList.add(job3);
        mockJobList.add(job4);
    }

    @Test
    public void testFilterByCriteria_AC1() {
        List<Jobs> outdoorJobs = mockJobList.stream()
                .filter(j -> j.getTags() != null && j.getTags().contains("Outdoor"))
                .collect(Collectors.toList());
        assertEquals(2, outdoorJobs.size());

        List<Jobs> fullTimeJobs = mockJobList.stream()
                .filter(j -> j.getTags() != null && j.getTags().contains("Full-time"))
                .collect(Collectors.toList());
        assertEquals(2, fullTimeJobs.size());
    }

    @Test
    public void testFilterByLocation_AC2() {
        String searchLocation = "Halifax";
        List<Jobs> filtered = mockJobList.stream()
                .filter(j -> j.getLocation() != null && j.getLocation().equalsIgnoreCase(searchLocation))
                .collect(Collectors.toList());

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(j -> "Halifax".equalsIgnoreCase(j.getLocation())));
    }

    @Test
    public void testSearchByKeyword_AC3() {
        String keyword = "Developer";
        List<Jobs> filtered = mockJobList.stream()
                .filter(j -> j.getTitle() != null && j.getTitle().contains(keyword))
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("AI Developer", filtered.get(0).getTitle());
    }

    @Test
    public void testFilterByPayRange_AC4() {
        int minPay = 22;
        int maxPay = 50;

        List<Jobs> filtered = mockJobList.stream()
                .filter(j -> j.getPay() >= minPay && j.getPay() <= maxPay)
                .collect(Collectors.toList());

        assertEquals(2, filtered.size());
    }

    @Test
    public void testNoJobsFound_AC5() {
        String searchKeyword = "Astronaut";
        List<Jobs> filtered = mockJobList.stream()
                .filter(j -> j.getTitle() != null && j.getTitle().contains(searchKeyword))
                .collect(Collectors.toList());

        assertTrue("Result list should be empty when no criteria match", filtered.isEmpty());
    }
}
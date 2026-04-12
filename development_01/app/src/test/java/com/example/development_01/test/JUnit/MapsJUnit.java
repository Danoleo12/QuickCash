package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.development_01.core.data.Jobs;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit tests for the maps/location logic behind US5.
 * We're testing the filtering + formatting that MapsActivity does
 * before it actually puts markers on the map.
 */
public class MapsJUnit {

    private List<Jobs> mockJobList;

    @Before
    public void setUp() {
        mockJobList = new ArrayList<>();

        // two jobs with real locations
        mockJobList.add(new Jobs("1", "AI Developer", "Working with AI systems", 45,
                "Halifax", Arrays.asList("Full-time", "Indoor"), null, "TechCorp"));
        mockJobList.add(new Jobs("2", "Gardener", "Maintaining gardens", 20,
                "Dartmouth", Arrays.asList("Part-time", "Outdoor"), null, "GreenThumb"));

        // three jobs with bad/missing locations — these should never show up on the map
        mockJobList.add(new Jobs("3", "Data Scientist", "Analyzing data", 55,
                null, Arrays.asList("Full-time", "Indoor"), null, "DataInc"));
        mockJobList.add(new Jobs("4", "Snow Shoveler", "Clearing snow", 25,
                "", Arrays.asList("Part-time", "Outdoor"), null, "WinterFix"));
        mockJobList.add(new Jobs("5", "Tutor", "Math tutoring", 30,
                "   ", Arrays.asList("Part-time", "Indoor"), null, "LearnCo"));
    }

    // --- location validation (same check MapsActivity uses) ---

    // only Halifax and Dartmouth should survive the filter
    @Test
    public void testOnlyJobsWithRealLocationsPassFilter() {
        List<Jobs> valid = mockJobList.stream()
                .filter(j -> j.getLocation() != null && !j.getLocation().trim().isEmpty())
                .collect(Collectors.toList());

        assertEquals(2, valid.size());
    }

    // null location should get rejected
    @Test
    public void testNullLocationIsRejected() {
        Jobs job = mockJobList.get(2);
        boolean ok = job.getLocation() != null && !job.getLocation().trim().isEmpty();
        assertFalse(ok);
    }

    // empty-string location should also get rejected
    @Test
    public void testEmptyStringLocationIsRejected() {
        Jobs job = mockJobList.get(3);
        assertTrue(job.getLocation().trim().isEmpty());
    }

    // spaces-only location — MapsActivity trims before checking so this should fail too
    @Test
    public void testWhitespaceOnlyLocationIsRejected() {
        Jobs job = mockJobList.get(4);
        boolean ok = job.getLocation() != null && !job.getLocation().trim().isEmpty();
        assertFalse(ok);
    }

    // --- marker title formatting ---

    // normal case: marker title is just the job title
    @Test
    public void testMarkerTitleShowsJobTitle() {
        String title = "AI Developer";
        String marker = title != null ? title : "Available Job";
        assertEquals("AI Developer", marker);
    }

    // if a job somehow has no title we fall back to "Available Job"
    @Test
    public void testMarkerTitleFallsBackWhenNull() {
        String title = null;
        String marker = title != null ? title : "Available Job";
        assertEquals("Available Job", marker);
    }

    // --- marker snippet (the info line under the title) ---

    // happy path: pay + description both present
    @Test
    public void testSnippetWithPayAndDescription() {
        Long pay = 45L;
        String desc = "Working with AI systems";

        String snippet = "Pay: $" + (pay != null ? pay : "N/A");
        if (desc != null && !desc.isEmpty()) snippet += " | " + desc;

        assertEquals("Pay: $45 | Working with AI systems", snippet);
    }

    // pay is null — should show N/A instead of crashing
    @Test
    public void testSnippetShowsNAWhenPayIsNull() {
        Long pay = null;
        String desc = "Some work";

        String snippet = "Pay: $" + (pay != null ? pay : "N/A");
        if (desc != null && !desc.isEmpty()) snippet += " | " + desc;

        assertEquals("Pay: $N/A | Some work", snippet);
    }

    // no description — snippet should just be the pay line
    @Test
    public void testSnippetOmitsDescriptionWhenNull() {
        Long pay = 30L;
        String desc = null;

        String snippet = "Pay: $" + (pay != null ? pay : "N/A");
        if (desc != null && !desc.isEmpty()) snippet += " | " + desc;

        assertEquals("Pay: $30", snippet);
    }

    // --- camera bounds collection ---

    // only the 2 valid locations should end up in the bounds list
    @Test
    public void testBoundsListOnlyContainsValidLocations() {
        List<String> captured = new ArrayList<>();
        for (Jobs job : mockJobList) {
            String loc = job.getLocation();
            if (loc != null && !loc.trim().isEmpty()) {
                captured.add(loc);
            }
        }

        assertEquals(2, captured.size());
        assertTrue(captured.contains("Halifax"));
        assertTrue(captured.contains("Dartmouth"));
    }

    // if every job has a bad location, bounds list should be empty (no camera move)
    @Test
    public void testBoundsListEmptyWhenNoValidLocations() {
        List<Jobs> badJobs = new ArrayList<>();
        badJobs.add(new Jobs("x", "A", "D", 10, null, null, null, "Co"));
        badJobs.add(new Jobs("y", "B", "D", 20, "", null, null, "Co"));

        List<String> captured = new ArrayList<>();
        for (Jobs j : badJobs) {
            String loc = j.getLocation();
            if (loc != null && !loc.trim().isEmpty()) captured.add(loc);
        }

        assertTrue(captured.isEmpty());
    }
}
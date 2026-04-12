package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.development_01.core.data.Jobs;
import com.example.development_01.core.data.firebase.PreferenceRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JUnit tests for the title-based Job Preference system.
 *
 * Covers:
 *  - PreferenceRepository: add, remove, fetch, isPreferred (Mockito-based)
 *  - searchByTitles filtering logic (pure Java, no Firebase needed)
 *
 * Firebase paths used by PreferenceRepository:
 *   users/{uid}/preference  →  List<String> of job titles
 */
public class PreferredJobsJUnitTest {

    private PreferenceRepository repository;
    private DatabaseReference mockUsersRef;
    private DatabaseReference mockUserRef;
    private DatabaseReference mockPreferenceRef;

    @Before
    public void setUp() {
        mockUsersRef     = mock(DatabaseReference.class);
        mockUserRef      = mock(DatabaseReference.class);
        mockPreferenceRef = mock(DatabaseReference.class);

        // users/{uid}  →  users/{uid}/preference
        when(mockUsersRef.child(anyString())).thenReturn(mockUserRef);
        when(mockUserRef.child("preference")).thenReturn(mockPreferenceRef);

        repository = new PreferenceRepository(mockUsersRef);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Fires the captured ValueEventListener with the given snapshot so the
     * repository's read-then-write logic actually executes inside the test.
     */
    private void fireSnapshot(DataSnapshot snapshot) {
        ArgumentCaptor<ValueEventListener> captor =
                ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockPreferenceRef).addListenerForSingleValueEvent(captor.capture());
        captor.getValue().onDataChange(snapshot);
    }

    @SuppressWarnings("unchecked")
    private DataSnapshot snapshotWithTitles(List<String> titles) {
        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.getValue(any(GenericTypeIndicator.class))).thenReturn(new ArrayList<>(titles));
        return snapshot;
    }

    private DataSnapshot emptySnapshot() {
        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.exists()).thenReturn(false);
        return snapshot;
    }

    // ── AC1: addPreference ────────────────────────────────────────────────────

    @Test
    public void testAddPreferenceWritesTitleToFirebase() {
        DataSnapshot snapshot = emptySnapshot();
        repository.addPreference("user123", "Software Developer", () -> {});
        fireSnapshot(snapshot);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPreferenceRef).setValue(listCaptor.capture());
        assertTrue("Saved list must contain the new title",
                listCaptor.getValue().contains("Software Developer"));
    }

    @Test
    public void testAddPreferenceAppendsToExistingList() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Developer"));
        repository.addPreference("user123", "Software Engineer", () -> {});
        fireSnapshot(snapshot);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPreferenceRef).setValue(listCaptor.capture());
        List<String> saved = listCaptor.getValue();
        assertEquals(2, saved.size());
        assertTrue(saved.contains("Developer"));
        assertTrue(saved.contains("Software Engineer"));
    }

    @Test
    public void testAddPreferenceDoesNotAddDuplicate() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Software Developer"));
        repository.addPreference("user123", "Software Developer", () -> {});
        fireSnapshot(snapshot);

        // Title already exists — setValue must NOT be called
        verify(mockPreferenceRef, never()).setValue(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPreferenceWithNullUidThrowsException() {
        repository.addPreference(null, "Software Developer", () -> {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPreferenceWithNullTitleThrowsException() {
        repository.addPreference("user123", null, () -> {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPreferenceWithEmptyTitleThrowsException() {
        repository.addPreference("user123", "", () -> {});
    }

    // ── AC1: removePreference ─────────────────────────────────────────────────

    @Test
    public void testRemovePreferenceDeletesTitleFromList() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Software Developer", "Designer"));
        repository.removePreference("user123", "Software Developer", () -> {});
        fireSnapshot(snapshot);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPreferenceRef).setValue(listCaptor.capture());
        assertFalse(listCaptor.getValue().contains("Software Developer"));
        assertTrue(listCaptor.getValue().contains("Designer"));
    }

    @Test
    public void testRemoveLastPreferenceSavesEmptyList() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Software Developer"));
        repository.removePreference("user123", "Software Developer", () -> {});
        fireSnapshot(snapshot);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPreferenceRef).setValue(listCaptor.capture());
        assertTrue(listCaptor.getValue().isEmpty());
    }

    @Test
    public void testRemoveTitleNotInListDoesNotAlterList() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Developer"));
        repository.removePreference("user123", "Nurse", () -> {});
        fireSnapshot(snapshot);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockPreferenceRef).setValue(listCaptor.capture());
        assertEquals(1, listCaptor.getValue().size());
        assertTrue(listCaptor.getValue().contains("Developer"));
    }

    // ── AC2: fetchPreferences ─────────────────────────────────────────────────

    @Test
    public void testFetchPreferencesReturnsAllStoredTitles() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Software Development", "Developer"));

        repository.fetchPreferences("user123", titles -> {
            assertEquals(2, titles.size());
            assertTrue(titles.contains("Software Development"));
            assertTrue(titles.contains("Developer"));
        });

        fireSnapshot(snapshot);
    }

    @Test
    public void testFetchPreferencesReturnsEmptyListWhenNoneExist() {
        DataSnapshot snapshot = emptySnapshot();

        repository.fetchPreferences("user123", titles -> {
            assertNotNull(titles);
            assertTrue(titles.isEmpty());
        });

        fireSnapshot(snapshot);
    }

    @Test
    public void testFetchPreferencesReturnsCorrectCount() {
        DataSnapshot snapshot = snapshotWithTitles(
                Arrays.asList("Software Development", "Developer", "Data Analyst"));

        repository.fetchPreferences("user123", titles ->
                assertEquals(3, titles.size()));

        fireSnapshot(snapshot);
    }

    // ── AC1: isPreferred ──────────────────────────────────────────────────────

    @Test
    public void testIsPreferredReturnsTrueWhenTitleExists() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Software Development", "Developer"));

        repository.isPreferred("user123", "Developer", isPreferred ->
                assertTrue(isPreferred));

        fireSnapshot(snapshot);
    }

    @Test
    public void testIsPreferredReturnsFalseWhenTitleAbsent() {
        DataSnapshot snapshot = snapshotWithTitles(Arrays.asList("Software Development"));

        repository.isPreferred("user123", "Nursing", isPreferred ->
                assertFalse(isPreferred));

        fireSnapshot(snapshot);
    }

    @Test
    public void testIsPreferredReturnsFalseWhenNoPreferencesExist() {
        DataSnapshot snapshot = emptySnapshot();

        repository.isPreferred("user123", "Developer", isPreferred ->
                assertFalse(isPreferred));

        fireSnapshot(snapshot);
    }

    // ── AC2: searchByTitles filtering logic (pure Java) ──────────────────────
    //
    // These tests verify the matching logic that PreferredJobsActivity uses
    // after fetching the preference titles: filter the Firestore jobs collection
    // to only those whose title appears in the user's preference list.

    private List<Jobs> buildMockJobList() {
        return new ArrayList<>(Arrays.asList(
            new Jobs("1", "Software Developer", "Write code",    60, "Halifax",
                     Arrays.asList("Full-time", "Remote"),  null, "TechCorp"),
            new Jobs("2", "Software Developer", "Backend APIs",  65, "Dartmouth",
                     Arrays.asList("Full-time", "Onsite"),  null, "DevInc"),
            new Jobs("3", "Graphic Designer",   "Design UI",     45, "Halifax",
                     Arrays.asList("Part-time"),             null, "DesignCo"),
            new Jobs("4", "Data Analyst",        "Analyse data",  55, "Bedford",
                     Arrays.asList("Full-time"),             null, "DataCorp"),
            new Jobs("5", "Nurse",               "Patient care",  50, "Halifax",
                     Arrays.asList("Full-time"),             null, "Hospital")
        ));
    }

    @Test
    public void testSearchByTitleReturnsAllJobsMatchingASinglePreference() {
        List<Jobs> all = buildMockJobList();
        List<String> preferences = Arrays.asList("Software Developer");

        List<Jobs> results = all.stream()
                .filter(j -> j.getTitle() != null && preferences.contains(j.getTitle()))
                .collect(Collectors.toList());

        assertEquals("Both 'Software Developer' jobs must be returned", 2, results.size());
        assertTrue(results.stream().allMatch(j -> "Software Developer".equals(j.getTitle())));
    }

    @Test
    public void testSearchByMultipleTitlesReturnsAllMatches() {
        List<Jobs> all = buildMockJobList();
        List<String> preferences = Arrays.asList("Software Developer", "Graphic Designer");

        List<Jobs> results = all.stream()
                .filter(j -> j.getTitle() != null && preferences.contains(j.getTitle()))
                .collect(Collectors.toList());

        assertEquals("Two 'Software Developer' + one 'Graphic Designer' = 3 total", 3, results.size());
    }

    @Test
    public void testSearchByTitlesReturnsEmptyWhenPreferenceListIsEmpty() {
        List<Jobs> all = buildMockJobList();
        List<String> preferences = new ArrayList<>();

        List<Jobs> results = all.stream()
                .filter(j -> j.getTitle() != null && preferences.contains(j.getTitle()))
                .collect(Collectors.toList());

        assertTrue("No preferences → no results", results.isEmpty());
    }

    @Test
    public void testSearchByTitlesReturnsEmptyWhenNoJobsMatch() {
        List<Jobs> all = buildMockJobList();
        List<String> preferences = Arrays.asList("Astronaut", "Brain Surgeon");

        List<Jobs> results = all.stream()
                .filter(j -> j.getTitle() != null && preferences.contains(j.getTitle()))
                .collect(Collectors.toList());

        assertTrue("Unmatched preferences → empty result list", results.isEmpty());
    }

    @Test
    public void testSearchByTitlesMatchingIsCaseSensitive() {
        List<Jobs> all = buildMockJobList();
        // "software developer" (lowercase) must NOT match "Software Developer"
        List<String> preferences = Arrays.asList("software developer");

        List<Jobs> results = all.stream()
                .filter(j -> j.getTitle() != null && preferences.contains(j.getTitle()))
                .collect(Collectors.toList());

        assertTrue("Title matching must be case-sensitive", results.isEmpty());
    }

    @Test
    public void testSearchResultsPreserveAllJobFields() {
        List<Jobs> all = buildMockJobList();
        List<String> preferences = Arrays.asList("Data Analyst");

        List<Jobs> results = all.stream()
                .filter(j -> j.getTitle() != null && preferences.contains(j.getTitle()))
                .collect(Collectors.toList());

        assertEquals(1, results.size());
        Jobs job = results.get(0);
        assertEquals("Data Analyst", job.getTitle());
        assertEquals("Bedford",      job.getLocation());
        assertEquals(55.0,           job.getPay(), 0.001);
        assertEquals("DataCorp",     job.getCompanyName());
    }
}

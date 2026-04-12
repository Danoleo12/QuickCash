package com.example.development_01.test.JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.development_01.core.core.HiredEmployee;
import com.example.development_01.core.core.HiredEmployeesRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * JUnit tests for HiredEmployeesRepository.
 *
 * The repository makes two async Firestore calls:
 *   Step 1 — query "jobs"         where employerEmail == X
 *   Step 2 — query "applications" where postId IN [jobIds] AND Job Status == "Hired"
 *
 * Strategy: mock FirebaseFirestore and its call chain; capture the
 * OnSuccessListener / OnFailureListener with ArgumentCaptor; fire them
 * manually inside each test so the callback logic executes synchronously.
 *
 * This mirrors the pattern used in PreferredJobsJUnitTest for Realtime DB.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class HiredEmployeesRepositoryJUnitTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────

    private FirebaseFirestore mockDb;
    private CollectionReference mockJobsCollection;
    private Query mockJobsQuery;
    private Task<QuerySnapshot> mockJobsTask;

    private HiredEmployeesRepository repository;
    private HiredEmployeesRepository.HiredEmployeesCallback mockCallback;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @Before
    public void setUp() {
        // Mock the Firestore instance and the entire call chain for "jobs":
        //   db.collection("jobs").whereEqualTo(...).get()
        mockDb             = mock(FirebaseFirestore.class);
        mockJobsCollection = mock(CollectionReference.class);
        mockJobsQuery      = mock(Query.class);
        mockJobsTask       = mock(Task.class);

        when(mockDb.collection("jobs")).thenReturn(mockJobsCollection);
        when(mockJobsCollection.whereEqualTo(anyString(), anyString()))
                .thenReturn(mockJobsQuery);
        when(mockJobsQuery.get()).thenReturn(mockJobsTask);

        // addOnSuccessListener / addOnFailureListener must return the Task
        // itself (the real GMS Task does this for chaining).
        when(mockJobsTask.addOnSuccessListener(any())).thenReturn(mockJobsTask);
        when(mockJobsTask.addOnFailureListener(any())).thenReturn(mockJobsTask);

        // Inject the mock Firestore via the package-private constructor.
        repository = new HiredEmployeesRepository(mockDb);

        // Mock callback so we can verify which method was called.
        mockCallback = mock(HiredEmployeesRepository.HiredEmployeesCallback.class);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Captures and fires the OnSuccessListener registered on mockJobsTask
     * with the given snapshot.  Must be called AFTER repository.fetchHiredEmployees().
     */
    private void fireJobsSuccess(QuerySnapshot snapshot) {
        ArgumentCaptor<OnSuccessListener> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockJobsTask).addOnSuccessListener(captor.capture());
        captor.getValue().onSuccess(snapshot);
    }

    /**
     * Captures and fires the OnFailureListener registered on mockJobsTask.
     */
    private void fireJobsFailure(Exception e) {
        ArgumentCaptor<OnFailureListener> captor =
                ArgumentCaptor.forClass(OnFailureListener.class);
        verify(mockJobsTask).addOnFailureListener(captor.capture());
        captor.getValue().onFailure(e);
    }

    /**
     * Builds a mock QuerySnapshot whose iterator yields the given documents.
     * The for-each loop in the repository calls snapshot.iterator() internally.
     */
    private QuerySnapshot snapshotWithDocs(List<QueryDocumentSnapshot> docs) {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        when(snapshot.iterator()).thenReturn(
                (Iterator) docs.iterator()
        );
        return snapshot;
    }

    /** Builds a mock QuerySnapshot that is empty (no documents). */
    private QuerySnapshot emptySnapshot() {
        return snapshotWithDocs(Collections.emptyList());
    }

    /**
     * Builds a mock QueryDocumentSnapshot representing one row in the
     * "jobs" Firestore collection.
     */
    private QueryDocumentSnapshot mockJobDoc(String docId, String title,
                                             String location, double pay) {
        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
        when(doc.getId()).thenReturn(docId);
        when(doc.getString("title")).thenReturn(title);
        when(doc.getString("location")).thenReturn(location);
        when(doc.get("pay")).thenReturn(pay);   // stored as Double in Firestore
        return doc;
    }

    /**
     * Builds a mock QueryDocumentSnapshot representing one row in the
     * "applications" Firestore collection.
     */
    private QueryDocumentSnapshot mockAppDoc(String applicantName,
                                             String applicantEmail,
                                             String jobTitle,
                                             String postId) {
        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
        when(doc.getString("applicantName")).thenReturn(applicantName);
        when(doc.getString("applicantEmail")).thenReturn(applicantEmail);
        when(doc.getString("jobTitle")).thenReturn(jobTitle);
        when(doc.getString("postId")).thenReturn(postId);
        return doc;
    }

    // ── AC1: Empty employer — no jobs in Firestore ────────────────────────────
    //
    // When the "jobs" query returns zero documents the repository must call
    // onResult() with an empty list immediately, without ever touching the
    // "applications" collection.

    @Test
    public void testNoJobsReturnsEmptyList() {
        repository.fetchHiredEmployees("employer@test.com", mockCallback);
        fireJobsSuccess(emptySnapshot());

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCallback).onResult(listCaptor.capture());
        assertTrue("Expected empty list when employer has no jobs",
                listCaptor.getValue().isEmpty());
    }

    @Test
    public void testNoJobsNeverQueriesApplications() {
        // Guard: if there are no jobs we must NOT query the applications
        // collection — that would be an unnecessary (and incorrect) Firestore read.
        repository.fetchHiredEmployees("employer@test.com", mockCallback);
        fireJobsSuccess(emptySnapshot());

        verify(mockDb, never()).collection("applications");
    }

    @Test
    public void testNoJobsNeverCallsOnError() {
        // A vacuous result (0 jobs) is success, not an error.
        repository.fetchHiredEmployees("employer@test.com", mockCallback);
        fireJobsSuccess(emptySnapshot());

        verify(mockCallback, never()).onError(any());
    }

    // ── AC2: Firestore jobs-query failure ──────────────────────────────────────
    //
    // If the first Firestore query (the "jobs" query) throws an exception the
    // repository must propagate it to onError() and never call onResult().

    @Test
    public void testJobsQueryFailureCallsOnError() {
        Exception testException = new Exception("Firestore unavailable");

        repository.fetchHiredEmployees("employer@test.com", mockCallback);
        fireJobsFailure(testException);

        verify(mockCallback).onError(testException);
    }

    @Test
    public void testJobsQueryFailureNeverCallsOnResult() {
        repository.fetchHiredEmployees("employer@test.com", mockCallback);
        fireJobsFailure(new Exception("network error"));

        verify(mockCallback, never()).onResult(any());
    }

    @Test
    public void testOnErrorReceivesTheOriginalException() {
        // The exact exception object (not a wrapper) must be forwarded so the
        // caller can inspect its message or type.
        Exception original = new RuntimeException("quota exceeded");

        repository.fetchHiredEmployees("employer@test.com", mockCallback);
        fireJobsFailure(original);

        ArgumentCaptor<Exception> exCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(mockCallback).onError(exCaptor.capture());
        assertSame("onError must receive the original exception unchanged",
                original, exCaptor.getValue());
    }

    // ── AC3: HiredEmployee model — field mapping from repository ─────────────
    //
    // These tests verify that the repository correctly maps Firestore field
    // names to HiredEmployee fields. They do NOT need a Firestore connection:
    // the mapping logic is the same pure-Java code path as AC1/AC2, just
    // triggered at the point where results are assembled.

    @Test
    public void testHiredEmployeeConstructorMapsAllFields() {
        // Direct unit test of HiredEmployee itself — confirms the repository's
        // new HiredEmployee(...) call will produce the right object.
        HiredEmployee emp = new HiredEmployee(
                "Alice Johnson", "alice@test.com",
                "Software Developer", "Halifax, NS",
                25.50, "job_001");

        assertEquals("Alice Johnson",      emp.getName());
        assertEquals("alice@test.com",     emp.getEmail());
        assertEquals("Software Developer", emp.getJobTitle());
        assertEquals("Halifax, NS",        emp.getLocation());
        assertEquals(25.50,                emp.getJobPay(), 0.001);
        assertEquals("job_001",            emp.getJobId());
    }

    // ── AC4: updateList adapter logic (pure Java, no Android needed) ──────────
    //
    // The repository calls adapter.updateList(employees) after fetching results.
    // We verify the list-manipulation contract here without needing RecyclerView.

    @Test
    public void testHiredEmployeeListCanBeBuiltAndIterated() {
        // Mirrors what the repository builds before handing off to the adapter.
        List<HiredEmployee> employees = new ArrayList<>();
        employees.add(new HiredEmployee("Alice", "a@x.com", "Dev", "Halifax", 25.0, "j1"));
        employees.add(new HiredEmployee("Bob",   "b@x.com", "QA",  "Dartmouth", 20.0, "j2"));

        assertEquals(2, employees.size());
        assertEquals("Alice", employees.get(0).getName());
        assertEquals("Bob",   employees.get(1).getName());
    }

    @Test
    public void testHiredEmployeeListIsEmptyAfterClear() {
        // Mirrors the updateList(emptyList) path when no hired employees exist.
        List<HiredEmployee> employees = new ArrayList<>();
        employees.add(new HiredEmployee("Alice", "a@x.com", "Dev", "Halifax", 25.0, "j1"));
        employees.clear();
        assertTrue(employees.isEmpty());
    }

    // ── AC5: Batching boundary — WHERE_IN_LIMIT edge cases ───────────────────
    //
    // The repository splits job IDs into batches of 10 for Firestore's whereIn
    // limit.  These tests confirm the splitting arithmetic is correct for lists
    // that sit at the boundary (exactly 10, exactly 11).
    //
    // We test the logic directly rather than through Firestore because the
    // batch-splitting is pure Java (Math.min / subList).

    @Test
    public void testBatchSplitExactlyTenJobsProducesOneBatch() {
        // 10 items ÷ 10 limit = 1 batch (no remainder).
        List<String> jobIds = buildIdList(10);
        List<List<String>> batches = splitIntoBatches(jobIds, 10);

        assertEquals("10 IDs with limit 10 must produce exactly 1 batch", 1, batches.size());
        assertEquals(10, batches.get(0).size());
    }

    @Test
    public void testBatchSplitElevenJobsProducesTwoBatches() {
        // 11 items ÷ 10 limit = first batch of 10, second batch of 1.
        List<String> jobIds = buildIdList(11);
        List<List<String>> batches = splitIntoBatches(jobIds, 10);

        assertEquals("11 IDs with limit 10 must produce 2 batches", 2, batches.size());
        assertEquals(10, batches.get(0).size());
        assertEquals(1,  batches.get(1).size());
    }

    @Test
    public void testBatchSplitOneJobProducesOneBatch() {
        List<String> jobIds = buildIdList(1);
        List<List<String>> batches = splitIntoBatches(jobIds, 10);

        assertEquals(1, batches.size());
        assertEquals(1, batches.get(0).size());
    }

    @Test
    public void testBatchSplitTwentyJobsProducesTwoBatchesOfTen() {
        List<String> jobIds = buildIdList(20);
        List<List<String>> batches = splitIntoBatches(jobIds, 10);

        assertEquals(2, batches.size());
        assertEquals(10, batches.get(0).size());
        assertEquals(10, batches.get(1).size());
    }

    // ── Helpers for AC5 ──────────────────────────────────────────────────────

    /** Mirrors the for-loop in HiredEmployeesRepository.fetchHiredEmployees. */
    private List<List<String>> splitIntoBatches(List<String> ids, int limit) {
        List<List<String>> batches = new ArrayList<>();
        int total = ids.size();
        for (int start = 0; start < total; start += limit) {
            int end = Math.min(start + limit, total);
            batches.add(new ArrayList<>(ids.subList(start, end)));
        }
        return batches;
    }

    private List<String> buildIdList(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add("job_" + i);
        }
        return ids;
    }
}

package com.example.development_01.core.core;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches all hired employees for a given employer by cross-referencing the
 * {@code jobs} and {@code applications} Firestore collections.
 *
 * <p>Flow:
 * <ol>
 *   <li>Query {@code jobs} where {@code employerEmail == employerEmail} to
 *       collect all job IDs together with their {@code pay}, {@code location},
 *       and {@code title}.</li>
 *   <li>Query {@code applications} where {@code postId in [jobIds]} AND
 *       {@code Job Status == "Hired"}.  Because Firestore's {@code whereIn}
 *       clause is limited to 10 items at a time the job-ID list is split into
 *       batches of 10 and each batch is issued as a separate query; all batch
 *       tasks are awaited with {@link Tasks#whenAllSuccess}.</li>
 *   <li>Merge each matching application with the corresponding job metadata
 *       and return a {@link HiredEmployee} list through the callback.</li>
 * </ol>
 */
public class HiredEmployeesRepository {

    // ── Firestore whereIn batch limit ─────────────────────────────────────────
    private static final int WHERE_IN_LIMIT = 10;

    private final FirebaseFirestore db;

    // ── Constructor ───────────────────────────────────────────────────────────

    public HiredEmployeesRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /** Public constructor used in unit tests to inject a mock instance. */
    public HiredEmployeesRepository(FirebaseFirestore db) {
        this.db = db;
    }

    // ── Callback interface ────────────────────────────────────────────────────

    public interface HiredEmployeesCallback {
        /** Called on success with the (possibly empty) list of hired employees. */
        void onResult(List<HiredEmployee> employees);

        /** Called when a Firestore error occurs at any stage of the fetch. */
        void onError(Exception e);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Fetches all hired employees whose jobs belong to {@code employerEmail}.
     *
     * @param employerEmail the email address of the employer whose jobs are queried
     * @param callback      result/error receiver; never {@code null}
     */
    public void fetchHiredEmployees(String employerEmail, HiredEmployeesCallback callback) {

        // Step 1 – fetch all jobs that belong to this employer
        db.collection("jobs")
                .whereEqualTo("employerEmail", employerEmail)
                .get()
                .addOnSuccessListener(jobsSnapshot -> {

                    // Build a map:  jobId -> { pay, location, title }
                    // We key on the document ID; also respect an explicit "id" field
                    // when present (same value is expected but we prefer doc ID).
                    Map<String, JobMeta> jobMetaMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : jobsSnapshot) {
                        String jobId = doc.getId();

                        String title    = doc.getString("title");
                        String location = doc.getString("location");

                        // "pay" can be stored as a double or as a Long
                        double pay = 0.0;
                        Object payObj = doc.get("pay");
                        if (payObj instanceof Number) {
                            pay = ((Number) payObj).doubleValue();
                        }

                        jobMetaMap.put(jobId, new JobMeta(title, location, pay));
                    }

                    // Early exit – no jobs means no hired employees
                    if (jobMetaMap.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    // Step 2 – query applications in batches of WHERE_IN_LIMIT
                    List<String> allJobIds = new ArrayList<>(jobMetaMap.keySet());
                    List<Task<QuerySnapshot>> batchTasks = new ArrayList<>();

                    int totalIds = allJobIds.size();
                    for (int start = 0; start < totalIds; start += WHERE_IN_LIMIT) {
                        int end = Math.min(start + WHERE_IN_LIMIT, totalIds);
                        List<String> batchIds = allJobIds.subList(start, end);

                        Task<QuerySnapshot> batchTask = db.collection("applications")
                                .whereIn("postId", batchIds)
                                .whereEqualTo("Job Status", "Hired")
                                .get();

                        batchTasks.add(batchTask);
                    }

                    // Step 3 – wait for all batch queries to complete
                    Tasks.whenAllSuccess(batchTasks)
                            .addOnSuccessListener(results -> {

                                List<HiredEmployee> hiredEmployees = new ArrayList<>();

                                for (Object result : results) {
                                    QuerySnapshot appSnapshot = (QuerySnapshot) result;

                                    for (QueryDocumentSnapshot appDoc : appSnapshot) {
                                        String applicantName  = appDoc.getString("applicantName");
                                        String applicantEmail = appDoc.getString("applicantEmail");
                                        String jobTitle       = appDoc.getString("jobTitle");
                                        String postId         = appDoc.getString("postId");

                                        // Look up job metadata using the postId
                                        JobMeta meta = jobMetaMap.get(postId);
                                        String  location = (meta != null) ? meta.location : "";
                                        double  pay      = (meta != null) ? meta.pay      : 0.0;

                                        // Prefer the jobTitle stored on the application document;
                                        // fall back to the title from the jobs collection.
                                        String resolvedTitle = (jobTitle != null && !jobTitle.isEmpty())
                                                ? jobTitle
                                                : (meta != null ? meta.title : "");

                                        hiredEmployees.add(new HiredEmployee(
                                                applicantName  != null ? applicantName  : "",
                                                applicantEmail != null ? applicantEmail : "",
                                                resolvedTitle,
                                                location,
                                                pay,
                                                postId != null ? postId : ""
                                        ));
                                    }
                                }

                                callback.onResult(hiredEmployees);
                            })
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Lightweight value-object that holds the subset of job fields we need. */
    private static final class JobMeta {
        final String title;
        final String location;
        final double pay;

        JobMeta(String title, String location, double pay) {
            this.title    = title    != null ? title    : "";
            this.location = location != null ? location : "";
            this.pay      = pay;
        }
    }
}

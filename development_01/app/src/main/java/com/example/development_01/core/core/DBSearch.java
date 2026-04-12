package com.example.development_01.core.core;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DBSearch {

    private final FirebaseFirestore db;
    private final CollectionReference jobsRef;

    public interface JobSearchCallback {
        void onCallback(List<Job> jobList);
        void onError(Exception e);
    }

    public DBSearch() {
        db = FirebaseFirestore.getInstance();
        jobsRef = db.collection("jobs");
    }

    public void searchByTitle(String queryText, JobSearchCallback callback) {
        if (queryText == null || queryText.isEmpty()) {
            searchJobs("", callback);
            return;
        }
        jobsRef.whereGreaterThanOrEqualTo("title", queryText)
                .whereLessThanOrEqualTo("title", queryText + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        results.add(job);
                    }
                    callback.onCallback(results);
                })
                .addOnFailureListener(callback::onError);
    }

    public void searchJobs(String query, JobSearchCallback callback) {
        jobsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        if (matches(job, query)) {
                            results.add(job);
                        }
                    }
                    callback.onCallback(results);
                })
                .addOnFailureListener(callback::onError);
    }

    private boolean matches(Job job, String query) {
        if (query == null || query.isEmpty()) return true;
        String q = query.toLowerCase();
        return (job.getTitle() != null && job.getTitle().toLowerCase().contains(q)) ||
               (job.getDescription() != null && job.getDescription().toLowerCase().contains(q)) ||
               (job.getLocation() != null && job.getLocation().toLowerCase().contains(q));
    }

    public void searchByLocation(String location, JobSearchCallback callback) {
        jobsRef.whereEqualTo("location", location)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        results.add(job);
                    }
                    callback.onCallback(results);
                })
                .addOnFailureListener(callback::onError);
    }

    public void searchByPayRange(int minPay, int maxPay, JobSearchCallback callback) {
        jobsRef.whereGreaterThanOrEqualTo("pay", minPay)
                .whereLessThanOrEqualTo("pay", maxPay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        results.add(job);
                    }
                    callback.onCallback(results);
                })
                .addOnFailureListener(callback::onError);
    }

    public void searchByTag(String tag, JobSearchCallback callback) {
        jobsRef.whereArrayContains("tags", tag)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        results.add(job);
                    }
                    callback.onCallback(results);
                })
                .addOnFailureListener(callback::onError);
    }

    public void searchByTitles(List<String> titles, JobSearchCallback callback) {
        if (titles == null || titles.isEmpty()) {
            callback.onCallback(new ArrayList<>());
            return;
        }
        jobsRef.whereIn("title", titles)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = doc.toObject(Job.class);
                        job.setId(doc.getId());
                        results.add(job);
                    }
                    callback.onCallback(results);
                })
                .addOnFailureListener(callback::onError);
    }
}

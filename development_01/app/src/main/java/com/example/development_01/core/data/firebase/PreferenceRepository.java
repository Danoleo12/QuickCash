package com.example.development_01.core.data.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing the user's job-title preferences stored at:
 *   users/{uid}/preference  →  List<String> of job titles
 *
 * All methods are asynchronous (Firebase callbacks).
 */
public class PreferenceRepository {

    private final DatabaseReference usersRef;

    public PreferenceRepository(DatabaseReference usersRef) {
        this.usersRef = usersRef;
    }

    // ── Callbacks ─────────────────────────────────────────────────────────────

    public interface PreferencesCallback {
        void onResult(List<String> titles);
    }

    public interface IsPreferredCallback {
        void onResult(boolean isPreferred);
    }

    public interface ActionCallback {
        void onComplete();
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private DatabaseReference preferenceRef(String uid) {
        return usersRef.child(uid).child("preference");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Adds a job title to the user's preference list.
     * Does nothing if the title is already present (no duplicates).
     */
    public void addPreference(String uid, String title, ActionCallback callback) {
        if (uid == null || uid.isEmpty()) throw new IllegalArgumentException("uid must not be null or empty");
        if (title == null)               throw new IllegalArgumentException("title must not be null");
        if (title.isEmpty())             throw new IllegalArgumentException("title must not be empty");

        preferenceRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> current = readList(snapshot);
                if (!current.contains(title)) {
                    current.add(title);
                    preferenceRef(uid).setValue(current);
                }
                if (callback != null) callback.onComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Removes a job title from the user's preference list.
     * Does nothing if the title is not present.
     */
    public void removePreference(String uid, String title, ActionCallback callback) {
        if (uid == null || uid.isEmpty()) throw new IllegalArgumentException("uid must not be null or empty");
        if (title == null)               throw new IllegalArgumentException("title must not be null");

        preferenceRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> current = readList(snapshot);
                current.remove(title);
                preferenceRef(uid).setValue(current);
                if (callback != null) callback.onComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Fetches all stored preference titles for the given user.
     * Returns an empty list if none exist.
     */
    public void fetchPreferences(String uid, PreferencesCallback callback) {
        preferenceRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (callback != null) callback.onResult(readList(snapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onResult(new ArrayList<>());
            }
        });
    }

    /**
     * Checks whether a specific job title is already in the user's preference list.
     */
    public void isPreferred(String uid, String title, IsPreferredCallback callback) {
        preferenceRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> current = readList(snapshot);
                if (callback != null) callback.onResult(current.contains(title));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onResult(false);
            }
        });
    }

    // ── Internal helper ───────────────────────────────────────────────────────

    private List<String> readList(DataSnapshot snapshot) {
        if (!snapshot.exists()) return new ArrayList<>();
        GenericTypeIndicator<List<String>> type = new GenericTypeIndicator<List<String>>() {};
        List<String> data = snapshot.getValue(type);
        return data != null ? new ArrayList<>(data) : new ArrayList<>();
    }
}

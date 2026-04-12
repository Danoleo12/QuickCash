package com.example.development_01.core.data.firebase;

import androidx.annotation.NonNull;

import com.example.development_01.core.data.Employee;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseCRUD implements AuthRepository{
    private FirebaseDatabase database = null;
    private FirebaseAuth auth = null;

    private DatabaseReference userRef = null;
    private DatabaseReference nameRef = null;
    private DatabaseReference emailRef = null;
    private DatabaseReference roleRef = null;

    private String extractedName;
    private String extractedEmail;
    private String extractedRole;

    public FirebaseCRUD(FirebaseDatabase database) {
        this.database = database;
        this.auth = FirebaseAuth.getInstance();
        this.userRef = database.getReference("users");
    }
    public FirebaseCRUD() {
        this.database = FirebaseDatabase.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.userRef = database.getReference("users");
    }

    // Set values to database
    public void setName(String name) {
        this.nameRef.setValue(name);
    }

    public void setEmail(String email) {
        this.emailRef.setValue(email);
    }

    public void setRole(String role) {
        this.roleRef.setValue(role);
    }

    // Listeners to get data from database
    protected void setNameListener() {
        this.nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                extractedName = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    protected void setEmailListener() {
        this.emailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                extractedEmail = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    protected void setRoleListener() {
        this.roleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                extractedRole = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Initialize references
    protected void initializeDatabaseRefs(String uid) {
        DatabaseReference currentUserRef = this.userRef.child(uid);

        this.nameRef = currentUserRef.child("userName");
        this.emailRef = currentUserRef.child("email");
        this.roleRef = currentUserRef.child("role");
    }

    // Initialize listeners
    protected void initializeDatabaseRefListeners() {
        this.setNameListener();
        this.setEmailListener();
        this.setRoleListener();
    }

    // Get extracted values
    public String getExtractedName() {
        return this.extractedName;
    }

    public String getExtractedEmail() {
        return this.extractedEmail;
    }

    public String getExtractedRole() {
        return this.extractedRole;
    }

    /**
     * AC4: Register a new user with the provided email, password and role.
     *
     * @param userName The user's username.
     * @param email    The user's email address.
     * @param password The user's password.
     * @param role     The user's role (either "employer" or "employee").
     */
    public void registerUser(String userName, String email, String password, String role, RegistrationCallback callback) {
        // Firebase Auth checks if email exists
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Get user ID
                        String uid = auth.getCurrentUser().getUid();

                        // Initialize references under users/{uid}
                        initializeDatabaseRefs(uid);

                        // Save user data to database using the Employee object for consistency
                        Employee newUser = new Employee(userName, email, role);
                        this.userRef.child(uid).setValue(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    // Initialize listeners after data is successfully set
                                    initializeDatabaseRefListeners();
                                    callback.onSuccess(uid);
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));

                    } else {
                        // Firebase Auth returns error if email already exists
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        callback.onError(errorMessage);
                    }
                });
    }
    /**
     * Reads users/{uid}/userName once.
     * Used by LoginActivity to display: "Valid Credential: <username>"
     */
    public void fetchUserNameByUid(String uid, UserNameCallback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Missing uid");
            }
            return;
        }

        DatabaseReference ref = this.userRef.child(uid).child("userName");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.getValue(String.class);
                if (callback != null) {
                    callback.onSuccess(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) {
                    callback.onError(error.getMessage());
                }
            }
        });
    }

    public void fetchUserProfile(String uid, UserProfileCallback callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Missing uid");
            return;
        }

        DatabaseReference ref = this.userRef.child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("userName").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);
                Double rating = snapshot.child("rating").getValue(Double.class);
                if (rating == null) rating = 0.0;

                callback.onSuccess(name, role, rating);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }



    public interface UserProfileCallback {
        void onSuccess(String name, String role, Double rating);
        void onError(String error);
    }

    public interface UserNameCallback {
        void onSuccess(String userName);
        void onError(String error);
    }
    // Callback interface for registration
    public interface RegistrationCallback {
        void onSuccess(String userId);
        void onError(String error);
    }
}
package com.example.development_01.core.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class JobAlertService extends Service {

    public static boolean testMode = false;
    public static final int SERVICE_NOTIF_ID = 1;

    private static final String CHANNEL_SERVICE = "job_alert_service";
    private static final String CHANNEL_NOTIFY  = "job_alerts";

    private ListenerRegistration jobListener;
    private boolean isFirstSnapshot = true;

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannels();
        startForeground(SERVICE_NOTIF_ID, buildServiceNotification());
        if (!testMode) {
            startListening();
        }
        return START_STICKY;
    }

    private void startListening() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        jobListener = FirebaseFirestore.getInstance()
                .collection("jobs")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    // Skip the initial load — only react to documents added after
                    // this listener was attached.
                    if (isFirstSnapshot) {
                        isFirstSnapshot = false;
                        return;
                    }

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            checkPreferencesAndNotify(uid, change.getDocument());
                        }
                    }
                });
    }

    private void checkPreferencesAndNotify(String uid, DocumentSnapshot doc) {
        String jobTitle = doc.getString("title");
        if (jobTitle == null) return;
        String lowerTitle = jobTitle.toLowerCase();

        FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("preference")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot prefSnap : snapshot.getChildren()) {
                            String pref = prefSnap.getValue(String.class);
                            if (pref != null && !pref.trim().isEmpty()
                                    && lowerTitle.contains(pref.toLowerCase().trim())) {
                                showJobNotification(doc);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showJobNotification(DocumentSnapshot doc) {
        String jobId = doc.getId();
        String jobTitle = doc.getString("title");
        String company = doc.getString("companyName");
        String description = doc.getString("description");
        String location = doc.getString("location");

        Double payDouble = doc.getDouble("pay");
        String pay = payDouble != null ? String.valueOf(payDouble) : "";

        @SuppressWarnings("unchecked")
        List<String> tagsList = (List<String>) doc.get("tags");
        String tags = (tagsList != null) ? String.join(",", tagsList) : "";

        Intent intent = new Intent(this, PostDescription.class);
        intent.putExtra(PostDescription.EXTRA_JOB_ID, jobId);
        intent.putExtra(PostDescription.EXTRA_JOB_TITLE, jobTitle);
        intent.putExtra(PostDescription.EXTRA_JOB_DESCRIPTION, description);
        intent.putExtra(PostDescription.EXTRA_LOCATION, location);
        intent.putExtra(PostDescription.EXTRA_PAY, pay);
        intent.putExtra(PostDescription.EXTRA_TAGS, tags);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                jobId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String safeTitle = jobTitle != null ? jobTitle : "New Job";
        String safeLocation = location != null ? location : "Location unavailable";
        String safeCompany = company != null ? company : "Unknown company";

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_NOTIFY)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Job Match!")
                .setContentText(safeTitle + " - " + safeLocation)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Job: " + safeTitle + "\nLocation: " + safeLocation + "\nCompany: " + safeCompany))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(0, "View Job Details", pendingIntent)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this).notify(jobId.hashCode(), notification);
    }
    private Notification buildServiceNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_SERVICE)
                .setContentTitle("Job Alerts Active")
                .setContentText("Watching for new job matches...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL_SERVICE, "Job Alert Service",
                    NotificationManager.IMPORTANCE_LOW));
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL_NOTIFY, "Job Alerts",
                    NotificationManager.IMPORTANCE_HIGH));
        }
    }

    @Override
    public void onDestroy() {
        if (jobListener != null) {
            jobListener.remove();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.RatingActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class RatingRobolectricTest {

    private FirebaseFirestore mockFirestore;
    private CollectionReference mockCollection;
    private DocumentReference mockDocument;
    private Task<Void> mockFirestoreTask;
    private MockedStatic<FirebaseFirestore> staticFirestore;

    private FirebaseDatabase mockDatabase;
    private DatabaseReference mockDatabaseRef;
    private MockedStatic<FirebaseDatabase> staticDatabase;

    @Before
    public void setup() {
        Context appContext = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext);
        }

        // Mock Firestore
        mockFirestore = mock(FirebaseFirestore.class);
        mockCollection = mock(CollectionReference.class);
        mockDocument = mock(DocumentReference.class);
        mockFirestoreTask = mock(Task.class);

        when(mockFirestore.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.update(anyString(), any())).thenReturn(mockFirestoreTask);
        when(mockFirestoreTask.addOnSuccessListener(any())).thenReturn(mockFirestoreTask);
        when(mockFirestoreTask.addOnFailureListener(any())).thenReturn(mockFirestoreTask);

        staticFirestore = mockStatic(FirebaseFirestore.class);
        staticFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);

        // Mock Realtime Database
        mockDatabase = mock(FirebaseDatabase.class);
        mockDatabaseRef = mock(DatabaseReference.class);
        when(mockDatabase.getReference(anyString())).thenReturn(mockDatabaseRef);
        when(mockDatabaseRef.child(anyString())).thenReturn(mockDatabaseRef);
        when(mockDatabaseRef.orderByChild(anyString())).thenReturn(mockDatabaseRef);
        when(mockDatabaseRef.equalTo(anyString())).thenReturn(mockDatabaseRef);

        staticDatabase = mockStatic(FirebaseDatabase.class);
        staticDatabase.when(FirebaseDatabase::getInstance).thenReturn(mockDatabase);
    }

    @After
    public void tearDown() {
        if (staticFirestore != null) {
            staticFirestore.close();
        }
        if (staticDatabase != null) {
            staticDatabase.close();
        }
    }

    @Test
    public void testRatingActivityLayoutAndSubmission() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), RatingActivity.class);
        intent.putExtra("APPLICANT_EMAIL", "test@employee.com");

        RatingActivity activity = Robolectric.buildActivity(RatingActivity.class, intent).setup().get();
        RatingBar ratingBar = activity.findViewById(R.id.ratingBar);
        Button submitBtn = activity.findViewById(R.id.btnSubmitRating);

        assertNotNull("RatingBar should exist", ratingBar);
        assertNotNull("Submit button should exist", submitBtn);

        ratingBar.setRating(4.5f);
        submitBtn.performClick();
    }
}

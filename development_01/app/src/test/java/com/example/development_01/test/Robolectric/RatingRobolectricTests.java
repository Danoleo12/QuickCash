package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import com.example.development_01.core.ui.EmployerPayPal;
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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class RatingRobolectricTests {

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
    public void testRatingButtonExistsAndInitialState() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EmployerPayPal.class);
        intent.putExtra("JOB_ID", "testJobId");
        intent.putExtra("APPLICANT_EMAIL", "test@employee.com");

        EmployerPayPal activity = Robolectric.buildActivity(EmployerPayPal.class, intent).setup().get();
        Button rateBtn = activity.findViewById(R.id.btnRateWorker);

        assertNotNull("Rate button should exist", rateBtn);
        assertFalse("Rate button should be disabled initially", rateBtn.isEnabled());
    }

    @Test
    public void testRatingButtonEnabledAfterMarkCompleted() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EmployerPayPal.class);
        intent.putExtra("JOB_ID", "testJobId");
        intent.putExtra("APPLICANT_EMAIL", "test@employee.com");

        EmployerPayPal activity = Robolectric.buildActivity(EmployerPayPal.class, intent).setup().get();
        Button markCompletedBtn = activity.findViewById(R.id.btnMarkCompleted);
        Button rateBtn = activity.findViewById(R.id.btnRateWorker);

        markCompletedBtn.performClick();
        ShadowLooper.idleMainLooper();

        assertTrue("Rate button should be enabled after marking job as completed", rateBtn.isEnabled());
    }

    @Test
    public void testRatingButtonClickNavigatesToRatingActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EmployerPayPal.class);
        intent.putExtra("JOB_ID", "testJobId");
        intent.putExtra("APPLICANT_EMAIL", "test@employee.com");

        EmployerPayPal activity = Robolectric.buildActivity(EmployerPayPal.class, intent).setup().get();
        Button markCompletedBtn = activity.findViewById(R.id.btnMarkCompleted);
        Button rateBtn = activity.findViewById(R.id.btnRateWorker);

        markCompletedBtn.performClick();
        ShadowLooper.idleMainLooper();
        
        rateBtn.performClick();
        ShadowLooper.idleMainLooper();

        Intent nextIntent = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull("Next intent should not be null", nextIntent);
        assertEquals(RatingActivity.class.getName(), nextIntent.getComponent().getClassName());
        assertEquals("test@employee.com", nextIntent.getStringExtra("APPLICANT_EMAIL"));
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
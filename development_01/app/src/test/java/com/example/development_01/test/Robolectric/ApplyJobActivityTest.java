package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.ApplyJobActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ApplyJobActivityTest {

    private ApplyJobActivity activity;

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private Button nextBtn;
    private Button uploadBtn;

    private View pageUserInfo;
    private View pageResume;
    private TextView uploadStatus;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();

        // Initialize Firebase for Robolectric
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setApiKey("fake-api-key")
                    .setProjectId("fake-project-id")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }

        activity = Robolectric.buildActivity(ApplyJobActivity.class)
                .create()
                .start()
                .resume()
                .get();

        nameInput = activity.findViewById(R.id.nameInput);
        emailInput = activity.findViewById(R.id.emailInput);
        phoneInput = activity.findViewById(R.id.phoneInput);
        nextBtn = activity.findViewById(R.id.nextBtn);
        uploadBtn = activity.findViewById(R.id.uploadResumeBtn);

        pageUserInfo = activity.findViewById(R.id.pageUserInfo);
        pageResume = activity.findViewById(R.id.pageResume);
        uploadStatus = activity.findViewById(R.id.uploadStatus);
    }

    // Test 1: user moves from page 1 to page 2
    @Test
    public void nextButtonMovesToResumePage() {

        nameInput.setText("Tester 1");
        emailInput.setText("tester@employee.com");
        phoneInput.setText("1234567890");

        nextBtn.performClick();

        assertEquals(View.GONE, pageUserInfo.getVisibility());
        assertEquals(View.VISIBLE, pageResume.getVisibility());
    }

    // Test 2: empty fields should keep user on page 1
    @Test
    public void nextButtonWithEmptyFieldsDoesNotAdvance() {

        nextBtn.performClick();

        assertEquals(View.VISIBLE, pageUserInfo.getVisibility());
        assertEquals(View.GONE, pageResume.getVisibility());
    }

    // Test 3: simulate resume upload in TEST_MODE
    @Test
    public void uploadButtonSetsSuccessMessageInTestMode() {

        ApplyJobActivity.testMode = true;

        uploadBtn.performClick();

        assertEquals("Resume Uploaded Successfully!", uploadStatus.getText().toString());
    }
}

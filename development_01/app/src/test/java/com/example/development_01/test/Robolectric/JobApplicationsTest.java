package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.JobApplicationsActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JobApplicationsTest {

    private JobApplicationsActivity activity;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();

        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setApiKey("fake-api-key")
                    .setProjectId("fake-project-id")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }

        Intent intent = new Intent();
        intent.putExtra("JOB_ID", "123");
        intent.putExtra("JOB_TITLE", "Software Developer");
        intent.putExtra("JOB_DESCRIPTION", "Test description");
        intent.putExtra("JOB_PAY", 25.0);
        intent.putExtra("JOB_LOCATION", "Halifax");

        activity = Robolectric.buildActivity(JobApplicationsActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();
    }

    @Test
    public void checkDetailsPopulated() {
        TextView tvTitle = activity.findViewById(R.id.tvDetailTitle);
        assertEquals("Software Developer", tvTitle.getText().toString());
    }

    @Test
    public void backButtonFinishesActivity() {
        activity.findViewById(R.id.btnBackApplications).performClick();
        assertEquals(true, activity.isFinishing());
    }
}
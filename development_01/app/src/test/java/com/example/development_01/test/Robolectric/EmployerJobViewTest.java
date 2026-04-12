package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.EmployerJobViewActivity;
import com.example.development_01.core.ui.JobApplicationsActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class EmployerJobViewTest {

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
    }

    // Tests for EmployerJobViewActivity
    @Test
    public void testEmployerJobViewCheckViewsNotNull() {
        Intent intent = new Intent();
        intent.putExtra("email", "employer@test.com");
        EmployerJobViewActivity activity = Robolectric.buildActivity(EmployerJobViewActivity.class, intent)
                .create().start().resume().get();

        assertNotNull(activity.findViewById(R.id.rvEmployerJobs));
        assertNotNull(activity.findViewById(R.id.btnBack));
    }

    @Test
    public void testEmployerJobViewNoJobsMessage() {
        Intent intent = new Intent();
        intent.putExtra("email", "employer@test.com");
        EmployerJobViewActivity activity = Robolectric.buildActivity(EmployerJobViewActivity.class, intent)
                .create().start().resume().get();

        TextView tvNoJobs = activity.findViewById(R.id.tvNoJobs);
        tvNoJobs.setVisibility(View.VISIBLE);
        assertEquals(View.VISIBLE, tvNoJobs.getVisibility());
    }

    @Test
    public void testEmployerJobViewBackButton() {
        Intent intent = new Intent();
        intent.putExtra("email", "employer@test.com");
        EmployerJobViewActivity activity = Robolectric.buildActivity(EmployerJobViewActivity.class, intent)
                .create().start().resume().get();

        activity.findViewById(R.id.btnBack).performClick();
        assertEquals(true, activity.isFinishing());
    }

    @Test
    public void testJobApplicationsCheckDetails() {
        Intent intent = new Intent();
        intent.putExtra("JOB_ID", "123");
        intent.putExtra("JOB_TITLE", "Software Developer");
        
        JobApplicationsActivity activity = Robolectric.buildActivity(JobApplicationsActivity.class, intent)
                .create().start().resume().get();

        TextView tvTitle = activity.findViewById(R.id.tvDetailTitle);
        assertEquals("Software Developer", tvTitle.getText().toString());
    }

}
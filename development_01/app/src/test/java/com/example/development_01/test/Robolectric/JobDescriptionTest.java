package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.JobDescription;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class JobDescriptionTest {

    private JobDescription activity;



    @Before
    public void setup() {
        Context appContext = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext);
        }

        // JobDescription reads getIntent().getStringExtra("email") in initializeJob()
        Intent intent = new Intent(appContext, JobDescription.class);
        intent.putExtra("email", "tester@employer.com");

        activity = Robolectric.buildActivity(JobDescription.class, intent)
                .create()
                .start()
                .resume()
                .get();
    }



    private void fillFormWithValidData() {
        ((com.google.android.material.textfield.TextInputEditText)
                activity.findViewById(R.id.jobTitle)).setText("Software Dev");

        ((com.google.android.material.textfield.TextInputEditText)
                activity.findViewById(R.id.companyTitle)).setText("Acme Corp");

        ((com.google.android.material.textfield.TextInputEditText)
                activity.findViewById(R.id.jobPay)).setText("25.00");

        ((com.google.android.material.textfield.TextInputEditText)
                activity.findViewById(R.id.jobLocation)).setText("Halifax, NS");

        ((com.google.android.material.textfield.TextInputEditText)
                activity.findViewById(R.id.jobTags)).setText("Android, Java");

        ((com.google.android.material.textfield.TextInputEditText)
                activity.findViewById(R.id.jobDescription)).setText("Build and maintain Android applications.");
    }



    @Test
    public void publishButtonExists() {
        Button publishBtn = activity.findViewById(R.id.publishBtn);
        assertNotNull("Publish button should be present in the layout", publishBtn);
    }



    @Test
    public void formValidationPassesWithAllFieldsFilled() {
        boolean result = activity.isFormValid(
                "Software Dev",   // title
                "Acme Corp",      // companyName
                "25.00",          // payValue
                "Halifax, NS",    // location
                "Android, Java",  // tags
                "Build Android apps." // description
        );
        assertTrue("Form should be valid when all fields are filled", result);
    }

    @Test
    public void formValidationFailsWhenTitleIsEmpty() {
        boolean result = activity.isFormValid(
                "",               // title — intentionally blank
                "Acme Corp",
                "25.00",
                "Halifax, NS",
                "Android, Java",
                "Build Android apps."
        );
        assertFalse("Form should be invalid when title is empty", result);
    }

    @Test
    public void formValidationFailsWhenCompanyNameIsEmpty() {
        boolean result = activity.isFormValid(
                "Software Dev",
                "",               // companyName — intentionally blank
                "25.00",
                "Halifax, NS",
                "Android, Java",
                "Build Android apps."
        );
        assertFalse("Form should be invalid when company name is empty", result);
    }

    @Test
    public void formValidationFailsWhenPayIsZero() {
        boolean result = activity.isFormValid(
                "Software Dev",
                "Acme Corp",
                "0.00",           // pay — zero is explicitly rejected by isPayValid()
                "Halifax, NS",
                "Android, Java",
                "Build Android apps."
        );
        assertFalse("Form should be invalid when pay is 0.00", result);
    }

    @Test
    public void formValidationFailsWhenLocationIsEmpty() {
        boolean result = activity.isFormValid(
                "Software Dev",
                "Acme Corp",
                "25.00",
                "",               // location — intentionally blank
                "Android, Java",
                "Build Android apps."
        );
        assertFalse("Form should be invalid when location is empty", result);
    }


    @Test
    public void formValidationFailsWhenTagsAreEmpty() {
        boolean result = activity.isFormValid(
                "Software Dev",
                "Acme Corp",
                "25.00",
                "Halifax, NS",
                "",               // tags — intentionally blank
                "Build Android apps."
        );
        assertFalse("Form should be invalid when tags are empty", result);
    }


    @Test
    public void formValidationFailsWhenDescriptionIsEmpty() {
        boolean result = activity.isFormValid(
                "Software Dev",
                "Acme Corp",
                "25.00",
                "Halifax, NS",
                "Android, Java",
                ""                // description — intentionally blank
        );
        assertFalse("Form should be invalid when description is empty", result);
    }


    @Test
    public void payValidationRejectsZeroEquivalents() {
        assertFalse("Empty string should be invalid pay",  activity.isPayValid(""));
        assertFalse("'0' should be invalid pay",           activity.isPayValid("0"));
        assertFalse("'0.0' should be invalid pay",         activity.isPayValid("0.0"));
        assertFalse("'0.00' should be invalid pay",        activity.isPayValid("0.00"));
    }



    @Test
    public void payValidationAcceptsValidAmount() {
        assertTrue("'25.00' should be a valid pay value", activity.isPayValid("25.00"));
        assertTrue("'0.01' should be a valid pay value",  activity.isPayValid("0.01"));
    }



    @Test
    public void initializeJobReturnsNullWhenFormIsEmpty() {
        // Fields are all empty by default — initializeJob() should bail out
        assertNull("initializeJob() should return null when form fields are empty",
                activity.initializeJob());
    }



    @Test
    public void initializeJobReturnsJobObjectWhenFormIsValid() {
        fillFormWithValidData();

        assertNotNull("initializeJob() should return a Job object when all fields are valid",
                activity.initializeJob());
    }



    @Test
    public void initializeJobMapsFieldValuesCorrectly() {
        fillFormWithValidData();

        com.example.development_01.core.core.Job job = activity.initializeJob();

        assertNotNull(job);
        assertEquals("Job title should match the field input",
                "Software Dev", job.getTitle());
        assertEquals("Company name should match the field input",
                "Acme Corp", job.getCompanyName());
        assertEquals("Pay should match the field input",
                25.00, job.getPay(), 0.001);
        assertEquals("Location should match the field input",
                "Halifax, NS", job.getLocation());
        assertEquals("Employer email should come from the Intent extra",
                "tester@employer.com", job.getEmployerEmail());
        assertTrue("Tags list should contain 'Android'",
                job.getTags().contains("Android"));
        assertTrue("Tags list should contain 'Java'",
                job.getTags().contains("Java"));
    }
}
package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.EmployeeActivity;
import com.example.development_01.core.ui.MapsActivity;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

/**
 * Robolectric tests for US5 — viewing jobs on Google Maps.
 * Covers the UI components in MapsActivity and the navigation
 * path from the employee dashboard into the map screen.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {30})
public class MapsRobolectric {

    private EmployeeActivity employeeActivity;

    @Before
    public void setUp() {
        // need Firebase initialized or the activities crash on startup
        Context appContext = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext);
        }

        employeeActivity = Robolectric.buildActivity(EmployeeActivity.class)
                .create().start().resume().get();
    }

    // --- employee dashboard: the "View Jobs on Map" button ---

    // make sure the button is actually there
    @Test
    public void testViewMapButtonExistsOnDashboard() {
        Button btnViewMap = employeeActivity.findViewById(R.id.btnViewMap);
        assertNotNull(btnViewMap);
    }

    // it should be visible, not hidden
    @Test
    public void testViewMapButtonIsVisible() {
        Button btnViewMap = employeeActivity.findViewById(R.id.btnViewMap);
        assertEquals(View.VISIBLE, btnViewMap.getVisibility());
    }

    // check the label text so we know it's the right button
    @Test
    public void testViewMapButtonText() {
        Button btnViewMap = employeeActivity.findViewById(R.id.btnViewMap);
        assertEquals("View Jobs on Map", btnViewMap.getText().toString());
    }

    // tapping the button should fire an intent to MapsActivity
    @Test
    public void testViewMapButtonNavigatesToMapsActivity() {
        Button btnViewMap = employeeActivity.findViewById(R.id.btnViewMap);
        btnViewMap.performClick();

        Intent intent = Shadows.shadowOf(employeeActivity).getNextStartedActivity();
        assertNotNull(intent);
        assertEquals(MapsActivity.class.getName(), intent.getComponent().getClassName());
    }

    // search button should still be there alongside the map button
    @Test
    public void testSearchButtonStillExistsOnDashboard() {
        Button btnSearch = employeeActivity.findViewById(R.id.btnSearchJobs);
        assertNotNull(btnSearch);
    }

    // --- MapsActivity layout checks ---

    // the map fragment is the whole point of this screen
    @Test
    public void testMapsActivityHasMapFragment() {
        MapsActivity mapsActivity = Robolectric.buildActivity(MapsActivity.class)
                .create().get();
        View mapFragment = mapsActivity.findViewById(R.id.map);
        assertNotNull(mapFragment);
    }

    // back button so the user can return to the dashboard
    @Test
    public void testMapsActivityHasBackButton() {
        MapsActivity mapsActivity = Robolectric.buildActivity(MapsActivity.class)
                .create().get();
        ImageButton btnBack = mapsActivity.findViewById(R.id.btnBack);
        assertNotNull(btnBack);
    }

    // title bar should say "Job Postings"
    @Test
    public void testMapsActivityTitleText() {
        MapsActivity mapsActivity = Robolectric.buildActivity(MapsActivity.class)
                .create().get();
        TextView mapTitle = mapsActivity.findViewById(R.id.mapTitle);
        assertNotNull(mapTitle);
        assertEquals("Job Postings", mapTitle.getText().toString());
    }

    // the floating card that holds the title + back button
    @Test
    public void testMapsActivityHasTopBarCard() {
        MapsActivity mapsActivity = Robolectric.buildActivity(MapsActivity.class)
                .create().get();
        View topBarCard = mapsActivity.findViewById(R.id.topBarCard);
        assertNotNull(topBarCard);
    }

    // clicking back should finish the activity (go back to dashboard)
    @Test
    public void testBackButtonFinishesMapsActivity() {
        MapsActivity mapsActivity = Robolectric.buildActivity(MapsActivity.class)
                .create().get();
        ImageButton btnBack = mapsActivity.findViewById(R.id.btnBack);
        btnBack.performClick();
        assertTrue(mapsActivity.isFinishing());
    }

    // quick sanity check: both nav buttons exist and are visible together
    @Test
    public void testDashboardHasBothNavButtons() {
        Button btnSearch = employeeActivity.findViewById(R.id.btnSearchJobs);
        Button btnViewMap = employeeActivity.findViewById(R.id.btnViewMap);

        assertEquals(View.VISIBLE, btnSearch.getVisibility());
        assertEquals(View.VISIBLE, btnViewMap.getVisibility());
    }
}
package com.example.development_01.test.Robolectric;
import com.example.development_01.R;
import com.example.development_01.core.ui.Favorite_Employee;
import com.google.firebase.FirebaseApp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import com.example.development_01.core.ui.JobDescription;
import com.example.development_01.core.ui.EmployeeActivity;
import com.example.development_01.core.ui.EmployerActivity;
import com.example.development_01.core.ui.LoginActivity;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DashBoard{

    ActivityController<LoginActivity> controller;
    LoginActivity shadow;
    Context context;

    @Before
    public void setup() {
        // Initialize Firebase for Robolectric
        Context appContext = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext);
        }

        controller = Robolectric.buildActivity(LoginActivity.class).setup();
        shadow = controller.get();
        context = appContext;
    }

    @Test
    public void employeeActivityDisplaysEmployeeDashboard(){
        EmployeeActivity activity = Robolectric.buildActivity(EmployeeActivity.class).setup().get();
        TextView roleText = activity.findViewById(com.example.development_01.R.id.employee_role);
        assertEquals("Employee Dashboard", roleText.getText().toString());
    }

    @Test
    public void employerActivityDisplaysEmployerDashboard(){
        EmployerActivity activity = Robolectric.buildActivity(EmployerActivity.class).setup().get();
        TextView roleText = activity.findViewById(com.example.development_01.R.id.employer_role);
        assertEquals("Employer Dashboard", roleText.getText().toString());
    }

    @Test
    public void employeeActivityBackButtonDoesNotLeave(){
        EmployeeActivity activity = Robolectric.buildActivity(EmployeeActivity.class).setup().get();
        activity.getOnBackPressedDispatcher().onBackPressed();
        assertFalse(activity.isFinishing());
    }

    @Test
    public void employerActivityBackButtonDoesNotLeave(){
        EmployerActivity activity = Robolectric.buildActivity(EmployerActivity.class).setup().get();
        activity.getOnBackPressedDispatcher().onBackPressed();
        assertFalse(activity.isFinishing());
    }

    @Test
    public void employeeLogoutReturnsToLogin(){
        EmployeeActivity activity = Robolectric.buildActivity(EmployeeActivity.class).setup().get();
        Button logutBtn = activity.findViewById(com.example.development_01.R.id.employeeLogOut);
        logutBtn.performClick();

        Intent intent = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(LoginActivity.class.getName(), intent.getComponent().getClassName());
        assertTrue(activity.isFinishing());
    }

    @Test
    public void employerLogoutReturnsToLogin(){
        EmployerActivity activity = Robolectric.buildActivity(EmployerActivity.class).setup().get();
        Button logutBtn = activity.findViewById(com.example.development_01.R.id.employerLogOut);
        logutBtn.performClick();

        Intent intent = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(LoginActivity.class.getName(), intent.getComponent().getClassName());
        assertTrue(activity.isFinishing());
    }

    @Test
    public void employerGoesToJobDescription(){
        EmployerActivity activity = Robolectric.buildActivity(EmployerActivity.class).setup().get();
        Button addJobBtn = activity.findViewById(com.example.development_01.R.id.addJob);
        addJobBtn.performClick();

        Intent intent = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(JobDescription.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void employerViewFavoritesEmployees(){
        EmployerActivity activity = Robolectric.buildActivity(EmployerActivity.class).setup().get();
        Button favorite = activity.findViewById(R.id.viewFavorites);
        favorite.performClick();

        Intent intent = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(Favorite_Employee.class.getName(), intent.getComponent().getClassName());
    }

}
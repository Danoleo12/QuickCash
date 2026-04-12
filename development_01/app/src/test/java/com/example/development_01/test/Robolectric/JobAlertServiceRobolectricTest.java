package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.EmployeeActivity;
import com.example.development_01.core.ui.JobAlertService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowNotificationManager;

/**
 * Robolectric tests for the Job Alert notification feature.
 *
 * These tests verify Android-framework behaviour on the JVM without a real device:
 *
 *   1. EmployeeActivity starts JobAlertService when created.
 *   2. JobAlertService displays a persistent foreground notification on start.
 *   3. JobAlertService teardown does not crash when testMode is active.
 *   4. EmployeeActivity stops JobAlertService on logout.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class JobAlertServiceRobolectricTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        // Initialize Firebase with dummy values — required so activities that
        // reference Firebase internally do not throw IllegalStateException.
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setApiKey("fake-api-key")
                    .setProjectId("fake-project-id")
                    .setDatabaseUrl("https://fake-project.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }

        // Enable test mode so JobAlertService skips real Firestore listener setup.
        JobAlertService.testMode = true;
    }

    // ── Test 1 ───────────────────────────────────────────────────────────────

    /**
     * When EmployeeActivity is created, it must fire an intent to start
     * JobAlertService so the Firestore listener runs in the background.
     */
    @Test
    public void employeeActivity_onCreate_startsJobAlertService() {
        Intent launchIntent = new Intent(context, EmployeeActivity.class);
        launchIntent.putExtra("name", "Test Employee");
        launchIntent.putExtra("email", "test@employee.com");

        ActivityController<EmployeeActivity> controller =
                Robolectric.buildActivity(EmployeeActivity.class, launchIntent)
                        .create()
                        .start()
                        .resume();

        ShadowApplication shadowApp = shadowOf((Application) context);
        Intent startedServiceIntent = shadowApp.getNextStartedService();

        assertNotNull("JobAlertService should have been started by EmployeeActivity",
                startedServiceIntent);
        assertEquals(
                JobAlertService.class.getName(),
                startedServiceIntent.getComponent().getClassName()
        );

        controller.pause().stop().destroy();
    }

    // ── Test 2 ───────────────────────────────────────────────────────────────

    /**
     * When JobAlertService.onStartCommand() is called, it must post a
     * foreground notification using the "job_alert_service" channel so Android
     * does not kill the service immediately.
     */
    @Test
    public void jobAlertService_onStartCommand_showsForegroundNotification() {
        ServiceController<JobAlertService> controller =
                Robolectric.buildService(JobAlertService.class)
                        .create()
                        .startCommand(0, 1);

        JobAlertService service = controller.get();

        ShadowNotificationManager shadowNm = shadowOf(
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE));

        // SERVICE_NOTIF_ID = 1 as defined in JobAlertService
        assertNotNull(
                "Foreground notification must be posted when service starts",
                shadowNm.getNotification(JobAlertService.SERVICE_NOTIF_ID)
        );

        controller.destroy();
    }

    // ── Test 3 ───────────────────────────────────────────────────────────────

    /**
     * When JobAlertService is destroyed (e.g. on logout), onDestroy() must
     * not throw a NullPointerException even when the Firestore listener was
     * never attached (testMode = true skips listener setup).
     *
     * This guards against a null-pointer crash if the service is stopped
     * before the first Firestore snapshot arrives.
     */
    @Test
    public void jobAlertService_onDestroy_doesNotCrashWhenListenerIsNull() {
        ServiceController<JobAlertService> controller =
                Robolectric.buildService(JobAlertService.class)
                        .create()
                        .startCommand(0, 1);

        // Should complete without any exception
        controller.destroy();
    }

    // ── Test 4 ───────────────────────────────────────────────────────────────

    /**
     * When the employee taps the Log Out button in EmployeeActivity, a stop-
     * service intent must be sent so the Firestore listener is cleaned up and
     * no further notifications are delivered for a logged-out account.
     */
    @Test
    public void employeeActivity_onLogout_stopsJobAlertService() {
        Intent launchIntent = new Intent(context, EmployeeActivity.class);
        launchIntent.putExtra("name", "Test Employee");
        launchIntent.putExtra("email", "test@employee.com");

        ActivityController<EmployeeActivity> controller =
                Robolectric.buildActivity(EmployeeActivity.class, launchIntent)
                        .create()
                        .start()
                        .resume();

        EmployeeActivity activity = controller.get();

        // Drain the start-service intent recorded in Test 1 so it does not
        // interfere with the stop-service check below.
        ShadowApplication shadowApp = shadowOf((Application) context);
        shadowApp.getNextStartedService();

        // Simulate logout button click
        activity.findViewById(R.id.employeeLogOut).performClick();

        Intent stoppedServiceIntent = shadowApp.getNextStoppedService();

        assertNotNull("JobAlertService should be stopped when the employee logs out",
                stoppedServiceIntent);
        assertEquals(
                JobAlertService.class.getName(),
                stoppedServiceIntent.getComponent().getClassName()
        );
    }
}

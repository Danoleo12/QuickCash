package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.ForgotPasswordActivity;
import com.example.development_01.core.ui.LoginActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.List;

/**
 * Robolectric JVM tests for the "Forgot Password" user story (email-only).
 *
 * Acceptance Criteria:
 * AC1: A "Forgot Password" link is visible on the Login screen.
 * AC2: Clicking the link opens a screen to enter the email.
 * AC3: Entering a valid email triggers Firebase Password Reset email.
 * AC4: Entering an invalid format or non-existent email shows an error.
 *
 * Notes:
 * - Robolectric does NOT auto-initialize FirebaseApp; we initialize it in setUp().
 * - FirebaseAuth network behavior is not deterministic in Robolectric JVM tests without DI/mocking.
 *   Therefore, we assert deterministically on local validation branches (empty/invalid format),
 *   and only sanity-check the "valid email" branch does not show validation errors.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ForgotPassword {

    private static final String VALID_EMAIL = "hulk@email.com";

    private Context context;
    private ActivityController<LoginActivity> loginController;
    private LoginActivity loginActivity;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        // Initialize default FirebaseApp for JVM tests (Robolectric does NOT do this automatically)
        List<FirebaseApp> apps = FirebaseApp.getApps(context);
        if (apps == null || apps.isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setApiKey("fake-api-key")
                    .setDatabaseUrl("https://example.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }

        // Launch LoginActivity for navigation tests
        loginController = Robolectric.buildActivity(LoginActivity.class);
        loginActivity = loginController.get();
        loginController.create().start().resume();
    }

    // ---------- Helpers ----------

    private void flushUi() {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    }

    private String getSnackbarText(View decorView) {
        TextView tv = findTextViewById(decorView, com.google.android.material.R.id.snackbar_text);
        return tv == null ? null : tv.getText().toString();
    }

    private TextView findTextViewById(View view, int id) {
        if (view == null) return null;

        if (view.getId() == id && view instanceof TextView) {
            return (TextView) view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                TextView found = findTextViewById(vg.getChildAt(i), id);
                if (found != null) return found;
            }
        }
        return null;
    }

    private ActivityController<ForgotPasswordActivity> launchForgotPasswordDirectly() {
        ActivityController<ForgotPasswordActivity> controller =
                Robolectric.buildActivity(ForgotPasswordActivity.class);
        controller.create().start().resume();
        return controller;
    }

    // ---------- Tests ----------

    /**
     * AC1 + AC2:
     * Login screen has "Forgot Password" button and clicking it opens ForgotPasswordActivity.
     */
    @Test
    public void forgotPasswordLink_click_startsForgotPasswordActivity() {
        Button forgotBtn = loginActivity.findViewById(R.id.forgotPWBtn);
        assertNotNull("forgotPWBtn is missing from activity_login", forgotBtn);

        forgotBtn.performClick();
        flushUi();

        Intent next = Shadows.shadowOf(loginActivity).getNextStartedActivity();
        assertNotNull("Expected LoginActivity to start ForgotPasswordActivity after clicking Forgot Password", next);

        ComponentName cn = next.getComponent();
        assertNotNull("Started Intent had no ComponentName", cn);

        assertEquals(
                "ForgotPasswordActivity should be launched",
                new ComponentName(loginActivity, ForgotPasswordActivity.class),
                cn
        );
    }

    /**
     * AC2:
     * ForgotPassword screen must be a real screen with the expected UI elements (email + submit).
     */
    @Test
    public void forgotPasswordScreen_hasExpectedUi() {
        ActivityController<ForgotPasswordActivity> fpController = launchForgotPasswordDirectly();
        ForgotPasswordActivity fp = fpController.get();

        TextView title = fp.findViewById(R.id.forgotpwTitle);
        assertNotNull("forgotpwTitle missing from Forgot Password layout (activity_forgotpw)", title);
        assertEquals("Reset Password", title.getText().toString());

        EditText email = fp.findViewById(R.id.emailAddress);
        assertNotNull("emailAddress missing from Forgot Password layout (activity_forgotpw)", email);

        Button submit = fp.findViewById(R.id.submitBtn);
        assertNotNull("submitBtn missing from Forgot Password layout (activity_forgotpw)", submit);
    }

    /**
     * AC4:
     * Empty email should show "Enter detail".
     * This is deterministic (no Firebase call).
     */
    @Test
    public void submit_emptyEmail_showsEnterDetail() {
        ActivityController<ForgotPasswordActivity> fpController = launchForgotPasswordDirectly();
        ForgotPasswordActivity fp = fpController.get();

        EditText email = fp.findViewById(R.id.emailAddress);
        Button submit = fp.findViewById(R.id.submitBtn);

        assertNotNull("emailAddress missing", email);
        assertNotNull("submitBtn missing", submit);

        email.setText("");

        submit.performClick();
        flushUi();

        String msg = getSnackbarText(fp.getWindow().getDecorView());
        assertNotNull("Expected a Snackbar message for empty email", msg);
        assertEquals("Enter detail", msg);
    }

    /**
     * AC4:
     * Invalid email format should show "Invalid email".
     * This is deterministic (no Firebase call).
     */
    @Test
    public void submit_invalidEmail_showsInvalidEmail() {
        ActivityController<ForgotPasswordActivity> fpController = launchForgotPasswordDirectly();
        ForgotPasswordActivity fp = fpController.get();

        EditText email = fp.findViewById(R.id.emailAddress);
        Button submit = fp.findViewById(R.id.submitBtn);

        assertNotNull("emailAddress missing", email);
        assertNotNull("submitBtn missing", submit);

        email.setText("not-an-email");

        submit.performClick();
        flushUi();

        String msg = getSnackbarText(fp.getWindow().getDecorView());
        assertNotNull("Expected a Snackbar message after invalid email submit", msg);
        assertEquals("Invalid email", msg);
    }

    /**
     * AC3:
     * Valid email should proceed past local validation and attempt Firebase reset.
     *
     * In Robolectric JVM tests, FirebaseAuth completion is not deterministic without DI/mocking.
     * So we assert that the *validation* errors are NOT shown immediately.
     */
    @Test
    public void submit_validEmail_doesNotShowValidationErrors() {
        ActivityController<ForgotPasswordActivity> fpController = launchForgotPasswordDirectly();
        ForgotPasswordActivity fp = fpController.get();

        EditText email = fp.findViewById(R.id.emailAddress);
        Button submit = fp.findViewById(R.id.submitBtn);

        assertNotNull("emailAddress missing", email);
        assertNotNull("submitBtn missing", submit);

        email.setText(VALID_EMAIL);

        submit.performClick();
        flushUi();

        String msg = getSnackbarText(fp.getWindow().getDecorView());

        // Acceptable outcomes in JVM test environment:
        // - null (Firebase task didn't complete / no snackbar yet)
        // - "Password reset email sent" (if it completed successfully)
        // - "Email not found" / "Password reset failed" (if Firebase returned error)
        //
        // But it should NOT be local validation errors for a valid email.
        if (msg != null) {
            assertTrue(
                    "Should not show validation error for valid email. Actual: " + msg,
                    !msg.equals("Enter detail") && !msg.equals("Invalid email")
            );
        }
    }
}

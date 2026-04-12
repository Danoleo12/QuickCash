package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.LoginActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.example.development_01.core.validation.EmailValidator;
import com.example.development_01.core.validation.PasswordValidator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.List;

/**
 * Robolectric JVM tests for LoginActivity.
 *
 * Firebase is NOT auto-initialized in Robolectric; we initialize FirebaseApp in setUp()
 * to avoid IllegalStateException when app code calls FirebaseDatabase.getInstance()/FirebaseAuth.getInstance().
 *
 * For login behavior assertions, we use a test-only subclass to simulate auth results deterministically.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class Login {

    private static final String VALID_EMAIL = "hulk@email.com";
    private static final String VALID_PASSWORD = "Test123@";
    private static final String VALID_USERNAME = "hulk";

    ActivityController<TestLoginActivity> controller;
    TestLoginActivity shadow;
    Context context;

    /**
     * Test-only activity: avoids depending on real FirebaseAuth network behavior in Robolectric.
     * Still calls super.onCreate() so your real layout + wiring runs.
     */
    public static class TestLoginActivity extends LoginActivity {
        static boolean persistedLoggedIn = false;

        @Override
        protected void onCreate(android.os.Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Simulate session persistence on reopen (case2)
            if (persistedLoggedIn) {
                TextView status = findViewById(R.id.statusTextView);
                if (status != null) {
                    status.setText("Valid Credential: " + VALID_USERNAME);
                    status.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        protected void handleLoginClick(View view) {
            String email = getEmail() == null ? "" : getEmail().trim();
            String password = getPassword() == null ? "" : getPassword().trim();

            // AC3: Empty fields check (NEW validators, SAME logic/snackbar)
            if (EmailValidator.isEmpty(email) || PasswordValidator.isEmpty(password)) {
                showErrorSnackbar(view, "Enter detail");
                return;
            }

            // Simulate Firebase success/failure (YOUR original shadow logic)
            if (VALID_EMAIL.equals(email) && VALID_PASSWORD.equals(password)) {
                persistedLoggedIn = true;
                TextView status = findViewById(R.id.statusTextView);
                if (status != null) {
                    status.setText("Valid Credential: " + VALID_USERNAME);
                    status.setVisibility(View.VISIBLE);
                }
            } else {
                showErrorSnackbar(view, "Invalid Credential");  // AC2
            }
        }

    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        // Initialize default FirebaseApp for JVM tests (Robolectric does NOT do this automatically)
        List<FirebaseApp> apps = FirebaseApp.getApps(context);
        if (apps == null || apps.isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    // Dummy values are fine for JVM tests; they just need to be non-empty
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setApiKey("fake-api-key")
                    .setDatabaseUrl("https://example.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }

        // Now it's safe to create Activities that reference Firebase in onCreate()
        controller = Robolectric.buildActivity(TestLoginActivity.class);
        shadow = controller.get();
        controller.create().start().resume();
    }

    @After
    public void tearDown() {
        TestLoginActivity.persistedLoggedIn = false;
    }

    // ---------- Helpers ----------

    private void setEmail(String email) {
        EditText emailEt = shadow.findViewById(R.id.emailAddress);
        assertNotNull("emailAddress EditText missing", emailEt);
        emailEt.setText(email);
    }

    private void setPassword(String password) {
        EditText pwEt = shadow.findViewById(R.id.userPassword);
        assertNotNull("userPassword EditText missing", pwEt);
        pwEt.setText(password);
    }

    private void clickLogin() {
        Button loginBtn = shadow.findViewById(R.id.loginBtn);
        assertNotNull("loginBtn missing", loginBtn);
        loginBtn.performClick();

        // Flush UI posted work so Snackbar is actually attached
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    }

    private String getSnackbarText() {
        View root = shadow.getWindow().getDecorView();
        TextView tv = findTextViewById(root, com.google.android.material.R.id.snackbar_text);
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

    // ---------- Tests (user stories) ----------

    @Test
    public void validPasswordInvalidEmail_showsInvalidCredentialSnackbar() {
        setEmail("wrong@email.com");
        setPassword(VALID_PASSWORD);

        clickLogin();

        assertEquals("Invalid Credential", getSnackbarText());
    }

    @Test
    public void validPasswordValidEmail_showsValidCredentialTextView_case1() {
        setEmail(VALID_EMAIL);
        setPassword(VALID_PASSWORD);

        clickLogin();

        TextView status = shadow.findViewById(R.id.statusTextView);
        assertNotNull("statusTextView missing", status);
        assertEquals(View.VISIBLE, status.getVisibility());
        assertEquals("Valid Credential: " + VALID_USERNAME, status.getText().toString());
    }

    @Test
    public void validPasswordEmptyEmail_showsEnterDetailSnackbar() {
        setEmail("");
        setPassword(VALID_PASSWORD);

        clickLogin();

        assertEquals("Enter detail", getSnackbarText());
    }

    @Test
    public void invalidPasswordValidEmail_showsInvalidCredentialSnackbar() {
        setEmail(VALID_EMAIL);
        setPassword("WrongPassword!");

        clickLogin();

        assertEquals("Invalid Credential", getSnackbarText());
    }

    @Test
    public void validPasswordValidEmail_showsValidCredentialTextView_case2() {
        // First login success
        setEmail(VALID_EMAIL);
        setPassword(VALID_PASSWORD);
        clickLogin();

        // Simulate close/reopen
        controller.pause().stop().destroy();

        ActivityController<TestLoginActivity> controller2 =
                Robolectric.buildActivity(TestLoginActivity.class);
        TestLoginActivity shadow2 = controller2.get();
        controller2.create().start().resume();

        TextView status = shadow2.findViewById(R.id.statusTextView);
        assertNotNull("statusTextView missing after recreate", status);
        assertEquals(View.VISIBLE, status.getVisibility());
        assertEquals("Valid Credential: " + VALID_USERNAME, status.getText().toString());

        controller2.pause().stop().destroy();
    }

    @Test
    public void emptyPasswordValidEmail_showsEnterDetailSnackbar() {
        setEmail(VALID_EMAIL);
        setPassword("");

        clickLogin();

        assertEquals("Enter detail", getSnackbarText());
    }

    @Test
    public void emptyPasswordEmptyEmail_showsEnterDetailSnackbar() {
        setEmail("");
        setPassword("");

        clickLogin();

        assertEquals("Enter detail", getSnackbarText());
    }
}

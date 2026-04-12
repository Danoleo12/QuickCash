package com.example.development_01.test.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.development_01.R;
import com.example.development_01.core.ui.EarningsActivity;
import com.example.development_01.core.ui.PaymentSettingsActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class Paymentrobolectrictest {

    //set up too from RobolectricTest.java
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        List<FirebaseApp> apps = FirebaseApp.getApps(context);
        if (apps == null || apps.isEmpty()) {
            FirebaseApp.initializeApp(context, new FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setApiKey("fake-api-key")
                    .setDatabaseUrl("https://example.firebaseio.com")
                    .setProjectId("fake-project-id")
                    .build());
        }
    }

    @Test
    public void allViewsAreNonNull() {
        PaymentSettingsActivity activity = Robolectric
                .buildActivity(PaymentSettingsActivity.class).create().start().resume().get();

        assertNotNull(activity.findViewById(R.id.etAccountName));
        assertNotNull(activity.findViewById(R.id.etBankName));
        assertNotNull(activity.findViewById(R.id.etAccountNumber));
        assertNotNull(activity.findViewById(R.id.etRoutingNumber));
        assertNotNull(activity.findViewById(R.id.btnSavePaymentInfo));
    }

    @Test
    public void emptyFormSaveShowsValidationToast() {
        PaymentSettingsActivity activity = Robolectric
                .buildActivity(PaymentSettingsActivity.class).create().start().resume().get();

        activity.findViewById(R.id.btnSavePaymentInfo).performClick();

        assertNotNull(ShadowToast.getLatestToast());
        assertEquals("All fields are required.", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void completedJobButtonEnabledAndShowsProcessingOnClick() {
        EarningsActivity activity = Robolectric
                .buildActivity(EarningsActivity.class).create().start().resume().get();

        Button btn = activity.findViewById(R.id.btnGetPaidNow);
        assertTrue(btn.isEnabled());

        btn.performClick();

        TextView status = activity.findViewById(R.id.tvPaymentStatus);
        assertEquals("Processing payment...", status.getText().toString());
    }
}

package com.example.development_01.androidTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UIAutomatorTest {

    private static final int LAUNCH_TIMEOUT = 5000;
    final String launcherPackage = "com.example.development_01";
    private UiDevice device;

    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();
        final Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(launcherPackage);
        Assert.assertNotNull(appIntent);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(appIntent);
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);
    }

    @Test
    public void checkIfSignUpPageIsVisible() {
        UiObject userNameBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/userNameBox"));
        assertTrue(userNameBox.exists());

        UiObject emailAddressBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/emailAddressBox"));
        assertTrue(emailAddressBox.exists());

        UiObject passwordBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/userPasswordBox"));
        assertTrue(passwordBox.exists());

        UiObject employerRoleBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/employerRadioBtn"));
        assertTrue(employerRoleBox.exists());

        UiObject employeeRoleBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/employeeRadioBtn"));
        assertTrue(employeeRoleBox.exists());

        UiObject validateBtn = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/validateBtn"));
        assertTrue(validateBtn.exists());
    }

    @Test
    public void checkIfUserCanTypeInField() throws UiObjectNotFoundException {
        UiObject userNameBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/userNameBox"));
        userNameBox.setText("Iron Man");
        assertEquals("Iron Man", userNameBox.getText());

        UiObject emailAddressBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/emailAddressBox"));
        emailAddressBox.setText("IamIronMan@123.com");
        assertEquals("IamIronMan@123.com", emailAddressBox.getText());
    }

    @Test
    public void checkIfUserCanSelectRole() throws UiObjectNotFoundException {
        UiObject employerRoleBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/employerRadioBtn"));
        employerRoleBox.click();
        assertTrue(employerRoleBox.isChecked());

        UiObject employeeRoleBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/employeeRadioBtn"));
        employeeRoleBox.click();
        assertTrue(employeeRoleBox.isChecked());

        // Check if Employer is unchecked
        assertFalse(employerRoleBox.isChecked());
    }

    @Test
    public void checkIfPasswordToggleWorks() throws Exception {
        UiObject passwordBox = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/userPasswordBox"));
        passwordBox.setText("TestPassword123");
        UiObject toggleIcon = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/text_input_end_icon"));

        assertTrue("Password toggle should exist", toggleIcon.exists());
        assertTrue("Password toggle should be clickable", toggleIcon.isClickable());

        toggleIcon.click();
        toggleIcon.click();
    }
    @Test
    public void checkIfSearchActivityOpens() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent();
        intent.setClassName(launcherPackage, launcherPackage + ".core.ui.SearchActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        device.wait(Until.hasObject(By.res(launcherPackage, "searchView")), LAUNCH_TIMEOUT);

        UiObject searchView = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/searchView"));
        assertTrue("SearchView should be visible on SearchActivity", searchView.exists());
    }

    @Test
    public void checkIfBackButtonExistsOnSearchActivity() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent();
        intent.setClassName(launcherPackage, launcherPackage + ".core.ui.SearchActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        device.wait(Until.hasObject(By.res(launcherPackage, "searchView")), LAUNCH_TIMEOUT);

        UiObject backButton = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/imageButton"));
        assertTrue("Back ImageButton should exist on SearchActivity", backButton.exists());
    }
    @Test
    public void checkIfSearchQueryContextLabelExists() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent();
        intent.setClassName(launcherPackage, launcherPackage + ".core.ui.PostSearchActivity");
        intent.putExtra("SEARCH_QUERY", "Developer");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        device.wait(Until.hasObject(By.res(launcherPackage, "tvSearchQueryContext")), LAUNCH_TIMEOUT);

        UiObject tvSearchQueryContext = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/tvSearchQueryContext"));
        assertTrue("tvSearchQueryContext label should exist on PostSearchActivity", tvSearchQueryContext.exists());
    }
    @Test
    public void checkIfEarningsActivityElementsAreVisible() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent();
        intent.setClassName(launcherPackage, launcherPackage + ".core.ui.EarningsActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        device.wait(Until.hasObject(By.res(launcherPackage, "tvJobStatus")), LAUNCH_TIMEOUT);

        UiObject tvJobStatus = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/tvJobStatus"));
        assertTrue("Job status label should be visible on EarningsActivity", tvJobStatus.exists());

        UiObject btnGetPaidNow = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/btnGetPaidNow"));
        assertTrue("Get Paid Now button should be visible on EarningsActivity", btnGetPaidNow.exists());
    }
    @Test
    public void checkIfGetPaidNowButtonIsEnabledAndClickable() throws UiObjectNotFoundException {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent();
        intent.setClassName(launcherPackage, launcherPackage + ".core.ui.EarningsActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        device.wait(Until.hasObject(By.res(launcherPackage, "btnGetPaidNow")), LAUNCH_TIMEOUT);

        UiObject btnGetPaidNow = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/btnGetPaidNow"));
        assertTrue("Get Paid Now button should exist", btnGetPaidNow.exists());
        assertTrue("Get Paid Now button should be enabled", btnGetPaidNow.isEnabled());
        assertTrue("Get Paid Now button should be clickable", btnGetPaidNow.isClickable());
        btnGetPaidNow.click();

        device.wait(Until.hasObject(By.res(launcherPackage, "tvPaymentStatus")), LAUNCH_TIMEOUT);
        UiObject tvPaymentStatus = device.findObject(new UiSelector().resourceId(launcherPackage + ":id/tvPaymentStatus"));
        assertTrue("Payment status label should be visible after clicking Get Paid Now", tvPaymentStatus.exists());
    }

}

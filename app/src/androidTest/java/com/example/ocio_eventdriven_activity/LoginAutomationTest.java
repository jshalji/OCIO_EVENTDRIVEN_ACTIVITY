package com.example.ocio_eventdriven_activity;

import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class LoginAutomationTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    private void waitForOtpScreen() {
        boolean visible = false;

        for (int i = 0; i < 30; i++) {
            SystemClock.sleep(500);

            final boolean[] result = {false};

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                View otp = activityRule.getActivity()
                        .findViewById(R.id.otpLayout);

                result[0] = otp != null
                        && otp.getVisibility() == View.VISIBLE;
            });

            if (result[0]) {
                visible = true;
                break;
            }
        }

        if (!visible) {
            throw new AssertionError("OTP screen did not appear.");
        }
    }

    private void waitForUserList() {
        boolean visible = false;

        for (int i = 0; i < 30; i++) {
            SystemClock.sleep(500);

            final boolean[] result = {false};

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                View users = activityRule.getActivity()
                        .findViewById(R.id.successLayout);

                result[0] = users != null
                        && users.getVisibility() == View.VISIBLE;
            });

            if (result[0]) {
                visible = true;
                break;
            }
        }

        if (!visible) {
            throw new AssertionError("User list screen did not appear.");
        }
    }

    @Test
    public void scenario1_InvalidLogin() {
        onView(withId(R.id.loginLayout))
                .check(matches(isDisplayed()));

        SystemClock.sleep(2000);

        onView(withId(R.id.usernameEditText))
                .perform(replaceText("invalid@example.com"));

        onView(withId(R.id.loginPasswordEditText))
                .perform(replaceText("wrongpassword"), closeSoftKeyboard());

        SystemClock.sleep(2000);

        onView(withId(R.id.loginButton)).perform(click());

        // Wait for the invalid-login API request.
        SystemClock.sleep(5000);

        onView(withId(R.id.loginLayout))
                .check(matches(isDisplayed()));

        onView(withId(R.id.otpLayout))
                .check(matches(not(isDisplayed())));

        SystemClock.sleep(3000);
    }

    @Test
    public void scenario2_CompleteValidLoginOtpAndUserList() {
        onView(withId(R.id.loginLayout))
                .check(matches(isDisplayed()));

        SystemClock.sleep(3000);

        onView(withId(R.id.usernameEditText))
                .perform(replaceText("josh@example.com"));

        onView(withId(R.id.loginPasswordEditText))
                .perform(replaceText("sample123"), closeSoftKeyboard());

        SystemClock.sleep(2000);

        onView(withId(R.id.loginButton)).perform(click());

        waitForOtpScreen();

        onView(withId(R.id.otpLayout))
                .check(matches(isDisplayed()));

        SystemClock.sleep(5000);

        final String[] otpHolder = {null};

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            TextView otpText = activityRule.getActivity()
                    .findViewById(R.id.otpDisplayTextView);

            otpHolder[0] = otpText.getText().toString()
                    .replace("Your OTP:", "")
                    .trim();
        });

        String otp = otpHolder[0];

        if (otp == null || !otp.matches("\\d{6}")) {
            throw new AssertionError("Invalid displayed OTP: " + otp);
        }

        onView(withId(R.id.otpEditText))
                .perform(replaceText(otp), closeSoftKeyboard());

        SystemClock.sleep(2000);

        onView(withId(R.id.verifyOtpButton)).perform(click());

        waitForUserList();

        onView(withId(R.id.successLayout))
                .check(matches(isDisplayed()));

        SystemClock.sleep(8000);
    }
}
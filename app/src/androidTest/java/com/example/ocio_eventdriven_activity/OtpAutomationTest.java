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
public class OtpAutomationTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    private void loginWithValidAccount() {
        onView(withId(R.id.usernameEditText))
                .perform(replaceText("josh@example.com"));

        onView(withId(R.id.loginPasswordEditText))
                .perform(replaceText("sample123"), closeSoftKeyboard());

        onView(withId(R.id.loginButton)).perform(click());

        // Wait for the asynchronous API login request.
        boolean otpScreenVisible = false;

        for (int i = 0; i < 20; i++) {
            SystemClock.sleep(500);

            final boolean[] visible = {false};

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                View otpLayout = activityRule.getActivity()
                        .findViewById(R.id.otpLayout);

                visible[0] = otpLayout != null
                        && otpLayout.getVisibility() == View.VISIBLE;
            });

            if (visible[0]) {
                otpScreenVisible = true;
                break;
            }
        }

        if (!otpScreenVisible) {
            throw new AssertionError(
                    "OTP screen did not appear after login.");
        }

        onView(withId(R.id.otpLayout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void scenario1_InvalidOtp() {
        loginWithValidAccount();

        onView(withId(R.id.otpEditText))
                .perform(replaceText("000000"), closeSoftKeyboard());

        onView(withId(R.id.verifyOtpButton)).perform(click());

        onView(withId(R.id.otpLayout))
                .check(matches(isDisplayed()));

        onView(withId(R.id.successLayout))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void scenario2_ValidOtp() {
        loginWithValidAccount();

        // Read the displayed OTP safely on Android's main thread.
        final String[] otpHolder = {null};

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            TextView otpTextView = activityRule.getActivity()
                    .findViewById(R.id.otpDisplayTextView);

            String displayedText = otpTextView.getText().toString();

            otpHolder[0] = displayedText
                    .replace("Your OTP:", "")
                    .trim();
        });

        String otp = otpHolder[0];

        if (otp == null || !otp.matches("\\d{6}")) {
            throw new AssertionError(
                    "Could not read the six-digit OTP: " + otp);
        }

        onView(withId(R.id.otpEditText))
                .perform(replaceText(otp), closeSoftKeyboard());

        onView(withId(R.id.verifyOtpButton)).perform(click());

        onView(withId(R.id.successLayout))
                .check(matches(isDisplayed()));
    }
}

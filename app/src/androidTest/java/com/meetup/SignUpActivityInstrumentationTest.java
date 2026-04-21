package com.meetup;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SignUpActivityInstrumentationTest {

    @Test
    public void signUpActivity_launches() {
        try (ActivityScenario<SignUpActivity> scenario = ActivityScenario.launch(SignUpActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.signUpRoot)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void signUp_showsError_onPasswordMismatch() {
        try (ActivityScenario<SignUpActivity> scenario = ActivityScenario.launch(SignUpActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.usernameEditText)).perform(ViewActions.typeText("testuser"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.emailEditText)).perform(ViewActions.typeText("test@test.com"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.passwordEditText)).perform(ViewActions.typeText("password123"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.confirmPasswordEditText)).perform(ViewActions.typeText("different123"), ViewActions.closeSoftKeyboard());

            Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click());

            String expectedError = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getString(R.string.error_password_mismatch);

            Espresso.onView(ViewMatchers.withId(R.id.confirmPasswordLayout))
                    .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(expectedError))));
        }
    }

    @Test
    public void signUp_showsError_onShortPassword() {
        try (ActivityScenario<SignUpActivity> scenario = ActivityScenario.launch(SignUpActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.passwordEditText)).perform(ViewActions.typeText("123"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.confirmPasswordEditText)).perform(ViewActions.typeText("123"), ViewActions.closeSoftKeyboard());

            Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click());

            String expectedError = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getString(R.string.error_password_too_short);

            Espresso.onView(ViewMatchers.withId(R.id.passwordLayout))
                    .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(expectedError))));
        }
    }
}
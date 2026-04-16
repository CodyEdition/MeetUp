package com.meetup;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LoginActivityInstrumentationTest {

    @Test
    public void targetPackage_isMeetUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.meetup", appContext.getPackageName());
    }

    @Test
    public void loginActivity_launches() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity ->
                    assertNotNull(activity.findViewById(R.id.loginRoot)));
        }
    }

    @Test
    public void loginActivity_showsError_onEmptyEmail() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            // Type password but leave email empty
            Espresso.onView(ViewMatchers.withId(R.id.passwordEditText))
                    .perform(ViewActions.typeText("password123"), ViewActions.closeSoftKeyboard());
            
            // Click sign in
            Espresso.onView(ViewMatchers.withId(R.id.signInButton)).perform(ViewActions.click());

            // Check if error is displayed on email layout
            // Note: Since TextInputLayout's error isn't easily matched by text, 
            // we check if the error string is what we expect via string resource
            String expectedError = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getString(R.string.error_empty_email);
            
            Espresso.onView(ViewMatchers.withId(R.id.emailLayout))
                    .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(expectedError))));
        }
    }

    @Test
    public void loginActivity_showsError_onEmptyPassword() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            // Type email but leave password empty
            Espresso.onView(ViewMatchers.withId(R.id.emailEditText))
                    .perform(ViewActions.typeText("test@example.com"), ViewActions.closeSoftKeyboard());
            
            // Click sign in
            Espresso.onView(ViewMatchers.withId(R.id.signInButton)).perform(ViewActions.click());

            String expectedError = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext().getString(R.string.error_empty_password);
            
            Espresso.onView(ViewMatchers.withId(R.id.passwordLayout))
                    .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(expectedError))));
        }
    }

    @Test
    public void loginActivity_navigateToSignUp() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            // Click Sign Up button
            Espresso.onView(ViewMatchers.withId(R.id.signUpButton)).perform(ViewActions.click());

            // Check if SignUpActivity's root or a specific view is displayed
            Espresso.onView(ViewMatchers.withId(R.id.signUpRoot))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}
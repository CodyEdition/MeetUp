package com.meetup;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import com.meetup.db.AppDatabase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventCreationUiTest {

    @Before
    public void setup() {
        AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).clearAllTables();
    }

    @Test
    public void testSuccessfulEventCreation() {
        try (ActivityScenario<CreateEventActivity> scenario = ActivityScenario.launch(CreateEventActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.eventTitleEditText)).perform(ViewActions.typeText("New Workshop"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventDescriptionEditText)).perform(ViewActions.typeText("Learning JUnit"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventDateEditText)).perform(ViewActions.replaceText("2026-01-01"));
            Espresso.onView(ViewMatchers.withId(R.id.eventTimeEditText)).perform(ViewActions.replaceText("10:00 AM"));
            Espresso.onView(ViewMatchers.withId(R.id.eventCityDropdown)).perform(ViewActions.typeText("Ottawa"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventLocationEditText)).perform(ViewActions.typeText("Virtual"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventMaxAttendeesEditText)).perform(ViewActions.typeText("50"), ViewActions.closeSoftKeyboard());

            Espresso.onView(ViewMatchers.withId(R.id.createEventButton)).perform(ViewActions.click());
            
            // Verify the user is redirected or success is handled (e.g., activity finishes)
            // If it finishes, the scenario state will change.
        }
    }

    @Test
    public void testCreationFails_WhenTitleIsEmpty() {
        try (ActivityScenario<CreateEventActivity> scenario = ActivityScenario.launch(CreateEventActivity.class)) {
            Espresso.onView(ViewMatchers.withId(R.id.createEventButton)).perform(ViewActions.click());

            String expectedError = "Title cannot be empty";
            Espresso.onView(ViewMatchers.withId(R.id.eventTitleEditText))
                    .check(ViewAssertions.matches(ViewMatchers.hasErrorText(expectedError)));
        }
    }
}
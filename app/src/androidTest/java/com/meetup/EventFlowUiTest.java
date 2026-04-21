package com.meetup;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventFlowUiTest {

    @Before
    public void clearDatabase() {
        // Clear database before each test to ensure a clean state
        AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext())
                .clearAllTables();
    }

    @Test
    public void createEvent_and_verifyInBrowsing() {
        // 1. Start CreateEventActivity
        try (ActivityScenario<CreateEventActivity> scenario = ActivityScenario.launch(CreateEventActivity.class)) {
            
            // Fill out the event creation form
            Espresso.onView(ViewMatchers.withId(R.id.eventTitleEditText)).perform(ViewActions.typeText("UI Test Event"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventDescriptionEditText)).perform(ViewActions.typeText("Testing the flow"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventDateEditText)).perform(ViewActions.replaceText("2026-12-12"));
            Espresso.onView(ViewMatchers.withId(R.id.eventTimeEditText)).perform(ViewActions.replaceText("14:00 PM"));
            
            // Select City (Interacting with AutoCompleteTextView)
            Espresso.onView(ViewMatchers.withId(R.id.eventCityDropdown)).perform(ViewActions.typeText("Ottawa"), ViewActions.closeSoftKeyboard());
            
            Espresso.onView(ViewMatchers.withId(R.id.eventLocationEditText)).perform(ViewActions.typeText("Test Lab"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventMaxAttendeesEditText)).perform(ViewActions.typeText("20"), ViewActions.closeSoftKeyboard());

            // Click create button
            Espresso.onView(ViewMatchers.withId(R.id.createEventButton)).perform(ViewActions.click());
        }

        // 2. Launch EventBrowsingActivity for Ottawa and verify the event appears in the list
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventBrowsingActivity.class);
        intent.putExtra("selected_city", "Ottawa");
        
        try (ActivityScenario<EventBrowsingActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if the newly created event title is displayed in the ListView
            Espresso.onData(anything())
                    .inAdapterView(ViewMatchers.withId(R.id.eventsListView))
                    .atPosition(0)
                    .onChildView(ViewMatchers.withId(R.id.eventTitleItem))
                    .check(ViewAssertions.matches(ViewMatchers.withText("UI Test Event")));
        }
    }

}

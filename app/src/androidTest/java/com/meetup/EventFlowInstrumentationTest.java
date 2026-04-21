package com.meetup;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventFlowInstrumentationTest {

    @Before
    public void setup() {
        // Clear database before tests to have a clean state
        AppDatabase.getInstance(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext())
                .clearAllTables();
    }

    @Test
    public void testEventCreationFlow() {
        try (ActivityScenario<CreateEventActivity> scenario = ActivityScenario.launch(CreateEventActivity.class)) {
            // Fill Event Details
            Espresso.onView(ViewMatchers.withId(R.id.eventTitleEditText)).perform(ViewActions.typeText("Unit Test Party"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventDescriptionEditText)).perform(ViewActions.typeText("Testing the creation flow"), ViewActions.closeSoftKeyboard());
            
            // Note: Date/Time pickers are hard to automate via typeText, 
            // but we can manually set them or skip for this simple flow test if they are pre-validated
            
            Espresso.onView(ViewMatchers.withId(R.id.eventLocationEditText)).perform(ViewActions.typeText("Virtual Room"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventMaxAttendeesEditText)).perform(ViewActions.typeText("50"), ViewActions.closeSoftKeyboard());

            // Click Create
            Espresso.onView(ViewMatchers.withId(R.id.createEventButton)).perform(ViewActions.click());

            // Check if we finished and returned (toast is hard to match, so we check if activity is closing)
            // Or better, check the Database if the event exists
        }
    }

    @Test
    public void testEventDetailsAndRsvpToggle() {
        // 1. Manually insert an event into DB
        AppDatabase db = AppDatabase.getInstance(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext());
        EventEntity event = new EventEntity("Detailed Event", "Desc", "Ottawa", "2026-01-01", "10:00", "Loc", 10, false, "Tag");
        db.eventDao().insert(event);
        int eventId = db.eventDao().getEventsByCity("Ottawa").get(0).id;

        // 2. Launch Details Activity with that ID
        Intent intent = new Intent(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetailsActivity.class);
        intent.putExtra("event_id", eventId);

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if title is displayed
            Espresso.onView(ViewMatchers.withId(R.id.detailsTitleText)).check(ViewAssertions.matches(ViewMatchers.withText("Detailed Event")));

            // Check initial RSVP button text
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton)).check(ViewAssertions.matches(ViewMatchers.withText("RSVP Now")));

            // Click RSVP
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton)).perform(ViewActions.click());

            // Check if button text changed to Cancel
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton)).check(ViewAssertions.matches(ViewMatchers.withText("Cancel RSVP")));
            
            // Check status text
            Espresso.onView(ViewMatchers.withId(R.id.rsvpStatusText)).check(ViewAssertions.matches(ViewMatchers.withText(ViewMatchers.containsString("You have RSVP’d"))));
        }
    }

    @Test
    public void testEventBrowsingList() {
        // Insert events for Ottawa
        AppDatabase db = AppDatabase.getInstance(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext());
        db.eventDao().insert(new EventEntity("Ottawa Meetup", "Desc", "Ottawa", "2026-01-01", "10:00", "Loc", 10, false, ""));

        Intent intent = new Intent(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext(), EventBrowsingActivity.class);
        intent.putExtra("selected_city", "Ottawa");

        try (ActivityScenario<EventBrowsingActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if list contains the event
            Espresso.onView(ViewMatchers.withText("Ottawa Meetup")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}
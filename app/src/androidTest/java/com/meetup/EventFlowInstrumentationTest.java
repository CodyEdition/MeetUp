package com.meetup;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventFlowInstrumentationTest {

    @Before
    public void setup() {
        // Clear database before tests to have a clean state
        AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext())
                .clearAllTables();
    }

    @Test
    public void testEventCreationFlow() {
        try (ActivityScenario<CreateEventActivity> scenario = ActivityScenario.launch(CreateEventActivity.class)) {
            // Fill Event Details
            Espresso.onView(ViewMatchers.withId(R.id.eventTitleEditText)).perform(ViewActions.typeText("Unit Test Party"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventDescriptionEditText)).perform(ViewActions.typeText("Testing the creation flow"), ViewActions.closeSoftKeyboard());
            
            // Simulating mandatory fields - Note: Date/Time might need separate handling if they open dialogs
            Espresso.onView(ViewMatchers.withId(R.id.eventLocationEditText)).perform(ViewActions.typeText("Virtual Room"), ViewActions.closeSoftKeyboard());
            Espresso.onView(ViewMatchers.withId(R.id.eventMaxAttendeesEditText)).perform(ViewActions.typeText("50"), ViewActions.closeSoftKeyboard());

            // Click Create
            Espresso.onView(ViewMatchers.withId(R.id.createEventButton)).perform(ViewActions.click());
        }
    }

    @Test
    public void testEventDetailsAndRsvpToggle() {
        // 1. Manually insert an event into DB
        AppDatabase db = AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        EventEntity event = new EventEntity("Detailed Event", "Desc", "Ottawa", "2026-01-01", "10:00", "Loc", 10, false, "Tag");
        db.eventDao().insert(event);
        
        // Fetch the generated ID
        EventEntity insertedEvent = db.eventDao().getEventsByCity("Ottawa").get(0);
        int eventId = insertedEvent.id;

        // 2. Launch Details Activity with that ID
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetailsActivity.class);
        intent.putExtra("event_id", eventId);

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if title is displayed correctly
            Espresso.onView(ViewMatchers.withId(R.id.detailsTitleText))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Detailed Event")));

            // Click RSVP button (Metin Resource'dan geldiği için ID üzerinden kontrol daha sağlıklı)
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton)).perform(ViewActions.click());

            // Check if status text contains the "RSVP’d" part (Hamcrest Matchers)
            // Metindeki özel karakterlere (’) dikkat edildi.
            Espresso.onView(ViewMatchers.withId(R.id.rsvpStatusText))
                    .check(ViewAssertions.matches(ViewMatchers.withText(Matchers.containsString("RSVP’d"))));
            
            // Re-click to cancel and verify
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton)).perform(ViewActions.click());
            Espresso.onView(ViewMatchers.withId(R.id.rsvpStatusText))
                    .check(ViewAssertions.matches(ViewMatchers.withText(Matchers.containsString("not RSVP’d"))));
        }
    }

    @Test
    public void testEventBrowsingList() {
        // Insert events for Ottawa
        AppDatabase db = AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        db.eventDao().insert(new EventEntity("Ottawa Meetup", "Desc", "Ottawa", "2026-01-01", "10:00", "Loc", 10, false, ""));

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventBrowsingActivity.class);
        intent.putExtra("selected_city", "Ottawa");

        try (ActivityScenario<EventBrowsingActivity> scenario = ActivityScenario.launch(intent)) {
            // Check if list contains the event by searching for the text
            Espresso.onView(ViewMatchers.withText("Ottawa Meetup"))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}
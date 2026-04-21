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
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventRsvpUiTest {

    @Test
    public void testRsvpButtonToggle() {
        // Setup initial un-RSVP'd event
        EventEntity event = new EventEntity("RSVP Party", "D", "City", "2026", "1", "L", 5, false, "T");
        int id = (int) AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).eventDao().insert(event);

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetailsActivity.class);
        intent.putExtra("event_id", id);

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify initial state
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Join")));

            // Action: Click RSVP
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton)).perform(ViewActions.click());

            // Verify updated state
            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Joined")));
        }
    }
}
package com.meetup;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.meetup.db.AppDatabase;
import com.meetup.db.EventEntity;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsUiTest {

    @Test
    public void testEventDetailsAreDisplayed() {
        // Setup event
        EventEntity event = new EventEntity("Detailed Talk", "Long Description", "Toronto", "2026-02-02", "15:00", "Hub", 20, false, "Tech");
        int id = (int) AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).eventDao().insert(event);

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetailsActivity.class);
        intent.putExtra("event_id", id);

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onView(ViewMatchers.withId(R.id.detailsTitleText))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Detailed Talk")));
            
            Espresso.onView(ViewMatchers.withId(R.id.detailsDescriptionText))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Long Description")));
            
            Espresso.onView(ViewMatchers.withId(R.id.detailsLocationText))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Hub")));

            Espresso.onView(ViewMatchers.withId(R.id.rsvpButton)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}
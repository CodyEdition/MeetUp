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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class EventBrowsingUiTest {

    @Before
    public void setup() {
        AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).clearAllTables();
    }

    @Test
    public void testEventsVisibleForSelectedCity() {
        // Seed data
        EventEntity event = new EventEntity("Ottawa Meetup", "Desc", "Ottawa", "2026-01-01", "12:00", "Loc", 10, false, "Tag");
        AppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).eventDao().insert(event);

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventBrowsingActivity.class);
        intent.putExtra("selected_city", "Ottawa");

        try (ActivityScenario<EventBrowsingActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onData(anything())
                    .inAdapterView(ViewMatchers.withId(R.id.eventsListView))
                    .atPosition(0)
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testEmptyState_WhenNoEventsFound() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventBrowsingActivity.class);
        intent.putExtra("selected_city", "NonExistentCity");

        try (ActivityScenario<EventBrowsingActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onView(ViewMatchers.withId(R.id.noEventsTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}
package com.meetup;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.meetup.db.AppDatabase;
import com.meetup.db.EventDao;
import com.meetup.db.EventEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseTest {

    private AppDatabase db;
    private EventDao eventDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Use an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        eventDao = db.eventDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void writeEventAndReadInList() {
        EventEntity event = new EventEntity("Test Event", "Desc", "Ottawa", "2026-05-05", "10:00", "Main St", 10, false, "Tech");
        eventDao.insert(event);
        
        List<EventEntity> events = eventDao.getEventsByCity("Ottawa");
        assertEquals(1, events.size());
        assertEquals("Test Event", events.get(0).title);
    }

    @Test
    public void updateRsvpStatus_isPersisted() {
        EventEntity event = new EventEntity("RSVP Event", "Desc", "Toronto", "2026-06-06", "12:00", "Park", 50, false, "");
        eventDao.insert(event);
        
        // Room auto-generates IDs, so we fetch it first
        List<EventEntity> events = eventDao.getAll();
        int id = events.get(0).id;
        
        // Update RSVP to true
        eventDao.updateRsvpStatus(id, true);
        
        EventEntity updatedEvent = eventDao.getEventById(id);
        assertTrue(updatedEvent.isRsvped);
    }

    @Test
    public void getRsvpedEvents_returnsOnlyJoined() {
        eventDao.insert(new EventEntity("Joined", "D", "Ottawa", "2026-01-01", "10:00", "L", 5, true, ""));
        eventDao.insert(new EventEntity("Not Joined", "D", "Ottawa", "2026-01-01", "10:00", "L", 5, false, ""));

        List<EventEntity> rsvped = eventDao.getRsvpedEvents();
        assertEquals(1, rsvped.size());
        assertEquals("Joined", rsvped.get(0).title);
    }
}
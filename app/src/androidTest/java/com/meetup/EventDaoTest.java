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
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EventDaoTest {
    private AppDatabase db;
    private EventDao eventDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Using an in-memory database so that the information is gone when the process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        eventDao = db.eventDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndReadEvent_Persistence() {
        // Create a new event entity
        EventEntity event = new EventEntity("Workshop", "Desc", "Ottawa", "2026-05-05", "10:00", "Lab", 10, false, "Tech");
        eventDao.insert(event);
        
        // Retrieve events by city
        List<EventEntity> events = eventDao.getEventsByCity("Ottawa");
        
        // Verify data persistence
        assertFalse(events.isEmpty());
        assertEquals("Workshop", events.get(0).title);
    }

    @Test
    public void updateRsvpStatus_UpdatesDatabase() {
        EventEntity event = new EventEntity("RSVP Test", "Desc", "Toronto", "2026-06-06", "12:00", "Hotel", 50, false, "Networking");
        long id = eventDao.insert(event);
        
        // Update RSVP status to true
        eventDao.updateRsvpStatus((int)id, true);
        
        // Verify the status changed in the DB
        EventEntity updatedEvent = eventDao.getEventById((int)id);
        assertTrue("RSVP status should be true", updatedEvent.isRsvped);
    }

    @Test
    public void filterByCity_ReturnsCorrectSubset() {
        // Insert events in different cities
        eventDao.insert(new EventEntity("Event A", "D", "Ottawa", "2026", "1", "L", 5, false, "T"));
        eventDao.insert(new EventEntity("Event B", "D", "Toronto", "2026", "1", "L", 5, false, "T"));

        // Browse events for Ottawa only
        List<List<EventEntity>> results = new java.util.ArrayList<>();
        results.add(eventDao.getEventsByCity("Ottawa"));
        
        assertEquals(1, results.get(0).size());
        assertEquals("Event A", results.get(0).get(0).title);
    }
    
    @Test
    public void getRsvpedEvents_ReturnsOnlyJoined() {
        eventDao.insert(new EventEntity("Joined", "D", "City", "2026", "1", "L", 5, true, "T"));
        eventDao.insert(new EventEntity("Not Joined", "D", "City", "2026", "1", "L", 5, false, "T"));

        // Assuming getRsvpedEvents exists in your DAO based on Activity usage
        List<EventEntity> rsvped = eventDao.getRsvpedEvents();
        assertEquals(1, rsvped.size());
        assertEquals("Joined", rsvped.get(0).title);
    }

    @Test
    public void deleteEvent_removesFromDatabase() {
        EventEntity event = new EventEntity("To Delete", "D", "City", "2026", "1", "L", 5, false, "T");
        long id = eventDao.insert(event);
        event.id = (int) id;

        eventDao.delete(event);

        EventEntity result = eventDao.getEventById((int) id);
        assertNull("Event should be null after deletion", result);
    }
}
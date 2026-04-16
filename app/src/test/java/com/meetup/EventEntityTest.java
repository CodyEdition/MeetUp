package com.meetup;

import com.meetup.db.EventEntity;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventEntityTest {

    @Test
    public void eventCreation_storesValuesCorrectly() {
        EventEntity event = new EventEntity(
                "Tech Talk", 
                "A discussion about AI", 
                "Ottawa", 
                "2026-10-10", 
                "14:00", 
                "Library Hall", 
                100, 
                false, 
                "AI,Technology"
        );

        assertEquals("Tech Talk", event.title);
        assertEquals("A discussion about AI", event.description);
        assertEquals("Ottawa", event.city);
        assertEquals("2026-10-10", event.date);
        assertEquals("14:00", event.time);
        assertEquals("Library Hall", event.location);
        assertEquals(100, event.maxAttendees);
        assertFalse(event.isRsvped);
        assertEquals("AI,Technology", event.tags);
    }

    @Test
    public void eventIgnoreConstructor_setsDefaultValues() {
        EventEntity event = new EventEntity("Simple Meeting", "Desc", "Toronto", "2026-12-01");
        
        assertEquals("Simple Meeting", event.title);
        assertEquals("Toronto", event.city);
        assertEquals("", event.time);
        assertEquals("", event.location);
        assertEquals(0, event.maxAttendees);
        assertEquals("", event.tags);
    }
}
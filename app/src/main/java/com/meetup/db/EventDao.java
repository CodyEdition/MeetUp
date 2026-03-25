package com.meetup.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao { //replaces PlaceholderDao

    @Insert
    long insert(EventEntity event);

    @Query("SELECT * FROM events")
    List<EventEntity> getAll();

    @Query("SELECT * FROM events WHERE city = :selectedCity")
    List<EventEntity> getEventsByCity(String selectedCity);

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    EventEntity getEventById(int eventId);

    @Query("UPDATE events SET isRsvped = :isRsvped WHERE id = :eventId")
    void updateRsvpStatus(int eventId, boolean isRsvped);
}
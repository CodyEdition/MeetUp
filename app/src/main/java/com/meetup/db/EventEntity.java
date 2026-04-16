package com.meetup.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public final String title;
    public final String description;
    public final String city;
    public final String date;
    public final String time;
    public final String location;
    public final int maxAttendees;

    public boolean isRsvped;

    public final String tags;

    public EventEntity(String title, String description, String city, String date, String time, String location, int maxAttendees, boolean isRsvped,String tags ) {
        this.title = title;
        this.description = description;
        this.city = city;
        this.date = date;
        this.time = time;
        this.location = location;
        this.maxAttendees = maxAttendees;
        this.isRsvped = isRsvped;
        this.tags = tags;
    }
}
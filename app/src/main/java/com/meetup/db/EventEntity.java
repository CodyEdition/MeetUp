package com.meetup.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity { // replaces placeholder

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public String city;
    public String date;
    public String time;
    public String location;
    public int maxAttendees;

    public boolean isRsvped;

    @Ignore
    public EventEntity(String title, String description, String city, String date) {
        this.title = title;
        this.description = description;
        this.city = city;
        this.date = date;
        this.time = "";
        this.location = "";
        this.maxAttendees = 0;
    }

    public EventEntity(String title, String description, String city, String date, String time, String location, int maxAttendees, boolean isRsvped) {
        this.title = title;
        this.description = description;
        this.city = city;
        this.date = date;
        this.time = time;
        this.location = location;
        this.maxAttendees = maxAttendees;
        this.isRsvped = isRsvped;
    }
}
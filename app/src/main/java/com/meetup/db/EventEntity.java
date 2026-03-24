package com.meetup.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity { // replaces placeholder

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public String city;
    public String date;

    public EventEntity(String title, String description, String city, String date) {
        this.title = title;
        this.description = description;
        this.city = city;
        this.date = date;
    }
}
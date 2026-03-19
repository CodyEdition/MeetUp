package com.meetup.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "placeholder")
public class PlaceholderEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public PlaceholderEntity(String name) {
        this.name = name;
    }
}

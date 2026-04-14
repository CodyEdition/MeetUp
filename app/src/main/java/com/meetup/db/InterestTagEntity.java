package com.meetup.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "interest_tags")
public class InterestTagEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public InterestTagEntity(String name) {
        this.name = name;
    }
}
package com.meetup.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaceholderDao {

    @Insert
    long insert(PlaceholderEntity entity);

    @Query("SELECT * FROM placeholder")
    List<PlaceholderEntity> getAll();
}

package com.meetup.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InterestTagDao {

    @Insert
    void insertTag(InterestTagEntity tag);

    @Query("SELECT * FROM interest_tags ORDER BY name ASC")
    List<InterestTagEntity> getAllTags();

    @Query("SELECT * FROM interest_tags WHERE LOWER(name) = LOWER(:tagName) LIMIT 1")
    InterestTagEntity findByName(String tagName);
}
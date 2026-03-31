package com.meetup.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateUser(UserEntity user);

    @Query("SELECT * FROM users LIMIT 1")
    UserEntity getCurrentUser();

    @Query("DELETE FROM users")
    void clearUser();
}
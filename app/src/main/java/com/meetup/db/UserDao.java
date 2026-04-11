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

    @Query("UPDATE users SET displayName = :displayName, bio = :bio WHERE id = :id")
    void updateProfile(int id, String displayName, String bio);

    @Query("UPDATE users SET email = :email WHERE id = :id")
    void updateEmail(int id, String email);

    @Query("UPDATE users SET interests = :interests WHERE id = :userId")
    void updateInterests(int userId, String interests);
}
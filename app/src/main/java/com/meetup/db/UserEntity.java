package com.meetup.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String userId; // Firebase UID
    public String email;
    public boolean isLoggedIn;
    public String lastSelectedCity;
    public String displayName;
    public String bio;

    public UserEntity(String userId, String email, boolean isLoggedIn) {
        this.userId = userId;
        this.email = email;
        this.isLoggedIn = isLoggedIn;
        this.lastSelectedCity = "";
        this.displayName = "";
        this.bio = "";
    }
}
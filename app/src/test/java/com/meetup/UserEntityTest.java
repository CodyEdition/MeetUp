package com.meetup;

import com.meetup.db.UserEntity;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserEntityTest {

    @Test
    public void userCreation_storesBasicInfo() {
        UserEntity user = new UserEntity("uid123", "test@example.com", true);
        
        assertEquals("uid123", user.userId);
        assertEquals("test@example.com", user.email);
        assertTrue(user.isLoggedIn);
        assertEquals("", user.lastSelectedCity); // Default value check
    }

    @Test
    public void userProfile_updatesCorrectly() {
        UserEntity user = new UserEntity("uid", "email", true);
        user.displayName = "Ozlem";
        user.bio = "Android Developer";
        user.interests = "Tech,Music";

        assertEquals("Ozlem", user.displayName);
        assertEquals("Android Developer", user.bio);
        assertEquals("Tech,Music", user.interests);
    }
}
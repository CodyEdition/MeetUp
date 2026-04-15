package com.meetup;

import com.meetup.db.InterestTagEntity;
import org.junit.Test;
import static org.junit.Assert.*;

public class InterestTagEntityTest {

    @Test
    public void tagCreation_storesNameCorrectly() {
        InterestTagEntity tag = new InterestTagEntity("Networking");
        assertEquals("Networking", tag.name);
    }

    @Test
    public void tagId_isZeroByDefault() {
        InterestTagEntity tag = new InterestTagEntity("Music");
        assertEquals(0, tag.id); // Room auto-generates this, so it should be 0 before insertion
    }
}
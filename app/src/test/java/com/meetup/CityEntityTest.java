package com.meetup;

import com.meetup.db.CityEntity;
import org.junit.Test;
import static org.junit.Assert.*;

public class CityEntityTest {

    @Test
    public void getFullDisplayName_formatsCorrectly() {
        CityEntity city = new CityEntity("Vancouver", "BC");
        assertEquals("Vancouver, BC", city.getFullDisplayName());
    }

    @Test
    public void cityEntity_storesDataCorrectly() {
        CityEntity city = new CityEntity("Calgary", "Alberta");
        assertEquals("Calgary", city.cityName);
        assertEquals("Alberta", city.province);
    }
}
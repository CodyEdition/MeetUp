package com.meetup;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CityHubTest {

    // ----------------------------------------------------------------
    // CityHub enum — display names
    // ----------------------------------------------------------------

    @Test
    public void cityHub_ottawa_hasCorrectDisplayName() {
        assertEquals("Ottawa", CityHub.OTTAWA.getDisplayName());
    }

    @Test
    public void cityHub_toronto_hasCorrectDisplayName() {
        assertEquals("Toronto", CityHub.TORONTO.getDisplayName());
    }

    @Test
    public void cityHub_montreal_hasCorrectDisplayName() {
        assertEquals("Montreal", CityHub.MONTREAL.getDisplayName());
    }

    // ----------------------------------------------------------------
    // fromDisplayName — valid inputs
    // ----------------------------------------------------------------

    @Test
    public void fromDisplayName_exactMatch_returnsCorrectHub() {
        assertEquals(CityHub.OTTAWA, CityHub.fromDisplayName("Ottawa"));
        assertEquals(CityHub.TORONTO, CityHub.fromDisplayName("Toronto"));
        assertEquals(CityHub.MONTREAL, CityHub.fromDisplayName("Montreal"));
    }

    @Test
    public void fromDisplayName_caseInsensitive_returnsHub() {
        assertEquals(CityHub.OTTAWA, CityHub.fromDisplayName("ottawa"));
        assertEquals(CityHub.TORONTO, CityHub.fromDisplayName("TORONTO"));
        assertEquals(CityHub.MONTREAL, CityHub.fromDisplayName("montreal"));
    }

    @Test
    public void fromDisplayName_unknownCity_returnsNull() {
        assertNull(CityHub.fromDisplayName("Vancouver"));
    }

    @Test
    public void fromDisplayName_emptyString_returnsNull() {
        assertNull(CityHub.fromDisplayName(""));
    }

    @Test
    public void fromDisplayName_null_returnsNull() {
        assertNull(CityHub.fromDisplayName(null));
    }

    // ----------------------------------------------------------------
    // Persistence / DB filter compatibility
    // CityHub.getDisplayName() must match the string stored in EventEntity.city
    // and used in EventDao.getEventsByCity(String)
    // ----------------------------------------------------------------

    @Test
    public void allHubs_displayNameIsNonNull() {
        for (CityHub hub : CityHub.values()) {
            assertNotNull(hub.getDisplayName());
        }
    }

    @Test
    public void allHubs_displayNameIsNonEmpty() {
        for (CityHub hub : CityHub.values()) {
            assertNotNull(hub.getDisplayName());
            assertEquals(false, hub.getDisplayName().trim().isEmpty());
        }
    }

    @Test
    public void allHubs_fromDisplayName_roundTrip() {
        // getDisplayName() -> fromDisplayName() must always return same enum value
        for (CityHub hub : CityHub.values()) {
            assertEquals(hub, CityHub.fromDisplayName(hub.getDisplayName()));
        }
    }

    @Test
    public void sharedPrefsKey_defaultFallback_isFirstHub() {
        // EventBrowsingActivity falls back to CityHub.values()[0] when prefs are empty.
        // This ensures at least one city is always available.
        CityHub defaultHub = CityHub.values()[0];
        assertNotNull(defaultHub);
        assertNotNull(defaultHub.getDisplayName());
    }
}

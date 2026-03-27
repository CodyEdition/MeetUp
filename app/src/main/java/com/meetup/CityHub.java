package com.meetup;

public enum CityHub {
    OTTAWA("Ottawa"),
    TORONTO("Toronto"),
    MONTREAL("Montreal");

    private final String displayName;

    CityHub(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CityHub fromDisplayName(String name) {
        if (name == null) return null;
        for (CityHub hub : values()) {
            if (hub.displayName.equalsIgnoreCase(name)) {
                return hub;
            }
        }
        return null;
    }
}

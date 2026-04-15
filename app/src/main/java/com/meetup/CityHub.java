package com.meetup;

public enum CityHub {
    OTTAWA("Ottawa", "Ontario"),
    TORONTO("Toronto", "Ontario"),
    MONTREAL("Montreal", "Quebec");

    private final String displayName;
    private final String province;

    CityHub(String displayName, String province) {
        this.displayName = displayName;
        this.province = province;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProvince() {
        return province;
    }

    public static CityHub fromDisplayName(String text) {
        if (text == null) return null;
        for (CityHub b : CityHub.values()) {
            if (b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
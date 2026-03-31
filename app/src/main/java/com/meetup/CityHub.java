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
}
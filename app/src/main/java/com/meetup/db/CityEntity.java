package com.meetup.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cities")
public class CityEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String cityName;
    public String province;

    public CityEntity(String cityName, String province) {
        this.cityName = cityName;
        this.province = province;
    }

    public String getFullDisplayName() {
        return cityName + ", " + province;
    }
}
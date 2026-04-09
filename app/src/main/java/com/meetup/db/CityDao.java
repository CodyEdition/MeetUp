package com.meetup.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCity(CityEntity city);

    @Query("SELECT * FROM cities")
    List<CityEntity> getAllCities();

    @Query("DELETE FROM cities WHERE cityName = :cityName")
    void deleteCity(String cityName);
}
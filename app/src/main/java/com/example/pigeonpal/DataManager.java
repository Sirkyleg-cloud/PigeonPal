package com.example.pigeonpal;


import java.util.ArrayList;
import java.util.List;

public class DataManager {

// The single instance (Singleton pattern)
private static DataManager instance;

    // The list that stores all sensor readings
    private List<SensorReading> readings;

    private DataManager() {
        readings = new ArrayList<>();

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void addReading(float value) {
        SensorReading reading = new SensorReading(value);
        readings.add(reading);
    }


    public List<SensorReading> getAllReadings() {
        return readings;
    }


    public int getReadingCount() {
        return readings.size();
    }

    public void clearReadings() {

        readings.clear();
    }

}
package com.example.pigeonpal;


import java.util.ArrayList;
import java.util.List;

/**
 * DataManager - Singleton class to store sensor readings
 *
 * Singleton means only ONE instance exists for the entire app.
 * All activities access the SAME data through this class.
 */
public class DataManager {

// The single instance (Singleton pattern)
private static DataManager instance;

    // The list that stores all sensor readings
    private List<SensorReading> readings;

    /**
     * Private constructor - prevents creating instances with 'new'
     * Forces use of getInstance() method
     */
    private DataManager() {
        readings = new ArrayList<>();
        // ArrayList is a resizable array
        // Can add/remove items dynamically
    }

    /**
     * Get the single instance of DataManager
     * Creates it if it doesn't exist yet
     *
     * @return The DataManager instance
     */
    public static DataManager getInstance() {
        if (instance == null) {
            // First time called - create the instance
            instance = new DataManager();
        }
        return instance;
        // 'static' means this method belongs to the class, not an object
        // Can call it as: DataManager.getInstance()
    }

    /**
     * Add a new sensor reading
     * Automatically adds current timestamp
     *
     * @param value The sensor value to add
     */
    public void addReading(int value) {
        long timestamp = System.currentTimeMillis();
        // System.currentTimeMillis() returns current time
        // as milliseconds since January 1, 1970

        SensorReading reading = new SensorReading(value, timestamp);
        readings.add(reading);
        // add() appends to the end of the list
    }

    /**
     * Get all sensor readings
     *
     * @return List of all readings
     */
    public List<SensorReading> getAllReadings() {
        return readings;
    }

/**
 * Get the number of readings stored
 *
 * @return Count of readings
 */
public int getReadingCount() {
    return readings.size();
}

    /**
     * Clear all readings (for testing/reset)
     */
    public void clearReadings() {
        readings.clear();
    }

    /**
     * Count readings by status category
     * Used for Pie Chart
     *
     * @param status "Low", "Medium", or "High"
     * @return Count of readings with that status
     */
    public int countByStatus(String status) {
        int count = 0;
        for (SensorReading reading : readings) {
            // Enhanced for loop - iterates through each item
            if (reading.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }
}
package com.example.pigeonpal;

/**
 * SensorReading - Represents a single sensor data point
 *
 * This class holds:
 * - The sensor value (0-100)
 * - The timestamp when it was recorded
 */
public class SensorReading {

    private int value;          // The sensor value (0-100)
    private long timestamp;     // Time in milliseconds since Jan 1, 1970

    /**
     * Constructor - creates a new SensorReading
     *
     * @param value     The sensor value
     * @param timestamp When it was recorded
     */
    public SensorReading(int value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
        // 'this' refers to the current object
        // this.value = the object's field
        // value = the parameter passed in
    }

    /**
     * Get the sensor value
     * @return The value (0-100)
     */
    public int getValue() {
        return value;
    }

    /**
     * Get the timestamp
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    /**
     * Determine the status category based on value
     * @return "Low", "Medium", or "High"
     */
    public String getStatus() {
        if (value < 30) {
            return "Low";
        } else if (value <= 65) {
            return "Medium";
        } else {
            return "High";
        }
    }
}

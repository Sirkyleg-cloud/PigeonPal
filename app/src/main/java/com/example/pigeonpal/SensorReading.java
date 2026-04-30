package com.example.pigeonpal;

public class SensorReading {

    private float value;          // The sensor value (0-100)
    private long timestamp;     // Time in milliseconds since Jan 1, 1970

    /**
     * Constructor - creates a new SensorReading
     *
     * @param value The sensor value
     */
    public SensorReading(float value) {
        this.value = value;
       // this.timestamp = timestamp;
        // 'this' refers to the current object
        // this.value = the object's field
        // value = the parameter passed in
    }

    /**
     * Get the sensor value
     * @return The value (0-100)
     */
    public float getValue() {
        return value;
    }

    /**
     * Get the timestamp
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

}

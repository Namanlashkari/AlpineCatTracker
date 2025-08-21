package com.alpinecattracker;

import java.time.Instant;

/**
 * A location report for a cat.
 */
public class CatLocation {
    private final double latitude;
    private final double longitude;
    private final Instant timestamp;

    public CatLocation(double latitude, double longitude, Instant timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

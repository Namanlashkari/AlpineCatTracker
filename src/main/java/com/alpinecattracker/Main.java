package com.alpinecattracker;

import java.time.Duration;

/**
 * Example application showing how to use {@link CatTrackerService}.
 */
public class Main {
    public static void main(String[] args) {
        CatTrackerService tracker = new CatTrackerService(new MockAirTagClient(), Duration.ofHours(24));
        tracker.addCat(new Cat("1", "Milo", "milo.jpg"));
        tracker.addCat(new Cat("2", "Luna", "luna.jpg"));
        tracker.startPolling(Duration.ofMinutes(5));

        Runtime.getRuntime().addShutdownHook(new Thread(tracker::shutdown));
    }
}

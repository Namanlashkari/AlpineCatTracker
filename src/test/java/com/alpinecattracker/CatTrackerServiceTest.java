package com.alpinecattracker;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class CatTrackerServiceTest {

    @Test
    void lastLocationRetainedWhenNoUpdate() {
        Cat cat = new Cat("1", "Milo", "milo.jpg");
        AtomicInteger calls = new AtomicInteger();
        AirTagClient client = c -> {
            if (calls.getAndIncrement() == 0) {
                return Optional.of(new CatLocation(10.0, 20.0, Instant.now()));
            }
            return Optional.empty();
        };
        CatTrackerService tracker = new CatTrackerService(client, new com.alpinecattracker.dao.InMemoryCatLocationDao(), Duration.ofHours(24));
        tracker.addCat(cat);

        tracker.pollOnce(); // first poll returns location
        tracker.pollOnce(); // second poll returns empty

        Optional<CatLocation> location = tracker.getLatestLocation("1");
        assertTrue(location.isPresent());
        assertEquals(10.0, location.get().getLatitude());
        assertEquals(20.0, location.get().getLongitude());
    }

    @Test
    void cleanupRemovesOldLocations() throws InterruptedException {
        Cat cat = new Cat("1", "Milo", "milo.jpg");
        AtomicInteger calls = new AtomicInteger();
        AirTagClient client = c -> {
            if (calls.getAndIncrement() == 0) {
                return Optional.of(new CatLocation(0.0, 0.0, Instant.now()));
            }
            return Optional.empty();
        };
        Duration retention = Duration.ofMillis(500);
        CatTrackerService tracker = new CatTrackerService(client, new com.alpinecattracker.dao.InMemoryCatLocationDao(), retention);
        tracker.addCat(cat);

        tracker.pollOnce(); // add initial location
        Thread.sleep(retention.toMillis() + 100);
        tracker.pollOnce(); // trigger cleanup

        assertTrue(tracker.getLatestLocation("1").isEmpty());
    }
}

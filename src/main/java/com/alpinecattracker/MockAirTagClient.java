package com.alpinecattracker;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock implementation of {@link AirTagClient} that generates random locations.
 */
public class MockAirTagClient implements AirTagClient {
    @Override
    public Optional<CatLocation> fetchLocation(Cat cat) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        // 20% chance that the location is not available
        if (rand.nextInt(5) == 0) {
            return Optional.empty();
        }
        double lat = -90 + 180 * rand.nextDouble();
        double lon = -180 + 360 * rand.nextDouble();
        return Optional.of(new CatLocation(lat, lon, Instant.now()));
    }
}

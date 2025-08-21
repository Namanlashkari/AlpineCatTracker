package com.alpinecattracker;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service that polls AirTags for registered cats and keeps their last known locations.
 */
public class CatTrackerService {
    private final AirTagClient airTagClient;
    private final Map<String, Cat> cats = new HashMap<>();
    private final Map<String, Deque<CatLocation>> locationHistory = new HashMap<>();
    private final Duration retention;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CatTrackerService(AirTagClient airTagClient, Duration retention) {
        this.airTagClient = airTagClient;
        this.retention = retention;
    }

    /** Adds a cat to be tracked. */
    public void addCat(Cat cat) {
        cats.put(cat.getId(), cat);
        locationHistory.computeIfAbsent(cat.getId(), id -> new ArrayDeque<>());
    }

    /**
     * Start polling all registered cats at the provided interval.
     */
    public void startPolling(Duration interval) {
        long millis = interval.toMillis();
        scheduler.scheduleAtFixedRate(this::pollOnce, 0, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Polls all cats exactly once. Exposed for testing.
     */
    public void pollOnce() {
        cats.values().forEach(cat -> {
            Optional<CatLocation> location = airTagClient.fetchLocation(cat);
            location.ifPresent(loc -> locationHistory.get(cat.getId()).addLast(loc));
        });
        cleanup();
    }

    /**
     * Returns the most recent location for the given cat id, if available.
     */
    public Optional<CatLocation> getLatestLocation(String catId) {
        Deque<CatLocation> deque = locationHistory.get(catId);
        if (deque == null || deque.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(deque.getLast());
    }

    /**
     * Finds a cat by its photo URL.
     */
    public Optional<Cat> getCatByPhotoUrl(String photoUrl) {
        return cats.values().stream()
                .filter(c -> c.getPhotoUrl().equals(photoUrl))
                .findFirst();
    }

    /**
     * Stops the polling scheduler.
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void cleanup() {
        Instant cutoff = Instant.now().minus(retention);
        locationHistory.values().forEach(deque -> {
            while (!deque.isEmpty() && deque.peekFirst().getTimestamp().isBefore(cutoff)) {
                deque.removeFirst();
            }
        });
    }
}

package com.alpinecattracker;

import java.time.Duration;
import java.time.Instant;
import com.alpinecattracker.dao.CatLocationDao;
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
    private final CatLocationDao locationDao;
    private final Duration retention;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CatTrackerService(AirTagClient airTagClient, CatLocationDao locationDao, Duration retention) {
        this.airTagClient = airTagClient;
        this.locationDao = locationDao;
        this.retention = retention;
    }

    /** Adds a cat to be tracked. */
    public void addCat(Cat cat) {
        cats.put(cat.getId(), cat);
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
            location.ifPresent(loc -> locationDao.saveLocation(cat.getId(), loc));
        });
        locationDao.cleanupOlderThan(Instant.now().minus(retention));
    }

    /**
     * Returns the most recent location for the given cat id, if available.
     */
    public Optional<CatLocation> getLatestLocation(String catId) {
        return locationDao.getLatestLocation(catId);
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
}

package com.alpinecattracker.dao;

import com.alpinecattracker.CatLocation;
import java.time.Instant;
import java.util.Optional;

/**
 * DAO for persisting and querying cat locations.
 */
public interface CatLocationDao {
    /**
     * Persists a location for the given cat id.
     */
    void saveLocation(String catId, CatLocation location);

    /**
     * Returns the most recent location for the given cat id.
     */
    Optional<CatLocation> getLatestLocation(String catId);

    /**
     * Removes all locations older than the provided cutoff.
     */
    void cleanupOlderThan(Instant cutoff);
}

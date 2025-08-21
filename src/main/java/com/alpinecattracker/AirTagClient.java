package com.alpinecattracker;

import java.util.Optional;

/**
 * Client capable of fetching the location of a cat's AirTag.
 * Implementations may call external services or simulate them.
 */
public interface AirTagClient {
    /**
     * Fetches the latest location of the provided cat.
     *
     * @param cat cat whose location should be fetched
     * @return an optional location. If empty, the location was not available
     */
    Optional<CatLocation> fetchLocation(Cat cat);
}

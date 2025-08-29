package com.alpinecattracker.dao;

import com.alpinecattracker.CatLocation;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple in-memory implementation used for tests.
 */
public class InMemoryCatLocationDao implements CatLocationDao {
    private final Map<String, Deque<CatLocation>> history = new HashMap<>();

    @Override
    public void saveLocation(String catId, CatLocation location) {
        history.computeIfAbsent(catId, id -> new ArrayDeque<>()).addLast(location);
    }

    @Override
    public Optional<CatLocation> getLatestLocation(String catId) {
        Deque<CatLocation> deque = history.get(catId);
        if (deque == null || deque.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(deque.getLast());
    }

    @Override
    public void cleanupOlderThan(Instant cutoff) {
        history.values().forEach(deque -> {
            while (!deque.isEmpty() && deque.peekFirst().getTimestamp().isBefore(cutoff)) {
                deque.removeFirst();
            }
        });
    }
}

package com.alpinecattracker.dao;

import com.alpinecattracker.CatLocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * PostgreSQL implementation that stores locations using PostGIS and a geolocation index.
 */
public class PostgresCatLocationDao implements CatLocationDao {
    private final DataSource dataSource;

    public PostgresCatLocationDao(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }

    private void init() {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS cat_location (
                        cat_id VARCHAR NOT NULL,
                        location GEOGRAPHY(Point,4326) NOT NULL,
                        timestamp TIMESTAMPTZ NOT NULL
                    )
                    """);
            st.execute("CREATE INDEX IF NOT EXISTS idx_cat_location_geo ON cat_location USING GIST (location)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize Postgres schema", e);
        }
    }

    @Override
    public void saveLocation(String catId, CatLocation location) {
        String sql = "INSERT INTO cat_location(cat_id, location, timestamp) VALUES (?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, catId);
            ps.setDouble(2, location.getLongitude());
            ps.setDouble(3, location.getLatitude());
            ps.setObject(4, location.getTimestamp());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save location", e);
        }
    }

    @Override
    public Optional<CatLocation> getLatestLocation(String catId) {
        String sql = "SELECT ST_Y(location::geometry) AS latitude, ST_X(location::geometry) AS longitude, timestamp FROM cat_location WHERE cat_id=? ORDER BY timestamp DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, catId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double lat = rs.getDouble("latitude");
                    double lon = rs.getDouble("longitude");
                    Instant ts = rs.getObject("timestamp", Instant.class);
                    return Optional.of(new CatLocation(lat, lon, ts));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch latest location", e);
        }
        return Optional.empty();
    }

    @Override
    public void cleanupOlderThan(Instant cutoff) {
        String sql = "DELETE FROM cat_location WHERE timestamp < ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, cutoff);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to cleanup old locations", e);
        }
    }
}

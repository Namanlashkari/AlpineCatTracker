package com.alpinecattracker;

import com.alpinecattracker.dao.CatLocationDao;
import com.alpinecattracker.dao.PostgresCatLocationDao;
import java.time.Duration;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Example application showing how to use {@link CatTrackerService}.
 */
public class Main {
    public static void main(String[] args) {
        CatLocationDao dao = new PostgresCatLocationDao(createDataSource());
        CatTrackerService tracker = new CatTrackerService(new MockAirTagClient(), dao, Duration.ofHours(24));
        tracker.addCat(new Cat("1", "Milo", "milo.jpg"));
        tracker.addCat(new Cat("2", "Luna", "luna.jpg"));
        tracker.startPolling(Duration.ofMinutes(5));

        Runtime.getRuntime().addShutdownHook(new Thread(tracker::shutdown));
    }

    private static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/cats"));
        ds.setUser(System.getenv().getOrDefault("DB_USER", "postgres"));
        ds.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "postgres"));
        return ds;
    }
}

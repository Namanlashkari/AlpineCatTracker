# Alpine Cat Tracker

Track outdoor cats with AirTag-style location polls, keep a short history, and look up the last known spot.

[![CI](https://github.com/Namanlashkari/AlpineCatTracker/actions/workflows/ci.yml/badge.svg)](https://github.com/Namanlashkari/AlpineCatTracker/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-0B7285.svg)](LICENSE)

## Why it exists

A small Java 17 service that:

- registers cats (id, name, photo)
- polls an `AirTagClient` on a schedule
- stores locations behind a DAO (in-memory or Postgres)
- drops points older than a retention window

Built to show clean layering: client → service → DAO. Tests run without a database.

## Architecture

```
CatTrackerService
    ├── AirTagClient          (live or MockAirTagClient)
    └── CatLocationDao
            ├── InMemoryCatLocationDao
            └── PostgresCatLocationDao
```

## Requirements

- JDK 17+
- Maven 3.9+
- Optional: Postgres if you run `Main` against a real DB

## Quick start

```bash
git clone https://github.com/Namanlashkari/AlpineCatTracker.git
cd AlpineCatTracker
mvn -q test
```

Run the demo process (needs `DB_URL` / `DB_USER` / `DB_PASSWORD`, or defaults to `localhost:5432/cats`):

```bash
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.alpinecattracker.Main
```

Or keep it fully local by wiring `InMemoryCatLocationDao` in your own `main`.

## Configuration

| Variable      | Default                                 |
|---------------|-----------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/cats` |
| `DB_USER`     | `postgres`                              |
| `DB_PASSWORD` | `postgres`                              |

## Tests

`CatTrackerServiceTest` covers:

- last known location kept when a later poll returns empty
- retention cleanup after the window expires

```bash
mvn test
```

CI runs the same command on every push to `main`.

## License

MIT

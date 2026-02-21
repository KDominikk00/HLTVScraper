# HLTV Scraper API

Spring Boot service that scrapes HLTV player stats and exposes a reusable API + service layer for other applications.

## What changed

- Rebuilt around a pluggable source abstraction (`HltvPlayerSource`) so parsing/fetching backends can be swapped.
- Added source fallback strategy (`ConfiguredHltvPlayerSource`) with configurable order (`jsoup`, `selenium`).
- Kept a fast JSoup scraper and added Selenium headless fallback for anti-bot protected responses.
- Added typed domain models (`PlayerStats`) and response models.
- Added thread-safe TTL cache with `force refresh` support.
- Added query filters (`team`, `minRating`, `limit`) and player lookup by nickname.
- Added centralized exception handling with consistent JSON errors.
- Replaced fragile live-browser tests with deterministic unit/web tests.

## API

Base path: `/api/v1/hltv`

### `GET /players`

Query params:

- `refresh` (`true|false`, default `false`): bypass cache.
- `limit` (`> 0`): max number of players.
- `team` (string): case-insensitive team filter.
- `minRating` (`>= 0`): minimum rating filter.

Response:

```json
{
  "players": [
    {
      "nickname": "donk",
      "team": "Spirit",
      "teamLogo": "https://...",
      "maps": 90,
      "rounds": 1900,
      "killDeathDiff": 150,
      "killDeathRatio": 1.3,
      "rating": 1.35
    }
  ],
  "count": 1,
  "fetchedAt": "2026-01-01T00:00:00Z",
  "fromCache": true
}
```

### `GET /players/{nickname}`

Returns a single player or `404`.

## Configuration

`src/main/resources/application.properties`

- `hltv.scraper.players-url`
- `hltv.scraper.request-timeout`
- `hltv.scraper.cache-ttl`
- `hltv.scraper.allowed-origins`
- `hltv.scraper.source-order` (default `jsoup,selenium`)
- `hltv.scraper.selenium-wait-timeout`
- `hltv.scraper.selenium-headless`

## Build and run

```bash
./mvnw clean test
./mvnw spring-boot:run
```

## Framework extension point

To add another data source (e.g. Selenium fallback, static file snapshots, proxy-backed fetcher), implement:

- `com.example.hltvscraper.hltv.infrastructure.HltvPlayerSourceClient`

Then register it as a Spring bean and add its id to `hltv.scraper.source-order`.

## Disclaimer

I do my best to maintain this project, but it is a solo effort. Frequent UI and structure changes on HLTV may impact whether the scraper works as expected.

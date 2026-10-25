<h1 align="center">CS Stats API</h1>

<p align="center">
  <em>Pro Counter-Strike 2 player stats, scraped and served.</em><br />
  A Spring Boot service that scrapes player stats from <a href="https://bo3.gg">bo3.gg</a> with headless Chrome
  and serves them over a small read-only REST API. It is the backend of
  <a href="https://cscomparison.vercel.app">CSComparison</a>.
</p>

<p align="center">
  <img alt="Java 21" src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" />
  <img alt="Spring Boot 4.1" src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white" />
  <img alt="Selenium 4.51" src="https://img.shields.io/badge/Selenium-4.51-43B02A?logo=selenium&logoColor=white" />
  <img alt="Maven" src="https://img.shields.io/badge/build-Maven-C71A36?logo=apachemaven&logoColor=white" />
  <img alt="Docker" src="https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white" />
</p>

<p align="center">
  <a href="https://cscomparison.vercel.app">Live site</a> ·
  <a href="https://cs-stats-api.onrender.com/api/v1/players?limit=3">Live API</a> ·
  <a href="https://github.com/KDominikk00/CSComparison">Frontend repo</a>
</p>

## Features

- **Rich player data.** For every player: profile, 6 month totals with win rates, accuracy by body part, per-map stats, and the in-game stats of the last 15 matches in seven categories (Main, Performance, AIM, Grenades, Primary devices, Pistols, Economy), each next to the average over all players.
- **Automatic discovery.** About 250 players on 58 teams, found by following the ranked list, the current tournaments, their teams and the teams' starting rosters. No hand-kept player list.
- **Read-only REST API.** A summary list with `limit`, `team` and `minScore` filters, and a full lookup by slug or nickname.
- **Scraping and serving are separate.** The API downloads a published snapshot and needs no browser, so it fits on a small free host.
- **Keeps serving through failures.** If a scrape or a download fails, the previous snapshot stays in place. The first scrape publishes partial results every 25 players.
- **Polite crawler.** One browser, at least 2 seconds between page loads, only pages that `robots.txt` allows, one full run per week.
- **Tested against real pages.** The parsers run against saved copies of the actual bo3.gg pages, and an opt-in live smoke test catches markup changes.

## How it works

```
   Your Mac (weekly, launchd)                          Render (free web service)
 +------------------------------+                    +--------------------------------+
 | scripts/scrape-and-publish   |                    | Spring Boot API (Docker)       |
 |  Selenium + headless Chrome  |   GitHub release   |  downloads the snapshot at     |
 |  ranked players              |  "data" asset:     |  startup and every 6 hours     |
 |  -> tournaments -> teams     | -----------------> |  keeps it in memory + on disk  |
 |  -> rosters -> player pages  | players-snapshot   |  REST  /api/v1/*               |
 +------------------------------+       .json        +----------------+---------------+
                                                                      |
                                                                      v
                                                    CSComparison (React, on Vercel)
```

bo3.gg shows a Cloudflare challenge to datacenter IPs, so the scrape cannot run on GitHub Actions or any cloud host. It runs from a home connection instead, and the API only ever reads the file it produced.

The same code runs in three modes, chosen by configuration:

| Mode | How to start | What it does |
| --- | --- | --- |
| **Local dev** | `./mvnw spring-boot:run` | Serves the API and scrapes in the same process, on startup and then every `scraper.refresh-interval`. |
| **Scrape job** | `--spring.profiles.active=scrape` | Does one scrape, writes the snapshot file and exits. A failed scrape exits with a non-zero code. |
| **API only** | `scraper.source=remote` (the Docker image) | Downloads the snapshot from `scraper.snapshot-url`, serves it, refreshes on a schedule. |

## Data sources

Everything comes from bo3.gg pages that its `robots.txt` allows. The filtered list URLs (`?tab=`, `?sort=`, `?period=` ...) and `/api/` are disallowed and are never requested.

| Data | Page | Notes |
| --- | --- | --- |
| Ranked players | `/players` | The default view only: the top 50 by score. |
| Tournaments | `/` | Current and recent events linked from the home page. `/tournaments/current` and `/finished` are listings and are skipped. |
| Teams | `/tournaments/{slug}` | Read from the tournament's own team list. The main menu's `/teams/earnings` link is not a team. |
| Rosters | `/teams/{slug}` | Starters only. Coaches and former members are ignored. |
| Player stats | `/players/{slug}` | Profile, 6 month totals, accuracy, map stats. The seven in-game categories come from the page's dropdown, which the crawler operates one option at a time. |

A bad team or tournament page is logged and skipped instead of stopping the crawl, and a scrape with fewer than `scraper.min-players` players counts as failed.

## REST API

Base path `/api/v1`. Only `GET` and `OPTIONS` are allowed cross-origin, and only from `scraper.allowed-origins`.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/players` | Summary of every player. Params: `limit` (> 0), `team` (case-insensitive contains), `minScore` (>= 0). |
| `GET` | `/players/{slug-or-nickname}` | One player with every stat. Matches the slug first, then the nickname, ignoring case. |
| `GET` | `/actuator/health` | Liveness and readiness. |

**Errors** share one shape, `{timestamp, status, error, message, path}`: `400` for invalid parameters, `404` for an unknown player, `503` until the first snapshot has loaded, `500` for anything else.

### `GET /players`

```json
{
  "players": [
    {
      "slug": "zywoo",
      "nickname": "ZywOo",
      "country": "France",
      "avatarUrl": "https://image-proxy.bo3.gg/...",
      "team": "Vitality",
      "teamSlug": "vitality",
      "teamLogoUrl": "https://image-proxy.bo3.gg/...",
      "score": 7.2,
      "killsPerRound": 0.84,
      "deathsPerRound": 0.53,
      "killDeathRatio": 1.58,
      "damagePerRound": 86.67,
      "maps": 95,
      "rounds": 1983
    }
  ],
  "count": 1,
  "fetchedAt": "2026-10-25T15:29:27Z"
}
```

### `GET /players/zywoo`

The summary fields above, plus:

```json
{
  "fullName": "Mathieu Herbaut",
  "age": 25,
  "birthDate": "2000-11-09",
  "prizeMoney": 1615290.0,
  "totals": {
    "tournaments": { "count": 9, "winRate": 33.0 },
    "matches": { "count": 38, "winRate": 74.0 },
    "maps": { "count": 95, "winRate": 68.0 },
    "rounds": { "count": 1983, "winRate": 56.0 }
  },
  "accuracy": [{ "bodyPart": "Head", "hits": 867, "share": 24.0 }],
  "categories": [
    {
      "name": "AIM",
      "stats": [{ "name": "Headshots %", "value": 43.1, "average": 46.0, "unit": "%" }]
    }
  ],
  "mapStats": [{ "map": "Cache", "score": 7.5, "count": 9, "killsPerRound": 0.93, "damagePerRound": 94.0 }]
}
```

`score` is bo3.gg's own per-map rating, not HLTV rating. `totals`, `accuracy` and `mapStats` cover the last 6 months. `categories` cover the last 15 matches, per round unless the unit says otherwise.

## Tech stack

| Area | Choice |
| --- | --- |
| Runtime | Java 21, virtual threads enabled |
| Framework | Spring Boot 4.1 (Web, Validation, Actuator), Jackson 3 |
| Scraping | Selenium 4.51 with headless Chrome (Selenium Manager finds the driver), jsoup 1.23 for parsing |
| Storage | One JSON snapshot, held in memory and written atomically to a file |
| Scheduling | Spring `SchedulingConfigurer`, start delay derived from the age of the snapshot |
| Packaging | Maven wrapper, multi-stage Docker image on `eclipse-temurin:21-jre`, non-root user |
| Tests | JUnit 5, Mockito, MockMvc, a JDK `HttpServer` for the download tests |

## Getting started

Requirements: JDK 21 and, for scraping, Chrome.

```bash
./mvnw clean test
SCRAPER_MAX_PLAYERS=10 SCRAPER_MIN_PLAYERS=5 ./mvnw spring-boot:run     # small run, API on http://localhost:8080
```

The API answers `503` until the scrape has stored its first snapshot, about 90 seconds for 10 players. (`SCRAPER_MIN_PLAYERS` is lowered because a scrape with fewer than 20 players counts as failed.) Without these variables it scrapes up to 300 players, which takes about 20 minutes, and publishes a partial snapshot every 25 players along the way.

**Run one scrape and exit**

```bash
java -jar target/cs-stats-api-*.jar --spring.profiles.active=scrape --scraper.snapshot-path=out/players-snapshot.json
```

**Run the API image against the published data**

```bash
docker build -t cs-stats-api .
docker run -p 8080:8080 cs-stats-api
```

**Tests**

```bash
./mvnw test                                                     # 38 tests, parsers run on saved real pages
./mvnw test -Dtest=LiveScrapeSmokeTest -DliveScrape=true        # scrapes 3 real players, off by default
```

## Configuration

Every property can be set as an environment variable (`scraper.max-players` is `SCRAPER_MAX_PLAYERS`).

| Property | Default | Notes |
| --- | --- | --- |
| `scraper.source` | `browser` | `browser` scrapes in this process, `remote` downloads `scraper.snapshot-url`. |
| `scraper.snapshot-url` | | Where the scrape job published the snapshot. Required for `remote`. |
| `scraper.refresh-interval` | `24h` | Time between scrapes or downloads. The Docker image sets `6h`. |
| `scraper.snapshot-path` | `data/players-snapshot.json` | Local copy that survives restarts. |
| `scraper.allowed-origins` | `http://localhost:5173` | CORS origins, comma separated. |
| `scraper.base-url` | `https://bo3.gg` | |
| `scraper.request-delay` | `2s` | Minimum gap between two page loads. |
| `scraper.page-timeout` | `30s` | Page load and element wait limit. |
| `scraper.headless` | `true` | Set `false` to watch the browser. |
| `scraper.max-players` | `300` | Upper limit for one scrape. |
| `scraper.max-tournaments` | `12` | Tournaments used to discover teams. |
| `scraper.min-players` | `20` | A scrape or download with fewer players is treated as failed. |
| `scraper.refresh-enabled` | `true` | `false` turns the schedule off. |

## Deployment

**API on [Render](https://render.com)**: a Docker web service on the free plan, built from this repository. Set `SERVER_PORT=10000` (Render's default port) and `SCRAPER_ALLOWED_ORIGINS` to the frontend's URL. The image already points `scraper.snapshot-url` at the `data` release of this repository. The free plan sleeps when idle, so the first request after a quiet spell takes about a minute.

**Weekly scrape on a Mac**: [`scripts/scrape-and-publish.sh`](scripts/scrape-and-publish.sh) builds the project, runs the scrape job and uploads `players-snapshot.json` to the `data` release with the GitHub CLI (`gh`, logged in). `SKIP_PUBLISH=1` scrapes without uploading, for testing. To run it every Monday at 10:00, and at the next wake-up if the Mac was asleep:

```bash
sed "s#REPO_PATH#$(pwd)#" scripts/com.cscomparison.scrape.plist > ~/Library/LaunchAgents/com.cscomparison.scrape.plist
launchctl bootstrap gui/$(id -u) ~/Library/LaunchAgents/com.cscomparison.scrape.plist
```

Its log is `/tmp/cscomparison-scrape.log`. Remove it with `launchctl bootout gui/$(id -u)/com.cscomparison.scrape`.

## Project structure

```
src/main/java/com/example/csstats/
  api/                  PlayerController, response records, error handling
  application/          PlayerStatsService, SnapshotRefresher, ScrapeJob, snapshot sources and store ports
  domain/               PlayerStats and its parts (CountStat, StatCategory, MapStats ...)
  infrastructure/
    FileSnapshotStore   in-memory snapshot mirrored to a JSON file
    RemoteSnapshotSource downloads a published snapshot
    bo3/                Bo3PlayerSource (the crawl), Bo3LinkParser, Bo3PlayerPageParser, Selenium browser session
  config/               properties, CORS, refresh schedule
src/main/resources/     application.properties, application-scrape.properties (scrape job profile)
src/test/resources/fixtures/bo3/   saved copies of the real pages the parsers are tested against
scripts/                scrape-and-publish.sh, launchd template
Dockerfile              API-only image, no browser
```

## Limitations

- **Scraping needs a home connection.** bo3.gg answers cloud IPs with a Cloudflare challenge, which was confirmed from GitHub Actions. The weekly job therefore needs the Mac to be on and online at some point on Mondays. If a week is missed, the API keeps serving the last snapshot.
- **Scraping is fragile.** bo3.gg has no public API, so a redesign will break the selectors. They live in `infrastructure/bo3`, and the live smoke test fails first.
- **Only part of the player pool.** Players come from the ranked list and the teams of current tournaments, so inactive or unlisted players are missing. Players without a bo3.gg photo have no `avatarUrl`.
- **Third-party data.** `robots.txt` allows the pages this crawler visits, but the site's terms may still restrict automated use. Keep the scrape infrequent and the request delay generous. The stats are informational, and `score` is bo3.gg's own rating.
- **Free hosting trade-offs.** The Render free plan sleeps when idle and has 512 MB of memory, of which the API uses roughly 350 MB.

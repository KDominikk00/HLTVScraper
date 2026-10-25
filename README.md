# CS Stats API

Spring Boot service that scrapes Counter-Strike 2 player stats from [bo3.gg](https://bo3.gg) and serves them over a small read-only REST API. It is the backend for the [CSComparison](https://github.com/KDominikk00/CSComparison) site.

## How it works

Scraping and serving are separate, so the API never needs a browser.

- **Scrape job.** `scripts/scrape-and-publish.sh` starts the app with the `scrape` profile. It crawls bo3.gg with headless Chrome (Selenium), writes `players-snapshot.json` and uploads it to the `data` release of this repository. The crawl starts at the ranked players list, follows the tournaments on the home page to their teams and starting rosters, then opens every player page, one browser with at least `scraper.request-delay` between page loads. Run it weekly from a normal home connection (`scripts/com.cscomparison.scrape.plist` is a macOS launchd template). It cannot run on GitHub Actions or other cloud hosts: bo3.gg shows a Cloudflare challenge to datacenter IPs, so the scrape fails there.
- **API.** The Docker image runs with `scraper.source=remote`. It downloads the published snapshot on startup and every `scraper.refresh-interval` (6h in the image), keeps it in memory and serves it. If a download fails, the previous snapshot keeps being served. Until the first download succeeds it answers `503`.
- **Local development** runs both parts in one process (`scraper.source=browser`, the default): it scrapes on startup and then every `scraper.refresh-interval`, saving partial results every 25 players so there is data before the crawl ends.

## What is scraped per player

Profile (nickname, name, country, age, birth date, team, avatar, prize money), 6 month totals with win rates (tournaments, matches, maps, rounds), accuracy by body part, per-map stats, and the in-game stats of the last 15 matches in these categories: Main, Performance, AIM, Grenades, Primary devices, Pistols, Economy. In-game values are per round unless the unit says otherwise, and each comes with the average over all players.

## API

Base path `/api/v1`.

### `GET /players`

Summary of every player. Query params: `limit` (> 0), `team` (case-insensitive contains), `minScore` (>= 0).

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
  "fetchedAt": "2026-10-25T14:00:00Z"
}
```

`score` is bo3.gg's own rating, not HLTV rating.

### `GET /players/{slug-or-nickname}`

The summary fields plus `fullName`, `age`, `birthDate`, `prizeMoney`, `totals` (`tournaments`, `matches`, `maps`, `rounds`, each `{count, winRate}`), `accuracy`, `categories` (`[{name, stats: [{name, value, average, unit}]}]`) and `mapStats`. Unknown player gives `404`.

Errors share one JSON shape: `{timestamp, status, error, message, path}`.

## Configuration

Every property can also be set as an environment variable (`scraper.max-players` is `SCRAPER_MAX_PLAYERS`).

| Property | Default | |
|---|---|---|
| `scraper.base-url` | `https://bo3.gg` | |
| `scraper.source` | `browser` | `browser` scrapes in this process, `remote` downloads `scraper.snapshot-url` |
| `scraper.snapshot-url` | | where the scrape job published the snapshot (remote only) |
| `scraper.refresh-interval` | `24h` | time between scrapes or downloads |
| `scraper.snapshot-path` | `data/players-snapshot.json` | local copy that survives restarts |
| `scraper.allowed-origins` | `http://localhost:5173` | CORS, comma separated |
| `scraper.request-delay` | `2s` | minimum gap between page loads |
| `scraper.page-timeout` | `30s` | |
| `scraper.headless` | `true` | |
| `scraper.max-players` | `300` | |
| `scraper.max-tournaments` | `12` | tournaments used to discover teams |
| `scraper.min-players` | `20` | a scrape with fewer players is treated as failed |
| `scraper.refresh-enabled` | `true` | `false` turns scheduled scraping off |

## Build and run

Needs JDK 21. Scraping also needs Chrome installed.

```bash
./mvnw clean test
./mvnw spring-boot:run
```

Run a single scrape and exit, the way the script does:

```bash
java -jar target/cs-stats-api-*.jar --spring.profiles.active=scrape --scraper.snapshot-path=out/players-snapshot.json
```

Run the API image against the published data:

```bash
docker build -t cs-stats-api .
docker run -p 8080:8080 cs-stats-api
```

The live smoke test scrapes a few real players and is skipped by default:

```bash
./mvnw test -Dtest=LiveScrapeSmokeTest -DliveScrape=true
```

Parser tests run against saved copies of the real pages in `src/test/resources/fixtures/bo3`. If bo3.gg changes its markup, the live test fails first; re-save the pages and adjust the selectors in `infrastructure/bo3`.

## Notes

bo3.gg's `robots.txt` allows the pages this crawler visits and disallows `/api/` and the filtered list URLs, which are not used. Its terms may still restrict automated use, so keep the scrape infrequent and the request delay generous.

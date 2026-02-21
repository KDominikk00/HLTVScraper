package com.example.hltvscraper.hltv.application;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class PlayerStatsService {

    private final HltvPlayerSource playerSource;
    private final HltvScraperProperties properties;
    private final Clock clock;
    private final Object cacheLock = new Object();
    private volatile CacheEntry cacheEntry;

    @Autowired
    public PlayerStatsService(HltvPlayerSource playerSource, HltvScraperProperties properties) {
        this(playerSource, properties, Clock.systemUTC());
    }

    PlayerStatsService(HltvPlayerSource playerSource, HltvScraperProperties properties, Clock clock) {
        this.playerSource = playerSource;
        this.properties = properties;
        this.clock = clock;
    }

    public PlayerSearchResult searchPlayers(PlayerQuery query) {
        CacheEntry cache = loadCache(query.forceRefresh());
        Stream<PlayerStats> stream = cache.players().stream();

        if (query.team() != null && !query.team().isBlank()) {
            String teamFilter = query.team().trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(player -> player.teamName() != null
                    && player.teamName().toLowerCase(Locale.ROOT).contains(teamFilter));
        }

        if (query.minRating() != null) {
            stream = stream.filter(player -> player.rating() != null && player.rating() >= query.minRating());
        }

        if (query.limit() != null) {
            stream = stream.limit(query.limit());
        }

        List<PlayerStats> players = stream.toList();
        return new PlayerSearchResult(players, cache.fetchedAt(), cache.fromCache());
    }

    public PlayerStats getPlayerByNickname(String nickname, boolean forceRefresh) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname must not be blank");
        }

        String normalizedNickname = nickname.trim().toLowerCase(Locale.ROOT);
        return loadCache(forceRefresh).players().stream()
                .filter(player -> player.nickname() != null
                        && player.nickname().trim().toLowerCase(Locale.ROOT).equals(normalizedNickname))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + nickname));
    }

    private CacheEntry loadCache(boolean forceRefresh) {
        Instant now = clock.instant();
        CacheEntry current = cacheEntry;

        if (!forceRefresh && isValid(current, now)) {
            return current.asCached();
        }

        synchronized (cacheLock) {
            current = cacheEntry;

            if (!forceRefresh && isValid(current, now)) {
                return current.asCached();
            }

            try {
                List<PlayerStats> freshPlayers = List.copyOf(playerSource.fetchPlayers());
                if (freshPlayers.isEmpty()) {
                    throw new ScraperUnavailableException("Scraper returned no players");
                }

                CacheEntry refreshed = new CacheEntry(freshPlayers, now, false);
                cacheEntry = refreshed;
                return refreshed;
            } catch (RuntimeException exception) {
                if (current != null) {
                    return current.asCached();
                }

                throw new ScraperUnavailableException(
                        "Unable to load HLTV players and no cache is available",
                        exception
                );
            }
        }
    }

    private boolean isValid(CacheEntry entry, Instant now) {
        return entry != null && entry.fetchedAt().plus(properties.getCacheTtl()).isAfter(now);
    }

    private record CacheEntry(List<PlayerStats> players, Instant fetchedAt, boolean fromCache) {
        private CacheEntry asCached() {
            return new CacheEntry(players, fetchedAt, true);
        }
    }
}

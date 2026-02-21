package com.example.hltvscraper.hltv.application;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatsServiceTest {

    private MutableClock clock;
    private HltvScraperProperties properties;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
        properties = new HltvScraperProperties();
        properties.setCacheTtl(Duration.ofHours(2));
    }

    @Test
    void reusesCacheWithinTtl() {
        CountingPlayerSource source = new CountingPlayerSource(List.of(samplePlayer("donk", 1.32)));
        PlayerStatsService service = new PlayerStatsService(source, properties, clock);

        PlayerSearchResult first = service.searchPlayers(new PlayerQuery(false, null, null, null));
        clock.advance(Duration.ofMinutes(30));
        PlayerSearchResult second = service.searchPlayers(new PlayerQuery(false, null, null, null));

        assertEquals(1, source.calls.get());
        assertFalse(first.fromCache());
        assertTrue(second.fromCache());
    }

    @Test
    void forceRefreshBypassesCache() {
        CountingPlayerSource source = new CountingPlayerSource(List.of(samplePlayer("m0NESY", 1.28)));
        PlayerStatsService service = new PlayerStatsService(source, properties, clock);

        service.searchPlayers(new PlayerQuery(false, null, null, null));
        service.searchPlayers(new PlayerQuery(true, null, null, null));

        assertEquals(2, source.calls.get());
    }

    @Test
    void appliesFiltersAndLimit() {
        CountingPlayerSource source = new CountingPlayerSource(List.of(
                samplePlayer("ZywOo", 1.35),
                samplePlayer("frozen", 1.13),
                samplePlayer("flameZ", 1.10)
        ));
        PlayerStatsService service = new PlayerStatsService(source, properties, clock);

        PlayerSearchResult result = service.searchPlayers(new PlayerQuery(false, 1, "team", 1.2));

        assertEquals(1, result.players().size());
        assertEquals("ZywOo", result.players().get(0).nickname());
    }

    @Test
    void throwsWhenPlayerIsMissing() {
        CountingPlayerSource source = new CountingPlayerSource(List.of(samplePlayer("ropz", 1.20)));
        PlayerStatsService service = new PlayerStatsService(source, properties, clock);

        assertThrows(PlayerNotFoundException.class, () -> service.getPlayerByNickname("s1mple", false));
    }

    @Test
    void fallsBackToPreviousCacheWhenRefreshFails() {
        FlakyPlayerSource source = new FlakyPlayerSource();
        PlayerStatsService service = new PlayerStatsService(source, properties, clock);

        PlayerSearchResult first = service.searchPlayers(new PlayerQuery(false, null, null, null));
        clock.advance(Duration.ofHours(3));
        PlayerSearchResult second = service.searchPlayers(new PlayerQuery(false, null, null, null));

        assertEquals("NiKo", first.players().get(0).nickname());
        assertEquals("NiKo", second.players().get(0).nickname());
        assertTrue(second.fromCache());
    }

    private PlayerStats samplePlayer(String nickname, double rating) {
        return new PlayerStats(nickname, "Team", "logo", 100, 2000, 100, 1.20, rating);
    }

    private static class CountingPlayerSource implements HltvPlayerSource {
        private final List<PlayerStats> players;
        private final AtomicInteger calls = new AtomicInteger();

        private CountingPlayerSource(List<PlayerStats> players) {
            this.players = players;
        }

        @Override
        public List<PlayerStats> fetchPlayers() {
            calls.incrementAndGet();
            return players;
        }
    }

    private static class FlakyPlayerSource implements HltvPlayerSource {
        private boolean firstCall = true;

        @Override
        public List<PlayerStats> fetchPlayers() {
            if (firstCall) {
                firstCall = false;
                return List.of(new PlayerStats("NiKo", "Falcons", "logo", 50, 1000, 80, 1.15, 1.18));
            }

            throw new ScraperUnavailableException("HLTV request failed");
        }
    }

    private static class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}

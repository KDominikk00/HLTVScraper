package com.example.csstats.application;

import com.example.csstats.TestPlayers;
import com.example.csstats.domain.PlayerStats;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerStatsServiceTest {

    private static final Instant FETCHED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void returnsSnapshotTimestamp() {
        PlayerStatsService service = serviceWith(samplePlayer("donk", 6.8));

        PlayerSearchResult result = service.searchPlayers(new PlayerQuery(null, null, null));

        assertEquals(FETCHED_AT, result.fetchedAt());
        assertEquals(1, result.players().size());
    }

    @Test
    void appliesFiltersAndLimit() {
        PlayerStatsService service = serviceWith(
                samplePlayer("ZywOo", 7.1),
                samplePlayer("frozen", 5.8),
                samplePlayer("flameZ", 5.6)
        );

        PlayerSearchResult result = service.searchPlayers(new PlayerQuery(1, "team", 6.0));

        assertEquals(1, result.players().size());
        assertEquals("ZywOo", result.players().get(0).nickname());
    }

    @Test
    void findsPlayerBySlugOrNicknameIgnoringCase() {
        PlayerStatsService service = serviceWith(samplePlayer("m0NESY", 6.9));

        assertEquals("m0NESY", service.getPlayer("m0nesy").nickname());
        assertEquals("m0NESY", service.getPlayer("  M0NESY ").nickname());
    }

    @Test
    void throwsWhenPlayerIsMissing() {
        PlayerStatsService service = serviceWith(samplePlayer("ropz", 6.2));

        assertThrows(PlayerNotFoundException.class, () -> service.getPlayer("s1mple"));
    }

    @Test
    void throwsWhenNoSnapshotExistsYet() {
        PlayerStatsService service = new PlayerStatsService(new InMemoryStore(null));

        assertThrows(ScraperUnavailableException.class,
                () -> service.searchPlayers(new PlayerQuery(null, null, null)));
    }

    private PlayerStatsService serviceWith(PlayerStats... players) {
        return new PlayerStatsService(new InMemoryStore(new PlayerSnapshot(List.of(players), FETCHED_AT)));
    }

    private PlayerStats samplePlayer(String nickname, double score) {
        return TestPlayers.player(nickname, "Team", score);
    }

    static class InMemoryStore implements PlayerSnapshotStore {
        private PlayerSnapshot snapshot;

        InMemoryStore(PlayerSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Optional<PlayerSnapshot> load() {
            return Optional.ofNullable(snapshot);
        }

        @Override
        public void save(PlayerSnapshot snapshot) {
            this.snapshot = snapshot;
        }
    }
}

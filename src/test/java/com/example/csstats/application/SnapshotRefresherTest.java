package com.example.csstats.application;

import com.example.csstats.application.PlayerStatsServiceTest.InMemoryStore;
import com.example.csstats.domain.PlayerStats;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnapshotRefresherTest {

    private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void storesFreshPlayersWithCurrentTimestamp() {
        InMemoryStore store = new InMemoryStore(null);
        SnapshotRefresher refresher = new SnapshotRefresher(() -> List.of(player("NiKo")), store, clock);

        refresher.refresh();

        PlayerSnapshot snapshot = store.load().orElseThrow();
        assertEquals(NOW, snapshot.fetchedAt());
        assertEquals("NiKo", snapshot.players().get(0).nickname());
    }

    @Test
    void keepsPreviousSnapshotWhenScrapeFails() {
        PlayerSnapshot old = new PlayerSnapshot(List.of(player("ropz")), Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryStore store = new InMemoryStore(old);
        SnapshotRefresher refresher = new SnapshotRefresher(() -> {
            throw new ScraperUnavailableException("blocked");
        }, store, clock);

        refresher.refresh();

        assertEquals(old, store.load().orElseThrow());
    }

    private PlayerStats player(String nickname) {
        return new PlayerStats(nickname, "Team", "logo", 50, 1000, 80, 1.15, 1.18);
    }
}

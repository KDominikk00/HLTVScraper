package com.example.csstats.application;

import com.example.csstats.TestPlayers;
import com.example.csstats.application.PlayerStatsServiceTest.InMemoryStore;
import com.example.csstats.domain.PlayerStats;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotRefresherTest {

    private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void storesTheFetchedSnapshot() {
        InMemoryStore store = new InMemoryStore(null);
        SnapshotRefresher refresher = new SnapshotRefresher(
                checkpoint -> new PlayerSnapshot(List.of(player("NiKo")), NOW), store, clock);

        refresher.refresh();

        PlayerSnapshot snapshot = store.load().orElseThrow();
        assertEquals(NOW, snapshot.fetchedAt());
        assertFalse(snapshot.partial());
        assertEquals("NiKo", snapshot.players().get(0).nickname());
    }

    @Test
    void keepsPreviousSnapshotWhenScrapeFails() {
        PlayerSnapshot old = new PlayerSnapshot(List.of(player("ropz")), Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryStore store = new InMemoryStore(old);
        SnapshotRefresher refresher = new SnapshotRefresher(checkpoint -> {
            throw new ScraperUnavailableException("blocked");
        }, store, clock);

        refresher.refresh();

        assertEquals(old, store.load().orElseThrow());
    }

    @Test
    void refreshOrThrowReportsTheFailure() {
        SnapshotRefresher refresher = new SnapshotRefresher(checkpoint -> {
            throw new ScraperUnavailableException("blocked");
        }, new InMemoryStore(null), clock);

        assertThrows(ScraperUnavailableException.class, refresher::refreshOrThrow);
    }

    @Test
    void savesPartialResultsOnlyWhileThereIsNoSnapshot() {
        InMemoryStore store = new InMemoryStore(null);
        SnapshotRefresher refresher = new SnapshotRefresher(checkpoint -> {
            checkpoint.accept(List.of(player("a")));
            checkpoint.accept(List.of(player("a"), player("b")));
            throw new ScraperUnavailableException("blocked halfway");
        }, store, clock);

        refresher.refresh();

        PlayerSnapshot saved = store.load().orElseThrow();
        assertEquals(1, saved.players().size());
        assertTrue(saved.partial());
    }

    private PlayerStats player(String nickname) {
        return TestPlayers.player(nickname, "Team", 6.0);
    }
}

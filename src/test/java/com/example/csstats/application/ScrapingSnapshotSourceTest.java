package com.example.csstats.application;

import com.example.csstats.TestPlayers;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ScrapingSnapshotSourceTest {

    @Test
    void stampsTheScrapedPlayersWithTheCurrentTime() {
        Instant now = Instant.parse("2026-02-01T12:00:00Z");
        ScrapingSnapshotSource source = new ScrapingSnapshotSource(
                checkpoint -> List.of(TestPlayers.player("NiKo", "Falcons", 6.2)),
                Clock.fixed(now, ZoneOffset.UTC));

        PlayerSnapshot snapshot = source.fetchSnapshot(partial -> { });

        assertEquals(now, snapshot.fetchedAt());
        assertFalse(snapshot.partial());
        assertEquals("NiKo", snapshot.players().get(0).nickname());
    }
}

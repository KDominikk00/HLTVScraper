package com.example.csstats.infrastructure;

import com.example.csstats.TestPlayers;
import com.example.csstats.config.ScraperProperties;
import com.example.csstats.application.PlayerSnapshot;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSnapshotStoreTest {

    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @TempDir
    Path tempDir;

    @Test
    void startsEmptyWhenNoFileExists() {
        assertTrue(storeAt(tempDir.resolve("missing/snapshot.json")).load().isEmpty());
    }

    @Test
    void survivesRestart() {
        Path file = tempDir.resolve("nested/snapshot.json");
        PlayerSnapshot snapshot = new PlayerSnapshot(
                List.of(TestPlayers.player("donk", "Spirit", 6.8)),
                Instant.parse("2026-01-01T00:00:00Z")
        );

        storeAt(file).save(snapshot);

        assertEquals(snapshot, storeAt(file).load().orElseThrow());
    }

    @Test
    void ignoresCorruptFile() throws Exception {
        Path file = tempDir.resolve("snapshot.json");
        Files.writeString(file, "{ not json");

        assertTrue(storeAt(file).load().isEmpty());
    }

    private FileSnapshotStore storeAt(Path file) {
        ScraperProperties properties = new ScraperProperties(
                URI.create("https://bo3.gg"), Duration.ofHours(24), file, List.of("http://localhost"),
                Duration.ZERO, Duration.ofSeconds(1), true, 10, 1, 1, null, null);
        return new FileSnapshotStore(objectMapper, properties);
    }
}

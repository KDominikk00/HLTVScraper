package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.PlayerSnapshot;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSnapshotStoreTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
                List.of(new PlayerStats("donk", "Spirit", "logo", 90, 1900, 150, 1.30, 1.35)),
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
        HltvScraperProperties properties = new HltvScraperProperties();
        properties.setSnapshotPath(file);
        return new FileSnapshotStore(objectMapper, properties);
    }
}

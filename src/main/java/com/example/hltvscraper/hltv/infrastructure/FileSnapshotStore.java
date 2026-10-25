package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.PlayerSnapshot;
import com.example.hltvscraper.hltv.application.PlayerSnapshotStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/** Keeps the latest snapshot in memory and mirrors it to a JSON file so it survives restarts. */
@Component
public class FileSnapshotStore implements PlayerSnapshotStore {

    private static final Logger logger = LoggerFactory.getLogger(FileSnapshotStore.class);

    private final ObjectMapper objectMapper;
    private final Path file;
    private volatile PlayerSnapshot current;

    public FileSnapshotStore(ObjectMapper objectMapper, HltvScraperProperties properties) {
        this.objectMapper = objectMapper;
        this.file = properties.getSnapshotPath();
        this.current = readFromDisk();
    }

    @Override
    public Optional<PlayerSnapshot> load() {
        return Optional.ofNullable(current);
    }

    @Override
    public void save(PlayerSnapshot snapshot) {
        current = snapshot;
        try {
            writeToDisk(snapshot);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not write snapshot to " + file, exception);
        }
    }

    private PlayerSnapshot readFromDisk() {
        if (!Files.exists(file)) {
            return null;
        }
        try {
            PlayerSnapshot snapshot = objectMapper.readValue(file.toFile(), PlayerSnapshot.class);
            logger.info("Loaded snapshot of {} players from {}", snapshot.players().size(), file);
            return snapshot;
        } catch (IOException | RuntimeException exception) {
            logger.warn("Ignoring unreadable snapshot file {}: {}", file, exception.getMessage());
            return null;
        }
    }

    private void writeToDisk(PlayerSnapshot snapshot) throws IOException {
        Path absolute = file.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Path temp = Files.createTempFile(absolute.getParent(), "snapshot-", ".tmp");
        try {
            objectMapper.writeValue(temp.toFile(), snapshot);
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}

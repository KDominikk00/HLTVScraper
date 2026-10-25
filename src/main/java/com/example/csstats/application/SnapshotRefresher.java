package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class SnapshotRefresher {

    private static final Logger logger = LoggerFactory.getLogger(SnapshotRefresher.class);

    private final SnapshotSource snapshotSource;
    private final PlayerSnapshotStore snapshotStore;
    private final Clock clock;

    public SnapshotRefresher(SnapshotSource snapshotSource, PlayerSnapshotStore snapshotStore, Clock clock) {
        this.snapshotSource = snapshotSource;
        this.snapshotStore = snapshotStore;
        this.clock = clock;
    }

    /** Fetches a new snapshot. On failure the previous snapshot stays in place. */
    public void refresh() {
        try {
            refreshOrThrow();
        } catch (RuntimeException exception) {
            logger.error("Refresh failed, keeping previous snapshot", exception);
        }
    }

    public void refreshOrThrow() {
        PlayerSnapshot snapshot = snapshotSource.fetchSnapshot(this::saveIfEmpty);
        snapshotStore.save(snapshot);
        logger.info("Stored snapshot with {} players", snapshot.players().size());
    }

    /** Lets a fresh deployment serve partial data while its first long scrape is still running. */
    private void saveIfEmpty(List<PlayerStats> partial) {
        if (snapshotStore.load().isEmpty() && !partial.isEmpty()) {
            snapshotStore.save(new PlayerSnapshot(partial, clock.instant(), true));
        }
    }
}

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

    private final PlayerSource playerSource;
    private final PlayerSnapshotStore snapshotStore;
    private final Clock clock;

    public SnapshotRefresher(PlayerSource playerSource, PlayerSnapshotStore snapshotStore, Clock clock) {
        this.playerSource = playerSource;
        this.snapshotStore = snapshotStore;
        this.clock = clock;
    }

    /** Scrapes fresh data. On failure the previous snapshot stays in place. */
    public void refresh() {
        try {
            List<PlayerStats> players = playerSource.fetchPlayers(this::saveIfEmpty);
            snapshotStore.save(new PlayerSnapshot(players, clock.instant()));
            logger.info("Stored snapshot with {} players", players.size());
        } catch (RuntimeException exception) {
            logger.error("Refresh failed, keeping previous snapshot", exception);
        }
    }

    /** Lets a fresh deployment serve partial data while its first long scrape is still running. */
    private void saveIfEmpty(List<PlayerStats> partial) {
        if (snapshotStore.load().isEmpty() && !partial.isEmpty()) {
            snapshotStore.save(new PlayerSnapshot(partial, clock.instant()));
        }
    }
}

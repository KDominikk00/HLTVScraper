package com.example.hltvscraper.hltv.application;

import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class SnapshotRefresher {

    private static final Logger logger = LoggerFactory.getLogger(SnapshotRefresher.class);

    private final HltvPlayerSource playerSource;
    private final PlayerSnapshotStore snapshotStore;
    private final Clock clock;

    public SnapshotRefresher(HltvPlayerSource playerSource, PlayerSnapshotStore snapshotStore, Clock clock) {
        this.playerSource = playerSource;
        this.snapshotStore = snapshotStore;
        this.clock = clock;
    }

    /** Scrapes fresh data. On failure the previous snapshot stays in place. */
    public void refresh() {
        try {
            List<PlayerStats> players = playerSource.fetchPlayers();
            snapshotStore.save(new PlayerSnapshot(players, clock.instant()));
            logger.info("Stored snapshot with {} players", players.size());
        } catch (RuntimeException exception) {
            logger.error("Refresh failed, keeping previous snapshot", exception);
        }
    }
}

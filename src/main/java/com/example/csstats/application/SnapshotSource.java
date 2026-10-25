package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;

import java.util.List;
import java.util.function.Consumer;

/** Where complete snapshots come from: a live scrape, or one that was scraped elsewhere. */
public interface SnapshotSource {

    /**
     * @param checkpoint receives the players collected so far while a long scrape is still running
     */
    PlayerSnapshot fetchSnapshot(Consumer<List<PlayerStats>> checkpoint);
}

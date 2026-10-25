package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;

import java.time.Instant;
import java.util.List;

/** @param partial true for a checkpoint taken while a scrape is still running */
public record PlayerSnapshot(List<PlayerStats> players, Instant fetchedAt, boolean partial) {

    public PlayerSnapshot {
        players = List.copyOf(players);
    }

    public PlayerSnapshot(List<PlayerStats> players, Instant fetchedAt) {
        this(players, fetchedAt, false);
    }
}

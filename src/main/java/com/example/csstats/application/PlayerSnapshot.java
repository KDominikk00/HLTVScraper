package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;

import java.time.Instant;
import java.util.List;

public record PlayerSnapshot(List<PlayerStats> players, Instant fetchedAt) {

    public PlayerSnapshot {
        players = List.copyOf(players);
    }
}

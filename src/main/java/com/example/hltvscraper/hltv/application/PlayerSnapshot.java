package com.example.hltvscraper.hltv.application;

import com.example.hltvscraper.hltv.domain.PlayerStats;

import java.time.Instant;
import java.util.List;

public record PlayerSnapshot(List<PlayerStats> players, Instant fetchedAt) {

    public PlayerSnapshot {
        players = List.copyOf(players);
    }
}

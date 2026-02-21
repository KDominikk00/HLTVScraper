package com.example.hltvscraper.hltv.application;

import com.example.hltvscraper.hltv.domain.PlayerStats;

import java.time.Instant;
import java.util.List;

public record PlayerSearchResult(
        List<PlayerStats> players,
        Instant fetchedAt,
        boolean fromCache
) {
}

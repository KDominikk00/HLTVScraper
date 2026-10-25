package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;

import java.time.Instant;
import java.util.List;

public record PlayerSearchResult(List<PlayerStats> players, Instant fetchedAt) {
}

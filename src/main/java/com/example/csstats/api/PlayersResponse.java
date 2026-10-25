package com.example.csstats.api;

import java.time.Instant;
import java.util.List;

public record PlayersResponse(List<PlayerSummaryResponse> players, int count, Instant fetchedAt) {
}

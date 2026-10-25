package com.example.csstats.api;

import java.time.Instant;
import java.util.List;

public record PlayersResponse(List<PlayerResponse> players, int count, Instant fetchedAt) {
}

package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.hltv.domain.PlayerStats;

import java.util.List;

public interface HltvPlayerSourceClient {
    String id();

    List<PlayerStats> fetchPlayers();
}

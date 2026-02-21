package com.example.hltvscraper.hltv.application;

import com.example.hltvscraper.hltv.domain.PlayerStats;

import java.util.List;

public interface HltvPlayerSource {
    List<PlayerStats> fetchPlayers();
}

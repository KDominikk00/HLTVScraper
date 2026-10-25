package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;

import java.util.List;

public interface PlayerSource {
    List<PlayerStats> fetchPlayers();
}

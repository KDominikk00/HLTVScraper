package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;

import java.util.List;
import java.util.function.Consumer;

public interface PlayerSource {

    /**
     * Scrapes all players. A scrape can take a long time, so {@code checkpoint} is handed
     * the players collected so far every now and then.
     */
    List<PlayerStats> fetchPlayers(Consumer<List<PlayerStats>> checkpoint);
}

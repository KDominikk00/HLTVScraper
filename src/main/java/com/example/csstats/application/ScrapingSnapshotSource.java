package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.function.Consumer;

/** Scrapes the site in this process. */
@Component
@ConditionalOnProperty(name = "scraper.source", havingValue = "browser", matchIfMissing = true)
public class ScrapingSnapshotSource implements SnapshotSource {

    private final PlayerSource playerSource;
    private final Clock clock;

    public ScrapingSnapshotSource(PlayerSource playerSource, Clock clock) {
        this.playerSource = playerSource;
        this.clock = clock;
    }

    @Override
    public PlayerSnapshot fetchSnapshot(Consumer<List<PlayerStats>> checkpoint) {
        List<PlayerStats> players = playerSource.fetchPlayers(checkpoint);
        return new PlayerSnapshot(players, clock.instant());
    }
}

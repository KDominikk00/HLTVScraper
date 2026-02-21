package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfSystemProperty(named = "liveScrape", matches = "true")
class LiveScrapeSmokeTest {

    @Test
    void fetchesPlayersFromLiveHltvPage() {
        HltvScraperProperties properties = new HltvScraperProperties();
        properties.setRequestTimeout(Duration.ofSeconds(30));
        properties.setSourceOrder(List.of("jsoup", "selenium"));

        HltvPlayerDocumentParser parser = new HltvPlayerDocumentParser();
        ConfiguredHltvPlayerSource source = new ConfiguredHltvPlayerSource(
                List.of(
                        new JsoupHltvPlayerSource(properties, parser),
                        new SeleniumHltvPlayerSource(properties, parser)
                ),
                properties
        );

        List<PlayerStats> players = source.fetchPlayers();

        assertFalse(players.isEmpty(), "Expected live scrape to return at least one player");
        assertNotNull(players.get(0).nickname());
    }
}

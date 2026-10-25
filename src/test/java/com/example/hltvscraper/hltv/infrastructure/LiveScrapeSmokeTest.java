package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfSystemProperty(named = "liveScrape", matches = "true")
class LiveScrapeSmokeTest {

    @Test
    void fetchesPlayersFromLiveHltvPage() {
        HltvScraperProperties properties = new HltvScraperProperties();
        properties.setSeleniumHeadless(!"false".equals(System.getProperty("scrapeHeadless")));

        ScrapedHltvPlayerSource source = new ScrapedHltvPlayerSource(
                new SeleniumPageFetcher(properties),
                new HltvPlayerDocumentParser(),
                properties
        );

        List<PlayerStats> players = source.fetchPlayers();

        assertFalse(players.isEmpty(), "Expected live scrape to return at least one player");
        assertNotNull(players.get(0).nickname());
    }
}

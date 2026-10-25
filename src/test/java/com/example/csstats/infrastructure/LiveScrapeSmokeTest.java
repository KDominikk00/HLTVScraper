package com.example.csstats.infrastructure;

import com.example.csstats.config.ScraperProperties;
import com.example.csstats.domain.PlayerStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfSystemProperty(named = "liveScrape", matches = "true")
class LiveScrapeSmokeTest {

    @Test
    void fetchesPlayersFromLivePage() {
        ScraperProperties properties = new ScraperProperties();
        properties.setSeleniumHeadless(!"false".equals(System.getProperty("scrapeHeadless")));

        ScrapedPlayerSource source = new ScrapedPlayerSource(
                new SeleniumPageFetcher(properties),
                new PlayerDocumentParser(),
                properties
        );

        List<PlayerStats> players = source.fetchPlayers();

        assertFalse(players.isEmpty(), "Expected live scrape to return at least one player");
        assertNotNull(players.get(0).nickname());
    }
}

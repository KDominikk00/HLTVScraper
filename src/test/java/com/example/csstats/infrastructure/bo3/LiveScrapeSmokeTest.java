package com.example.csstats.infrastructure.bo3;

import com.example.csstats.config.ScraperProperties;
import com.example.csstats.domain.PlayerStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Hits the real site, so it only runs with -DliveScrape=true. Scrapes a handful of players. */
@EnabledIfSystemProperty(named = "liveScrape", matches = "true")
class LiveScrapeSmokeTest {

    @Test
    void scrapesPlayersFromLiveSite() {
        ScraperProperties properties = new ScraperProperties(
                URI.create("https://bo3.gg"), Duration.ofHours(24), Path.of("target/live-snapshot.json"),
                List.of("http://localhost"), Duration.ofSeconds(2), Duration.ofSeconds(30),
                !"false".equals(System.getProperty("scrapeHeadless")), 3, 1, 1, null, null);

        Bo3PlayerSource source = new Bo3PlayerSource(
                new SeleniumBrowserSessionFactory(properties),
                new Bo3LinkParser(),
                new Bo3PlayerPageParser(),
                properties
        );

        List<PlayerStats> players = source.fetchPlayers(partial -> { });

        assertFalse(players.isEmpty(), "Expected the live scrape to return players");
        PlayerStats first = players.get(0);
        assertNotNull(first.nickname());
        assertNotNull(first.score());
        assertNotNull(first.rounds());
        assertFalse(first.categories().isEmpty());
        System.out.println("LIVE " + first.nickname() + " categories=" + first.categories().stream().map(c -> c.name() + ":" + c.stats().size()).toList());
    }
}

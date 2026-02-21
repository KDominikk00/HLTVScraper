package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.ScraperUnavailableException;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguredHltvPlayerSourceTest {

    @Test
    void fallsBackToSecondSourceWhenFirstFails() {
        HltvScraperProperties properties = new HltvScraperProperties();
        properties.setSourceOrder(List.of("first", "second"));

        HltvPlayerSourceClient first = new StubSource("first", true, List.of());
        HltvPlayerSourceClient second = new StubSource(
                "second",
                false,
                List.of(new PlayerStats("m0NESY", "G2", "logo", 50, 1000, 90, 1.25, 1.22))
        );

        ConfiguredHltvPlayerSource source = new ConfiguredHltvPlayerSource(List.of(first, second), properties);

        List<PlayerStats> players = source.fetchPlayers();

        assertEquals(1, players.size());
        assertEquals("m0NESY", players.get(0).nickname());
    }

    @Test
    void throwsWhenAllSourcesFail() {
        HltvScraperProperties properties = new HltvScraperProperties();
        properties.setSourceOrder(List.of("first", "second"));

        HltvPlayerSourceClient first = new StubSource("first", true, List.of());
        HltvPlayerSourceClient second = new StubSource("second", true, List.of());

        ConfiguredHltvPlayerSource source = new ConfiguredHltvPlayerSource(List.of(first, second), properties);

        assertThrows(ScraperUnavailableException.class, source::fetchPlayers);
    }

    private static class StubSource implements HltvPlayerSourceClient {
        private final String id;
        private final boolean fails;
        private final List<PlayerStats> players;

        private StubSource(String id, boolean fails, List<PlayerStats> players) {
            this.id = id;
            this.fails = fails;
            this.players = new ArrayList<>(players);
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public List<PlayerStats> fetchPlayers() {
            if (fails) {
                throw new ScraperUnavailableException("Source failed: " + id);
            }
            return players;
        }
    }
}

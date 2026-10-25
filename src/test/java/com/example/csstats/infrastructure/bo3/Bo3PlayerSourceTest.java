package com.example.csstats.infrastructure.bo3;

import com.example.csstats.application.ScraperUnavailableException;
import com.example.csstats.config.ScraperProperties;
import com.example.csstats.domain.PlayerStats;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bo3PlayerSourceTest {

    private final List<String> loaded = new ArrayList<>();

    @Test
    void keepsDiscoveringTeamsWhenOnePageFails() {
        // The first team on the tournament page is not loadable, like the /teams/earnings rankings page was.
        Bo3PlayerSource source = source(path -> {
            if (path.equals("/teams/9z")) {
                throw new ScraperUnavailableException("timed out");
            }
            return page(path);
        }, 300);

        List<PlayerStats> players = source.fetchPlayers(partial -> { });

        Set<String> slugs = players.stream().map(PlayerStats::slug).collect(Collectors.toSet());
        assertEquals(52, slugs.size(), "50 ranked players plus the 2 starters of the loaded roster who are not ranked");
        assertTrue(slugs.containsAll(List.of("mezil", "jl-cs-go")));
        assertTrue(loaded.contains("/teams/vitality"));
    }

    @Test
    void stopsDiscoveryOnceEnoughPlayersAreKnown() {
        Bo3PlayerSource source = source(Bo3PlayerSourceTest::page, 50);

        List<PlayerStats> players = source.fetchPlayers(partial -> { });

        assertEquals(50, players.size());
        assertTrue(loaded.stream().noneMatch(path -> path.startsWith("/teams/")));
    }

    @Test
    void failsWhenTooFewPlayersCouldBeScraped() {
        Bo3PlayerSource source = source(path -> {
            if (path.startsWith("/players/") && !path.equals("/players")) {
                throw new ScraperUnavailableException("blocked");
            }
            return page(path);
        }, 300);

        assertThrows(ScraperUnavailableException.class, () -> source.fetchPlayers(partial -> { }));
    }

    private Bo3PlayerSource source(Function<String, Document> pages, int maxPlayers) {
        ScraperProperties properties = new ScraperProperties(
                URI.create("https://bo3.gg"), Duration.ofHours(24), Path.of("unused.json"), List.of("http://localhost"),
                Duration.ZERO, Duration.ofSeconds(1), true, maxPlayers, 12, 20, "browser", null);
        BrowserSessionFactory factory = () -> new FakeSession(pages);
        return new Bo3PlayerSource(factory, new Bo3LinkParser(), new Bo3PlayerPageParser(), properties);
    }

    private static Document page(String path) {
        if (path.equals("/")) return Fixtures.page("home");
        if (path.equals("/players")) return Fixtures.page("players");
        if (path.startsWith("/tournaments/")) return Fixtures.page("tournament");
        if (path.startsWith("/teams/")) return Fixtures.page("team");
        if (path.startsWith("/players/")) return Fixtures.page("player");
        throw new ScraperUnavailableException("unexpected page " + path);
    }

    private class FakeSession implements BrowserSession {
        private final Function<String, Document> pages;
        private Document current;

        FakeSession(Function<String, Document> pages) {
            this.pages = pages;
        }

        @Override
        public Document load(URI url, String... readySelectors) {
            loaded.add(url.getPath());
            current = pages.apply(url.getPath());
            return current;
        }

        @Override
        public Document selectOption(String widgetSelector, String optionText) {
            return current;
        }

        @Override
        public void close() {
        }
    }
}

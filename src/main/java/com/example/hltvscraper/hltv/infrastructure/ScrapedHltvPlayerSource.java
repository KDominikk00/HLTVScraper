package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.HltvPlayerSource;
import com.example.hltvscraper.hltv.application.ScraperUnavailableException;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class ScrapedHltvPlayerSource implements HltvPlayerSource {

    private static final Logger logger = LoggerFactory.getLogger(ScrapedHltvPlayerSource.class);

    private final PageFetcher pageFetcher;
    private final HltvPlayerDocumentParser parser;
    private final URI playersUrl;

    public ScrapedHltvPlayerSource(
            PageFetcher pageFetcher,
            HltvPlayerDocumentParser parser,
            HltvScraperProperties properties
    ) {
        this.pageFetcher = pageFetcher;
        this.parser = parser;
        this.playersUrl = properties.getPlayersUrl();
    }

    @Override
    public List<PlayerStats> fetchPlayers() {
        String html = pageFetcher.fetchHtml(playersUrl);
        List<PlayerStats> players = parser.parse(Jsoup.parse(html, playersUrl.toString()));
        if (players.isEmpty()) {
            throw new ScraperUnavailableException("No players found on " + playersUrl + " (blocked or layout changed?)");
        }

        logger.info("Scraped {} players", players.size());
        return players;
    }
}

package com.example.csstats.infrastructure;

import com.example.csstats.config.ScraperProperties;
import com.example.csstats.application.PlayerSource;
import com.example.csstats.application.ScraperUnavailableException;
import com.example.csstats.domain.PlayerStats;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class ScrapedPlayerSource implements PlayerSource {

    private static final Logger logger = LoggerFactory.getLogger(ScrapedPlayerSource.class);

    private final PageFetcher pageFetcher;
    private final PlayerDocumentParser parser;
    private final URI playersUrl;

    public ScrapedPlayerSource(
            PageFetcher pageFetcher,
            PlayerDocumentParser parser,
            ScraperProperties properties
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

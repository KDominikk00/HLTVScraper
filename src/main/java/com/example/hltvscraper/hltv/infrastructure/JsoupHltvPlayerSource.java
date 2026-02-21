package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.ScraperUnavailableException;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JsoupHltvPlayerSource implements HltvPlayerSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(JsoupHltvPlayerSource.class);

    private final HltvScraperProperties properties;
    private final HltvPlayerDocumentParser parser;

    public JsoupHltvPlayerSource(HltvScraperProperties properties, HltvPlayerDocumentParser parser) {
        this.properties = properties;
        this.parser = parser;
    }

    @Override
    public String id() {
        return "jsoup";
    }

    @Override
    public List<PlayerStats> fetchPlayers() {
        try {
            Document document = Jsoup.connect(properties.getPlayersUrl().toString())
                    .userAgent(properties.getUserAgent())
                    .timeout(Math.toIntExact(properties.getRequestTimeout().toMillis()))
                    .get();

            List<PlayerStats> players = parser.parse(document);
            logger.info("Fetched {} HLTV players", players.size());
            return players;
        } catch (IOException exception) {
            throw new ScraperUnavailableException("Failed to scrape HLTV players", exception);
        }
    }
}

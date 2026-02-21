package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.HltvPlayerSource;
import com.example.hltvscraper.hltv.application.ScraperUnavailableException;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Primary
public class ConfiguredHltvPlayerSource implements HltvPlayerSource {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguredHltvPlayerSource.class);

    private final Map<String, HltvPlayerSourceClient> sourceClients;
    private final HltvScraperProperties properties;

    public ConfiguredHltvPlayerSource(List<HltvPlayerSourceClient> sourceClients, HltvScraperProperties properties) {
        this.sourceClients = sourceClients.stream()
                .collect(Collectors.toMap(client -> client.id().toLowerCase(Locale.ROOT), Function.identity()));
        this.properties = properties;
    }

    @Override
    public List<PlayerStats> fetchPlayers() {
        RuntimeException lastException = null;

        for (String sourceId : properties.getSourceOrder()) {
            String normalizedId = sourceId.toLowerCase(Locale.ROOT);
            HltvPlayerSourceClient sourceClient = sourceClients.get(normalizedId);
            if (sourceClient == null) {
                logger.warn("Skipping unknown source id '{}'", sourceId);
                continue;
            }

            try {
                List<PlayerStats> players = sourceClient.fetchPlayers();
                if (!players.isEmpty()) {
                    logger.info("Successfully fetched {} players with source '{}'", players.size(), sourceClient.id());
                    return players;
                }

                lastException = new ScraperUnavailableException(
                        "Source '%s' returned no players".formatted(sourceClient.id())
                );
            } catch (RuntimeException exception) {
                logger.warn("Source '{}' failed: {}", sourceClient.id(), exception.getMessage());
                lastException = exception;
            }
        }

        throw new ScraperUnavailableException(
                "All configured HLTV sources failed: " + properties.getSourceOrder(),
                lastException
        );
    }
}

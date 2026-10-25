package com.example.csstats.infrastructure;

import com.example.csstats.application.PlayerSnapshot;
import com.example.csstats.application.ScraperUnavailableException;
import com.example.csstats.application.SnapshotSource;
import com.example.csstats.config.ScraperProperties;
import com.example.csstats.domain.PlayerStats;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/** Downloads a snapshot that a scrape job published, so this process never needs a browser. */
@Component
@ConditionalOnProperty(name = "scraper.source", havingValue = "remote")
public class RemoteSnapshotSource implements SnapshotSource {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper;
    private final URI snapshotUrl;
    private final Duration timeout;
    private final int minPlayers;

    public RemoteSnapshotSource(ObjectMapper objectMapper, ScraperProperties properties) {
        if (properties.snapshotUrl() == null) {
            throw new IllegalStateException("scraper.snapshot-url must be set when scraper.source is remote");
        }
        this.objectMapper = objectMapper;
        this.snapshotUrl = properties.snapshotUrl();
        this.timeout = properties.pageTimeout();
        this.minPlayers = properties.minPlayers();
    }

    @Override
    public PlayerSnapshot fetchSnapshot(Consumer<List<PlayerStats>> checkpoint) {
        HttpRequest request = HttpRequest.newBuilder(snapshotUrl).timeout(timeout).GET().build();
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw new ScraperUnavailableException("Snapshot download failed with HTTP " + response.statusCode());
                }
                PlayerSnapshot snapshot = objectMapper.readValue(body, PlayerSnapshot.class);
                if (snapshot.partial() || snapshot.players().size() < minPlayers) {
                    throw new ScraperUnavailableException(
                            "Downloaded snapshot is incomplete (%d players)".formatted(snapshot.players().size()));
                }
                return snapshot;
            }
        } catch (IOException exception) {
            throw new ScraperUnavailableException("Could not download snapshot from " + snapshotUrl, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ScraperUnavailableException("Interrupted while downloading the snapshot", exception);
        }
    }
}

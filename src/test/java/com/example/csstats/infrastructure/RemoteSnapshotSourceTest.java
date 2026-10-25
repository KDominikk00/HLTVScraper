package com.example.csstats.infrastructure;

import com.example.csstats.TestPlayers;
import com.example.csstats.application.PlayerSnapshot;
import com.example.csstats.application.ScraperUnavailableException;
import com.example.csstats.config.ScraperProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemoteSnapshotSourceTest {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void downloadsASnapshotAndKeepsItsTimestamp() {
        PlayerSnapshot published = new PlayerSnapshot(
                List.of(TestPlayers.player("donk", "Spirit", 6.8)), Instant.parse("2026-02-01T00:00:00Z"));
        serve("/snapshot.json", 200, mapper.writeValueAsString(published));

        PlayerSnapshot downloaded = source(1).fetchSnapshot(partial -> { });

        assertEquals(published, downloaded);
    }

    @Test
    void rejectsServerErrors() {
        serve("/snapshot.json", 404, "not found");

        assertThrows(ScraperUnavailableException.class, () -> source(1).fetchSnapshot(partial -> { }));
    }

    @Test
    void rejectsPartialSnapshots() {
        PlayerSnapshot partial = new PlayerSnapshot(
                List.of(TestPlayers.player("donk", "Spirit", 6.8)), Instant.parse("2026-02-01T00:00:00Z"), true);
        serve("/snapshot.json", 200, mapper.writeValueAsString(partial));

        assertThrows(ScraperUnavailableException.class, () -> source(1).fetchSnapshot(ignored -> { }));
    }

    @Test
    void rejectsSnapshotsWithTooFewPlayers() {
        PlayerSnapshot small = new PlayerSnapshot(
                List.of(TestPlayers.player("donk", "Spirit", 6.8)), Instant.parse("2026-02-01T00:00:00Z"));
        serve("/snapshot.json", 200, mapper.writeValueAsString(small));

        assertThrows(ScraperUnavailableException.class, () -> source(5).fetchSnapshot(ignored -> { }));
    }

    private RemoteSnapshotSource source(int minPlayers) {
        URI url = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/snapshot.json");
        ScraperProperties properties = new ScraperProperties(
                URI.create("https://bo3.gg"), Duration.ofHours(6), Path.of("unused.json"), List.of("http://localhost"),
                Duration.ZERO, Duration.ofSeconds(5), true, 10, 1, minPlayers, "remote", url);
        return new RemoteSnapshotSource(mapper, properties);
    }

    private void serve(String path, int status, String body) {
        server.createContext(path, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }
}

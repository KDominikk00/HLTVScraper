package com.example.csstats.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "scraper")
@Validated
public record ScraperProperties(
        @DefaultValue("https://bo3.gg") @NotNull URI baseUrl,
        @DefaultValue("24h") @NotNull Duration refreshInterval,
        @DefaultValue("data/players-snapshot.json") @NotNull Path snapshotPath,
        @DefaultValue("http://localhost:5173") @NotEmpty List<String> allowedOrigins,
        @DefaultValue("2s") @NotNull Duration requestDelay,
        @DefaultValue("30s") @NotNull Duration pageTimeout,
        @DefaultValue("true") boolean headless,
        @DefaultValue("300") @Positive int maxPlayers,
        @DefaultValue("12") @Positive int maxTournaments,
        @DefaultValue("20") @Positive int minPlayers,
        @DefaultValue("browser") @Pattern(regexp = "browser|remote") String source,
        URI snapshotUrl
) {
}

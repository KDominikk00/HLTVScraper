package com.example.hltvscraper.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "hltv.scraper")
@Validated
public class HltvScraperProperties {

    @NotNull
    private URI playersUrl = URI.create("https://www.hltv.org/stats/players");

    @NotNull
    private Duration requestTimeout = Duration.ofSeconds(20);

    @NotNull
    private Duration cacheTtl = Duration.ofHours(6);

    @NotBlank
    private String userAgent =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    @NotEmpty
    private List<String> allowedOrigins =
            new ArrayList<>(List.of("http://localhost:5173"));

    @NotEmpty
    private List<String> sourceOrder =
            new ArrayList<>(List.of("jsoup", "selenium"));

    @NotNull
    private Duration seleniumWaitTimeout = Duration.ofSeconds(30);

    private boolean seleniumHeadless = true;

    public URI getPlayersUrl() {
        return playersUrl;
    }

    public void setPlayersUrl(URI playersUrl) {
        this.playersUrl = playersUrl;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = new ArrayList<>(allowedOrigins);
    }

    public List<String> getSourceOrder() {
        return sourceOrder;
    }

    public void setSourceOrder(List<String> sourceOrder) {
        this.sourceOrder = new ArrayList<>(sourceOrder);
    }

    public Duration getSeleniumWaitTimeout() {
        return seleniumWaitTimeout;
    }

    public void setSeleniumWaitTimeout(Duration seleniumWaitTimeout) {
        this.seleniumWaitTimeout = seleniumWaitTimeout;
    }

    public boolean isSeleniumHeadless() {
        return seleniumHeadless;
    }

    public void setSeleniumHeadless(boolean seleniumHeadless) {
        this.seleniumHeadless = seleniumHeadless;
    }
}

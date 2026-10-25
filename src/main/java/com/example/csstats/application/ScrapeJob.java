package com.example.csstats.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Runs one scrape and ends the process, for scheduled jobs. Start with {@code --spring.profiles.active=scrape};
 * a failed scrape makes the process exit with a non-zero code.
 */
@Component
@Profile("scrape")
public class ScrapeJob implements ApplicationRunner {

    private final SnapshotRefresher refresher;

    public ScrapeJob(SnapshotRefresher refresher) {
        this.refresher = refresher;
    }

    @Override
    public void run(ApplicationArguments args) {
        refresher.refreshOrThrow();
    }
}

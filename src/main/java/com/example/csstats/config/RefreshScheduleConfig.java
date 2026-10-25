package com.example.csstats.config;

import com.example.csstats.application.PlayerSnapshot;
import com.example.csstats.application.PlayerSnapshotStore;
import com.example.csstats.application.SnapshotRefresher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scraper.refresh-enabled", havingValue = "true", matchIfMissing = true)
public class RefreshScheduleConfig implements SchedulingConfigurer {

    private final SnapshotRefresher refresher;
    private final PlayerSnapshotStore snapshotStore;
    private final ScraperProperties properties;
    private final Clock clock;

    public RefreshScheduleConfig(
            SnapshotRefresher refresher,
            PlayerSnapshotStore snapshotStore,
            ScraperProperties properties,
            Clock clock
    ) {
        this.refresher = refresher;
        this.snapshotStore = snapshotStore;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.addFixedDelayTask(new IntervalTask(refresher::refresh, properties.getRefreshInterval(), initialDelay()));
    }

    /** Scrape right away when there is no snapshot or it is stale, otherwise wait out the remainder. */
    private Duration initialDelay() {
        Duration interval = properties.getRefreshInterval();
        return snapshotStore.load()
                .map(PlayerSnapshot::fetchedAt)
                .map(fetchedAt -> interval.minus(Duration.between(fetchedAt, Instant.now(clock))))
                .filter(remaining -> !remaining.isNegative())
                .orElse(Duration.ZERO);
    }
}

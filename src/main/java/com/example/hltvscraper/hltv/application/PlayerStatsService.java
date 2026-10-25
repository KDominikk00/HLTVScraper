package com.example.hltvscraper.hltv.application;

import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class PlayerStatsService {

    private final PlayerSnapshotStore snapshotStore;

    public PlayerStatsService(PlayerSnapshotStore snapshotStore) {
        this.snapshotStore = snapshotStore;
    }

    public PlayerSearchResult searchPlayers(PlayerQuery query) {
        PlayerSnapshot snapshot = currentSnapshot();
        Stream<PlayerStats> stream = snapshot.players().stream();

        if (query.team() != null && !query.team().isBlank()) {
            String teamFilter = query.team().trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(player -> player.teamName() != null
                    && player.teamName().toLowerCase(Locale.ROOT).contains(teamFilter));
        }

        if (query.minRating() != null) {
            stream = stream.filter(player -> player.rating() != null && player.rating() >= query.minRating());
        }

        if (query.limit() != null) {
            stream = stream.limit(query.limit());
        }

        List<PlayerStats> players = stream.toList();
        return new PlayerSearchResult(players, snapshot.fetchedAt());
    }

    public PlayerStats getPlayerByNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname must not be blank");
        }

        String normalizedNickname = nickname.trim().toLowerCase(Locale.ROOT);
        return currentSnapshot().players().stream()
                .filter(player -> player.nickname() != null
                        && player.nickname().trim().toLowerCase(Locale.ROOT).equals(normalizedNickname))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + nickname));
    }

    private PlayerSnapshot currentSnapshot() {
        return snapshotStore.load()
                .orElseThrow(() -> new ScraperUnavailableException("Player data has not been loaded yet"));
    }
}

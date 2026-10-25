package com.example.csstats.application;

import com.example.csstats.domain.PlayerStats;
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

        if (query.minScore() != null) {
            stream = stream.filter(player -> player.score() != null && player.score() >= query.minScore());
        }

        if (query.limit() != null) {
            stream = stream.limit(query.limit());
        }

        List<PlayerStats> players = stream.toList();
        return new PlayerSearchResult(players, snapshot.fetchedAt());
    }

    /** Looks a player up by slug first, then by nickname, ignoring case. */
    public PlayerStats getPlayer(String slugOrNickname) {
        if (slugOrNickname == null || slugOrNickname.isBlank()) {
            throw new IllegalArgumentException("player must not be blank");
        }

        String wanted = slugOrNickname.trim().toLowerCase(Locale.ROOT);
        List<PlayerStats> players = currentSnapshot().players();
        return players.stream().filter(player -> wanted.equals(lower(player.slug()))).findFirst()
                .or(() -> players.stream().filter(player -> wanted.equals(lower(player.nickname()))).findFirst())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + slugOrNickname));
    }

    private static String lower(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private PlayerSnapshot currentSnapshot() {
        return snapshotStore.load()
                .orElseThrow(() -> new ScraperUnavailableException("Player data has not been loaded yet"));
    }
}

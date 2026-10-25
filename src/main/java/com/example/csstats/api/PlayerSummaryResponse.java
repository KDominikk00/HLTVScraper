package com.example.csstats.api;

import com.example.csstats.domain.PlayerStats;

/** The handful of stats needed to list and compare players without pulling every category. */
public record PlayerSummaryResponse(
        String slug,
        String nickname,
        String country,
        String avatarUrl,
        String team,
        String teamSlug,
        String teamLogoUrl,
        Double score,
        Double killsPerRound,
        Double deathsPerRound,
        Double killDeathRatio,
        Double damagePerRound,
        Integer maps,
        Integer rounds
) {

    public static PlayerSummaryResponse from(PlayerStats player) {
        return new PlayerSummaryResponse(
                player.slug(),
                player.nickname(),
                player.country(),
                player.avatarUrl(),
                player.teamName(),
                player.teamSlug(),
                player.teamLogoUrl(),
                player.score(),
                player.killsPerRound(),
                player.deathsPerRound(),
                player.killDeathRatio(),
                player.damagePerRound(),
                player.maps() == null ? null : player.maps().count(),
                player.rounds() == null ? null : player.rounds().count()
        );
    }
}

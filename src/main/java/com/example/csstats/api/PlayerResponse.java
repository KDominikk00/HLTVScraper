package com.example.csstats.api;

import com.example.csstats.domain.PlayerStats;

public record PlayerResponse(
        String nickname,
        String team,
        String teamLogo,
        Integer maps,
        Integer rounds,
        Integer killDeathDiff,
        Double killDeathRatio,
        Double rating
) {

    public static PlayerResponse from(PlayerStats player) {
        return new PlayerResponse(
                player.nickname(),
                player.teamName(),
                player.teamLogoUrl(),
                player.maps(),
                player.rounds(),
                player.killDeathDiff(),
                player.killDeathRatio(),
                player.rating()
        );
    }
}

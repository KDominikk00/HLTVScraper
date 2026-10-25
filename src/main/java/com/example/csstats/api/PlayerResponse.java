package com.example.csstats.api;

import com.example.csstats.domain.AccuracyStat;
import com.example.csstats.domain.CountStat;
import com.example.csstats.domain.MapStats;
import com.example.csstats.domain.PlayerStats;
import com.example.csstats.domain.StatCategory;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.LocalDate;
import java.util.List;

/** A player with everything we scraped. */
public record PlayerResponse(
        @JsonUnwrapped PlayerSummaryResponse summary,
        String fullName,
        Integer age,
        LocalDate birthDate,
        Double prizeMoney,
        CountStat tournaments,
        CountStat matches,
        CountStat maps,
        CountStat rounds,
        List<AccuracyStat> accuracy,
        List<StatCategory> categories,
        List<MapStats> mapStats
) {

    public static PlayerResponse from(PlayerStats player) {
        return new PlayerResponse(
                PlayerSummaryResponse.from(player),
                player.fullName(),
                player.age(),
                player.birthDate(),
                player.prizeMoney(),
                player.tournaments(),
                player.matches(),
                player.maps(),
                player.rounds(),
                player.accuracy(),
                player.categories(),
                player.mapStats()
        );
    }
}

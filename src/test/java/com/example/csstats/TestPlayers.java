package com.example.csstats;

import com.example.csstats.domain.AccuracyStat;
import com.example.csstats.domain.CountStat;
import com.example.csstats.domain.MapStats;
import com.example.csstats.domain.PlayerStats;
import com.example.csstats.domain.StatCategory;
import com.example.csstats.domain.StatValue;

import java.time.LocalDate;
import java.util.List;

public final class TestPlayers {

    private TestPlayers() {
    }

    public static PlayerStats player(String nickname, String team, double score) {
        return new PlayerStats(
                nickname.toLowerCase(),
                nickname,
                nickname + " Fullname",
                "France",
                25,
                LocalDate.of(2000, 11, 9),
                "https://img/" + nickname + ".png",
                team,
                team.toLowerCase(),
                "https://img/" + team + ".png",
                1_000_000.0,
                new CountStat(9, 33.0),
                new CountStat(38, 74.0),
                new CountStat(95, 68.0),
                new CountStat(1983, 56.0),
                List.of(new AccuracyStat("Head", 867, 24.0)),
                List.of(new StatCategory("Main", List.of(
                        new StatValue("Score", score, 6.27, null),
                        new StatValue("Kills", 0.84, 0.67, null),
                        new StatValue("Death", 0.56, 0.67, null),
                        new StatValue("Damage", 86.67, 73.56, null)
                ))),
                List.of(new MapStats("Cache", 7.5, 9, 0.93, 94.0))
        );
    }
}

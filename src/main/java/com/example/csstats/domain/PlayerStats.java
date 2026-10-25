package com.example.csstats.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Everything we know about one player. The in-game {@code categories} cover the player's last 15 matches,
 * the count stats, accuracy and map stats cover the last 6 months.
 */
public record PlayerStats(
        String slug,
        String nickname,
        String fullName,
        String country,
        Integer age,
        LocalDate birthDate,
        String avatarUrl,
        String teamName,
        String teamSlug,
        String teamLogoUrl,
        Double prizeMoney,
        CountStat tournaments,
        CountStat matches,
        CountStat maps,
        CountStat rounds,
        List<AccuracyStat> accuracy,
        List<StatCategory> categories,
        List<MapStats> mapStats
) {

    public PlayerStats {
        accuracy = List.copyOf(accuracy);
        categories = List.copyOf(categories);
        mapStats = List.copyOf(mapStats);
    }

    public Double score() {
        return statValue("Main", "Score");
    }

    public Double killsPerRound() {
        return statValue("Main", "Kills");
    }

    public Double deathsPerRound() {
        return statValue("Main", "Death");
    }

    public Double damagePerRound() {
        return statValue("Main", "Damage");
    }

    public Double killDeathRatio() {
        Double kills = killsPerRound();
        Double deaths = deathsPerRound();
        if (kills == null || deaths == null || deaths == 0) {
            return null;
        }
        return Math.round(kills / deaths * 100) / 100.0;
    }

    public Optional<StatValue> stat(String category, String name) {
        return categories.stream()
                .filter(candidate -> candidate.name().equalsIgnoreCase(category))
                .flatMap(candidate -> candidate.stats().stream())
                .filter(stat -> stat.name().equalsIgnoreCase(name))
                .findFirst();
    }

    private Double statValue(String category, String name) {
        return stat(category, name).map(StatValue::value).orElse(null);
    }
}

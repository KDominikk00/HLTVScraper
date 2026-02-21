package com.example.hltvscraper.hltv.domain;

public record PlayerStats(
        String nickname,
        String teamName,
        String teamLogoUrl,
        Integer maps,
        Integer rounds,
        Integer killDeathDiff,
        Double killDeathRatio,
        Double rating
) {
}

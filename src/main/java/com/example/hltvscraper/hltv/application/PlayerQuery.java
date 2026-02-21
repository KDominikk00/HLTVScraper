package com.example.hltvscraper.hltv.application;

public record PlayerQuery(
        boolean forceRefresh,
        Integer limit,
        String team,
        Double minRating
) {
    public PlayerQuery {
        if (limit != null && limit < 1) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        if (minRating != null && minRating < 0) {
            throw new IllegalArgumentException("minRating must be greater than or equal to 0");
        }
    }
}

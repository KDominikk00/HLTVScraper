package com.example.csstats.application;

public record PlayerQuery(Integer limit, String team, Double minScore) {

    public PlayerQuery {
        if (limit != null && limit < 1) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        if (minScore != null && minScore < 0) {
            throw new IllegalArgumentException("minScore must be greater than or equal to 0");
        }
    }
}

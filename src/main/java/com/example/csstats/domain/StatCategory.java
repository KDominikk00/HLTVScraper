package com.example.csstats.domain;

import java.util.List;

public record StatCategory(String name, List<StatValue> stats) {

    public StatCategory {
        stats = List.copyOf(stats);
    }
}

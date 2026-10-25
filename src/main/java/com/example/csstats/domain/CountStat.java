package com.example.csstats.domain;

/** A total together with the share of it that was won, in percent. */
public record CountStat(Integer count, Double winRate) {
}

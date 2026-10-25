package com.example.csstats.domain;

/**
 * One in-game stat: the player's value, the same stat averaged over all players, and the unit
 * ("%" or "s") when the value carries one.
 */
public record StatValue(String name, Double value, Double average, String unit) {
}

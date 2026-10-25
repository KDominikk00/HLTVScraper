package com.example.csstats.infrastructure.bo3;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bo3LinkParserTest {

    private final Bo3LinkParser parser = new Bo3LinkParser();

    @Test
    void readsRankedPlayers() {
        List<String> players = parser.rankedPlayers(Fixtures.page("players"));

        assertEquals(50, players.size());
        assertEquals("m0nesy", players.get(0));
        assertTrue(players.contains("zywoo"));
    }

    @Test
    void readsTournamentsFromHomePage() {
        List<String> tournaments = parser.tournaments(Fixtures.page("home"));

        assertTrue(tournaments.contains("pgl-masters-bucharest-2026"));
        assertFalse(tournaments.stream().anyMatch(slug -> slug.contains("/")));
    }

    @Test
    void readsTeamsOfATournament() {
        List<String> teams = parser.teams(Fixtures.page("tournament"));

        assertTrue(teams.containsAll(List.of("vitality", "spirit", "natus-vincere")));
    }

    @Test
    void readsOnlyStartersFromARoster() {
        List<String> roster = parser.roster(Fixtures.page("team"));

        assertEquals(List.of("ropz", "zywoo", "flamez", "jl-cs-go", "mezil"), roster);
    }
}

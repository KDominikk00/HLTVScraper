package com.example.csstats.api;

import com.example.csstats.application.PlayerNotFoundException;
import com.example.csstats.application.PlayerSearchResult;
import com.example.csstats.application.PlayerStatsService;
import com.example.csstats.TestPlayers;
import com.example.csstats.domain.PlayerStats;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerStatsService playerStatsService;

    @Test
    void returnsPlayersEnvelope() throws Exception {
        PlayerStats player = TestPlayers.player("donk", "Spirit", 6.8);
        PlayerSearchResult result = new PlayerSearchResult(
                List.of(player),
                Instant.parse("2026-01-01T00:00:00Z")
        );

        when(playerStatsService.searchPlayers(argThat(matchesQuery(10, "Spirit", 6.0))))
                .thenReturn(result);

        mockMvc.perform(get("/api/v1/players")
                        .param("limit", "10")
                        .param("team", "Spirit")
                        .param("minScore", "6.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.fetchedAt").value("2026-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.players[0].nickname").value("donk"))
                .andExpect(jsonPath("$.players[0].team").value("Spirit"))
                .andExpect(jsonPath("$.players[0].score").value(6.8))
                .andExpect(jsonPath("$.players[0].killDeathRatio").value(1.5))
                .andExpect(jsonPath("$.players[0].maps").value(95));

        verify(playerStatsService).searchPlayers(argThat(matchesQuery(10, "Spirit", 6.0)));
    }

    @Test
    void returnsFullPlayerWithStatCategories() throws Exception {
        when(playerStatsService.getPlayer("donk")).thenReturn(TestPlayers.player("donk", "Spirit", 6.8));

        mockMvc.perform(get("/api/v1/players/donk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("donk"))
                .andExpect(jsonPath("$.team").value("Spirit"))
                .andExpect(jsonPath("$.categories[0].name").value("Main"))
                .andExpect(jsonPath("$.categories[0].stats[0].name").value("Score"))
                .andExpect(jsonPath("$.mapStats[0].map").value("Cache"))
                .andExpect(jsonPath("$.rounds.count").value(1983));
    }

    @Test
    void returns404WhenPlayerIsMissing() throws Exception {
        when(playerStatsService.getPlayer(eq("unknown")))
                .thenThrow(new PlayerNotFoundException("Player not found: unknown"));

        mockMvc.perform(get("/api/v1/players/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Player not found: unknown"));
    }

    @Test
    void validatesLimit() throws Exception {
        mockMvc.perform(get("/api/v1/players").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    private ArgumentMatcher<com.example.csstats.application.PlayerQuery> matchesQuery(
            Integer limit,
            String team,
            Double minScore
    ) {
        return query -> query.limit().equals(limit)
                && query.team().equals(team)
                && query.minScore().equals(minScore);
    }
}

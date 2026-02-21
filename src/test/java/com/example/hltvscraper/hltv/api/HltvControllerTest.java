package com.example.hltvscraper.hltv.api;

import com.example.hltvscraper.hltv.application.PlayerNotFoundException;
import com.example.hltvscraper.hltv.application.PlayerSearchResult;
import com.example.hltvscraper.hltv.application.PlayerStatsService;
import com.example.hltvscraper.hltv.domain.PlayerStats;
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

@WebMvcTest(HltvController.class)
class HltvControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerStatsService playerStatsService;

    @Test
    void returnsPlayersEnvelope() throws Exception {
        PlayerStats player = new PlayerStats("donk", "Spirit", "logo", 90, 1900, 150, 1.30, 1.35);
        PlayerSearchResult result = new PlayerSearchResult(
                List.of(player),
                Instant.parse("2026-01-01T00:00:00Z"),
                true
        );

        when(playerStatsService.searchPlayers(argThat(matchesQuery(10, "Spirit", 1.2, true))))
                .thenReturn(result);

        mockMvc.perform(get("/api/v1/hltv/players")
                        .param("refresh", "true")
                        .param("limit", "10")
                        .param("team", "Spirit")
                        .param("minRating", "1.2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.fromCache").value(true))
                .andExpect(jsonPath("$.players[0].nickname").value("donk"))
                .andExpect(jsonPath("$.players[0].team").value("Spirit"))
                .andExpect(jsonPath("$.players[0].rating").value(1.35));

        verify(playerStatsService).searchPlayers(argThat(matchesQuery(10, "Spirit", 1.2, true)));
    }

    @Test
    void returns404WhenPlayerIsMissing() throws Exception {
        when(playerStatsService.getPlayerByNickname(eq("unknown"), eq(false)))
                .thenThrow(new PlayerNotFoundException("Player not found: unknown"));

        mockMvc.perform(get("/api/v1/hltv/players/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Player not found: unknown"));
    }

    @Test
    void validatesLimit() throws Exception {
        mockMvc.perform(get("/api/v1/hltv/players").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    private ArgumentMatcher<com.example.hltvscraper.hltv.application.PlayerQuery> matchesQuery(
            Integer limit,
            String team,
            Double minRating,
            boolean refresh
    ) {
        return query -> query.limit().equals(limit)
                && query.team().equals(team)
                && query.minRating().equals(minRating)
                && query.forceRefresh() == refresh;
    }
}

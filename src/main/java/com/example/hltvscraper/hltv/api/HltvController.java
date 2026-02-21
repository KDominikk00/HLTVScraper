package com.example.hltvscraper.hltv.api;

import com.example.hltvscraper.hltv.application.PlayerQuery;
import com.example.hltvscraper.hltv.application.PlayerSearchResult;
import com.example.hltvscraper.hltv.application.PlayerStatsService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hltv")
@Validated
public class HltvController {

    private final PlayerStatsService playerStatsService;

    public HltvController(PlayerStatsService playerStatsService) {
        this.playerStatsService = playerStatsService;
    }

    @GetMapping("/players")
    public PlayersResponse getPlayers(
            @RequestParam(defaultValue = "false") boolean refresh,
            @RequestParam(required = false) @Positive Integer limit,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) @DecimalMin("0.0") Double minRating
    ) {
        PlayerQuery query = new PlayerQuery(refresh, limit, team, minRating);
        PlayerSearchResult result = playerStatsService.searchPlayers(query);

        List<PlayerResponse> players = result.players().stream()
                .map(PlayerResponse::from)
                .toList();

        return new PlayersResponse(players, players.size(), result.fetchedAt(), result.fromCache());
    }

    @GetMapping("/players/{nickname}")
    public PlayerResponse getPlayerByNickname(
            @PathVariable String nickname,
            @RequestParam(defaultValue = "false") boolean refresh
    ) {
        return PlayerResponse.from(playerStatsService.getPlayerByNickname(nickname, refresh));
    }
}

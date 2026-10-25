package com.example.csstats.api;

import com.example.csstats.application.PlayerQuery;
import com.example.csstats.application.PlayerSearchResult;
import com.example.csstats.application.PlayerStatsService;
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
@RequestMapping("/api/v1")
@Validated
public class PlayerController {

    private final PlayerStatsService playerStatsService;

    public PlayerController(PlayerStatsService playerStatsService) {
        this.playerStatsService = playerStatsService;
    }

    @GetMapping("/players")
    public PlayersResponse getPlayers(
            @RequestParam(required = false) @Positive Integer limit,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) @DecimalMin("0.0") Double minScore
    ) {
        PlayerQuery query = new PlayerQuery(limit, team, minScore);
        PlayerSearchResult result = playerStatsService.searchPlayers(query);

        List<PlayerSummaryResponse> players = result.players().stream()
                .map(PlayerSummaryResponse::from)
                .toList();

        return new PlayersResponse(players, players.size(), result.fetchedAt());
    }

    @GetMapping("/players/{player}")
    public PlayerResponse getPlayer(@PathVariable String player) {
        return PlayerResponse.from(playerStatsService.getPlayer(player));
    }
}

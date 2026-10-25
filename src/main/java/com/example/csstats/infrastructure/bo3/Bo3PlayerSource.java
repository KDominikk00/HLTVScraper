package com.example.csstats.infrastructure.bo3;

import com.example.csstats.application.PlayerSource;
import com.example.csstats.application.ScraperUnavailableException;
import com.example.csstats.config.ScraperProperties;
import com.example.csstats.domain.PlayerStats;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Crawls bo3.gg: the ranked players first, then every team playing in a current tournament,
 * and finally each player's own page.
 */
@Component
@ConditionalOnProperty(name = "scraper.source", havingValue = "browser", matchIfMissing = true)
public class Bo3PlayerSource implements PlayerSource {

    private static final Logger logger = LoggerFactory.getLogger(Bo3PlayerSource.class);

    // These go to Chrome as well as Jsoup, and Chrome insists on quoted attribute values.
    private static final String RANKED_PLAYER_LINK = ".table-row .table-cell.player a";
    private static final String TOURNAMENT_LINK = "a[href^='/tournaments/']";
    private static final String TEAM_LINK = "a[href^='/teams/']";
    private static final String ROSTER_LINK = "li[data-test='squadItem'] a[href^='/players/']";
    private static final String GENERAL_STATS = ".c-widget-general-stats .table-row";
    private static final String INGAME_ROW = Bo3PlayerPageParser.INGAME_WIDGET + " .c-table-ingame-stats .table-group .table-row";

    private static final String MAIN_CATEGORY = "Main";
    private static final List<String> OTHER_CATEGORIES =
            List.of("Performance", "AIM", "Grenades", "Primary devices", "Pistols", "Economy");
    private static final int CHECKPOINT_EVERY = 25;

    private final BrowserSessionFactory sessionFactory;
    private final Bo3LinkParser linkParser;
    private final Bo3PlayerPageParser playerParser;
    private final ScraperProperties properties;

    public Bo3PlayerSource(
            BrowserSessionFactory sessionFactory,
            Bo3LinkParser linkParser,
            Bo3PlayerPageParser playerParser,
            ScraperProperties properties
    ) {
        this.sessionFactory = sessionFactory;
        this.linkParser = linkParser;
        this.playerParser = playerParser;
        this.properties = properties;
    }

    @Override
    public List<PlayerStats> fetchPlayers(Consumer<List<PlayerStats>> checkpoint) {
        List<PlayerStats> players = new ArrayList<>();

        try (BrowserSession session = sessionFactory.open()) {
            List<String> slugs = discoverPlayers(session);
            logger.info("Scraping {} players", slugs.size());

            for (String slug : slugs) {
                scrapePlayer(session, slug).ifPresent(players::add);
                if (players.size() % CHECKPOINT_EVERY == 0 && !players.isEmpty()) {
                    checkpoint.accept(List.copyOf(players));
                }
            }
        }

        if (players.size() < properties.minPlayers()) {
            throw new ScraperUnavailableException(
                    "Only %d players could be scraped, expected at least %d".formatted(players.size(), properties.minPlayers()));
        }
        return players;
    }

    private List<String> discoverPlayers(BrowserSession session) {
        Set<String> slugs = new LinkedHashSet<>();
        slugs.addAll(linkParser.rankedPlayers(session.load(url("/players"), RANKED_PLAYER_LINK)));

        try {
            for (String teamSlug : discoverTeams(session)) {
                if (slugs.size() >= properties.maxPlayers()) {
                    break;
                }
                slugs.addAll(linkParser.roster(session.load(url("/teams/" + teamSlug), ROSTER_LINK)));
            }
        } catch (ScraperUnavailableException exception) {
            logger.warn("Team discovery stopped early, continuing with {} players: {}", slugs.size(), exception.getMessage());
        }

        return slugs.stream().limit(properties.maxPlayers()).toList();
    }

    private Set<String> discoverTeams(BrowserSession session) {
        Set<String> teams = new LinkedHashSet<>();
        List<String> tournaments = linkParser.tournaments(session.load(url("/"), TOURNAMENT_LINK));
        for (String tournament : tournaments.stream().limit(properties.maxTournaments()).toList()) {
            teams.addAll(linkParser.teams(session.load(url("/tournaments/" + tournament), TEAM_LINK)));
        }
        logger.info("Found {} teams in {} tournaments", teams.size(), Math.min(tournaments.size(), properties.maxTournaments()));
        return teams;
    }

    private Optional<PlayerStats> scrapePlayer(BrowserSession session, String slug) {
        try {
            Document page = session.load(url("/players/" + slug), GENERAL_STATS, INGAME_ROW);

            Map<String, Document> categories = new LinkedHashMap<>();
            categories.put(MAIN_CATEGORY, page);
            for (String category : OTHER_CATEGORIES) {
                try {
                    categories.put(category, session.selectOption(Bo3PlayerPageParser.INGAME_WIDGET, category));
                } catch (ScraperUnavailableException exception) {
                    logger.warn("{}: skipped category {}: {}", slug, category, exception.getMessage());
                }
            }
            return playerParser.parse(slug, page, categories);
        } catch (ScraperUnavailableException exception) {
            logger.warn("Skipped player {}: {}", slug, exception.getMessage());
            return Optional.empty();
        }
    }

    private URI url(String path) {
        return properties.baseUrl().resolve(path);
    }
}

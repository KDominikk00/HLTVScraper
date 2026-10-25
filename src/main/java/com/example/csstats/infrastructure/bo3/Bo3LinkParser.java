package com.example.csstats.infrastructure.bo3;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pulls the slugs of players, teams and tournaments out of the pages that link to them. */
@Component
public class Bo3LinkParser {

    private static final String ROSTER_ITEMS = ".c-widget-team-last-match-squad li[data-test=squadItem]";
    private static final String LISTED_PLAYERS = ".table-row .table-cell.player a";

    /** Players on the ranking list at /players. */
    public List<String> rankedPlayers(Document document) {
        return slugs(document.select(LISTED_PLAYERS), "players");
    }

    /** Tournaments linked from the home page. */
    public List<String> tournaments(Document document) {
        return slugs(document.select("a[href^=/tournaments/]"), "tournaments");
    }

    /** Teams taking part in a tournament. */
    public List<String> teams(Document document) {
        return slugs(document.select("a[href^=/teams/]"), "teams");
    }

    /** Starting players of a team, coaches and former members excluded. */
    public List<String> roster(Document document) {
        return slugs(document.select(ROSTER_ITEMS + " a[href^=/players/]"), "players");
    }

    private List<String> slugs(Iterable<Element> links, String section) {
        Pattern pattern = Pattern.compile("^/" + section + "/([^/#?]+)(?:#.*)?$");
        Set<String> slugs = new LinkedHashSet<>();
        for (Element link : links) {
            Matcher matcher = pattern.matcher(link.attr("href"));
            if (matcher.matches()) {
                slugs.add(matcher.group(1));
            }
        }
        return List.copyOf(slugs);
    }
}

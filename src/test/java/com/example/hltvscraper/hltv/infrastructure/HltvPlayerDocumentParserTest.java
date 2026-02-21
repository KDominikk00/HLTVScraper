package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HltvPlayerDocumentParserTest {

    private final HltvPlayerDocumentParser parser = new HltvPlayerDocumentParser();

    @Test
    void parsesExpectedPlayerFields() {
        Document document = Jsoup.parse(
                """
                <table class=\"player-ratings-table\"> 
                  <tbody>
                    <tr>
                      <td class=\"playerCol\"><a>ZywOo</a></td>
                      <td class=\"teamCol\"><a><img alt=\"Vitality\" src=\"/img/vitality.png\" /></a></td>
                      <td class=\"statsDetail\">120</td>
                      <td class=\"statsDetail\">2500</td>
                      <td class=\"statsDetail\">1.33</td>
                      <td class=\"kdDiffCol\">+210</td>
                      <td class=\"ratingCol\">1.35</td>
                    </tr>
                  </tbody>
                </table>
                """,
                "https://www.hltv.org"
        );

        List<PlayerStats> players = parser.parse(document);

        assertEquals(1, players.size());
        PlayerStats player = players.get(0);

        assertEquals("ZywOo", player.nickname());
        assertEquals("Vitality", player.teamName());
        assertEquals("https://www.hltv.org/img/vitality.png", player.teamLogoUrl());
        assertEquals(120, player.maps());
        assertEquals(2500, player.rounds());
        assertEquals(210, player.killDeathDiff());
        assertEquals(1.33, player.killDeathRatio());
        assertEquals(1.35, player.rating());
    }

    @Test
    void handlesMissingValuesGracefully() {
        Document document = Jsoup.parse(
                """
                <table class=\"player-ratings-table\"> 
                  <tbody>
                    <tr>
                      <td class=\"playerCol\"><a>PlayerWithoutTeam</a></td>
                      <td class=\"ratingCol\">-</td>
                    </tr>
                  </tbody>
                </table>
                """
        );

        List<PlayerStats> players = parser.parse(document);

        assertEquals(1, players.size());
        PlayerStats player = players.get(0);

        assertEquals("No Team", player.teamName());
        assertNull(player.rating());
    }
}

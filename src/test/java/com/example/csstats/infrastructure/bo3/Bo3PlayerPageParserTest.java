package com.example.csstats.infrastructure.bo3;

import com.example.csstats.domain.MapStats;
import com.example.csstats.domain.PlayerStats;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bo3PlayerPageParserTest {

    private final Bo3PlayerPageParser parser = new Bo3PlayerPageParser();

    @Test
    void parsesProfile() {
        PlayerStats player = zywoo();

        assertEquals("zywoo", player.slug());
        assertEquals("ZywOo", player.nickname());
        assertEquals("Mathieu Herbaut", player.fullName());
        assertEquals("France", player.country());
        assertEquals(LocalDate.of(2000, 11, 9), player.birthDate());
        assertEquals(25, player.age());
        assertEquals(1615290.0, player.prizeMoney());
        assertEquals("Vitality", player.teamName());
        assertEquals("vitality", player.teamSlug());
        assertTrue(player.teamLogoUrl().startsWith("https://image-proxy.bo3.gg/uploads/team/667/"));
        assertTrue(player.avatarUrl().startsWith("https://image-proxy.bo3.gg/uploads/player/18452/"));
    }

    @Test
    void parsesSixMonthTotals() {
        PlayerStats player = zywoo();

        assertEquals(9, player.tournaments().count());
        assertEquals(38, player.matches().count());
        assertEquals(74.0, player.matches().winRate());
        assertEquals(95, player.maps().count());
        assertEquals(1983, player.rounds().count());
    }

    @Test
    void parsesAccuracyAndThousandsSuffix() {
        PlayerStats player = zywoo();

        assertEquals(5, player.accuracy().size());
        assertEquals("Head", player.accuracy().get(0).bodyPart());
        assertEquals(867, player.accuracy().get(0).hits());
        assertEquals(24.0, player.accuracy().get(0).share());
        assertEquals(1700, player.accuracy().get(1).hits());
    }

    @Test
    void parsesInGameStatsFromEachCategory() {
        PlayerStats player = zywoo();

        assertEquals(7.2, player.stat("Main", "Score").orElseThrow().value());
        assertEquals(6.27, player.stat("Main", "Score").orElseThrow().average());
        assertEquals(0.84, player.stat("Main", "Kills").orElseThrow().value());
        assertEquals(86.67, player.stat("Main", "Damage").orElseThrow().value());
    }

    @Test
    void readsUnitsFromCategoryTables() {
        Document aim = Jsoup.parse("""
                <div class="o-widget c-widget-ingame-stats"><div class="c-table-ingame-stats"><div class="table-group">
                  <div class="table-row"><div class="table-cell stat"><p>Headshots %</p></div>
                    <div class="table-cell avg"><p>43.1%</p></div><div class="table-cell overall-avg"><p>46%</p></div></div>
                  <div class="table-row"><div class="table-cell stat"><p>Flash blinded</p></div>
                    <div class="table-cell avg"><p>1.38s</p></div><div class="table-cell overall-avg"><p>1.15s</p></div></div>
                </div></div></div>
                """);
        Document page = Fixtures.page("player");

        PlayerStats player = parser.parse("zywoo", page, Map.of("Main", page, "AIM", aim)).orElseThrow();

        assertEquals("%", player.stat("AIM", "Headshots %").orElseThrow().unit());
        assertEquals(43.1, player.stat("AIM", "Headshots %").orElseThrow().value());
        assertEquals(46.0, player.stat("AIM", "Headshots %").orElseThrow().average());
        assertEquals("s", player.stat("AIM", "Flash blinded").orElseThrow().unit());
    }

    @Test
    void parsesMapStats() {
        MapStats cache = zywoo().mapStats().get(0);

        assertEquals("Cache", cache.map());
        assertEquals(7.5, cache.score());
        assertEquals(9, cache.count());
        assertEquals(0.93, cache.killsPerRound());
        assertEquals(94.0, cache.damagePerRound());
        assertEquals(6, zywoo().mapStats().size());
    }

    @Test
    void returnsEmptyForNonPlayerPage() {
        assertTrue(parser.parse("x", Fixtures.page("home"), Map.of()).isEmpty());
        assertNotNull(zywoo());
    }

    private PlayerStats zywoo() {
        Document page = Fixtures.page("player");
        return parser.parse("zywoo", page, Map.of("Main", page)).orElseThrow();
    }
}

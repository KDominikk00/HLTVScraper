package com.example.csstats.infrastructure.bo3;

import com.example.csstats.domain.AccuracyStat;
import com.example.csstats.domain.CountStat;
import com.example.csstats.domain.MapStats;
import com.example.csstats.domain.PlayerStats;
import com.example.csstats.domain.StatCategory;
import com.example.csstats.domain.StatValue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Bo3PlayerPageParser {

    /** The in-game stats widget on the Stats tab, as opposed to the small summary on the overview. */
    public static final String INGAME_WIDGET = ".c-widget-ingame-stats:not(.c-widget-ingame-stats--small)";

    private static final DateTimeFormatter BIRTH_DATE = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
    private static final Pattern BACKGROUND_URL = Pattern.compile("url\\([\"']?([^\"')]+)[\"']?\\)");
    private static final Pattern NUMBER = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*([kK])?\\s*(%|s)?");

    /**
     * @param page       the full player page
     * @param categories the in-game stats table per category name; each document must contain the
     *                   in-game widget as it looked with that category selected
     */
    public Optional<PlayerStats> parse(String slug, Document page, Map<String, Document> categories) {
        Element nicknameElement = page.selectFirst(".o-profile-header__info h1");
        if (nicknameElement == null || nicknameElement.text().isBlank()) {
            return Optional.empty();
        }

        Map<String, Element> info = playerInfo(page);
        Element team = info.get("team");
        Element teamLink = team == null ? null : team.selectFirst("a[href^=/teams/]");

        Map<String, CountStat> counts = generalStats(page);

        PlayerStats player = new PlayerStats(
                slug,
                nicknameElement.text().trim(),
                textOrNull(page.selectFirst(".o-profile-header__info p")),
                textOrNull(info.get("country")),
                toInteger(textOrNull(info.get("age"))),
                toDate(textOrNull(info.get("birth date"))),
                backgroundUrl(page.selectFirst(".o-profile-header__image .c-avatar span[style*=background-image]")),
                team == null ? null : team.text().trim(),
                teamLink == null ? null : teamLink.attr("href").replaceFirst("^/teams/", ""),
                biggerImage(backgroundUrl(team == null ? null : team.selectFirst(".c-logo__img"))),
                toNumber(textOrNull(info.get("total prize money"))),
                counts.get("tournaments"),
                counts.get("matches"),
                counts.get("maps"),
                counts.get("rounds"),
                accuracy(page),
                categories(categories),
                mapStats(page)
        );
        return Optional.of(player);
    }

    private Map<String, Element> playerInfo(Document page) {
        Map<String, Element> info = new HashMap<>();
        for (Element item : page.select(".o-profile-sidebar__item li:has(> label)")) {
            Element label = item.selectFirst("> label");
            Element value = item.selectFirst("> p, > a");
            if (label != null && value != null) {
                info.put(label.text().trim().toLowerCase(Locale.ROOT), value);
            }
        }
        return info;
    }

    private Map<String, CountStat> generalStats(Document page) {
        Map<String, CountStat> counts = new HashMap<>();
        for (Element row : page.select(".c-widget-general-stats .table-group .table-row")) {
            String name = textOrNull(row.selectFirst(".stats"));
            if (name == null) {
                continue;
            }
            Integer count = toInteger(textOrNull(row.selectFirst(".count")));
            Double winRate = toNumber(textOrNull(row.selectFirst(".winrate .value")));
            counts.put(name.toLowerCase(Locale.ROOT), new CountStat(count, winRate));
        }
        return counts;
    }

    private List<AccuracyStat> accuracy(Document page) {
        List<AccuracyStat> stats = new ArrayList<>();
        for (Element row : page.select(".c-table-accuracy-stats .table-group .table-row")) {
            String bodyPart = textOrNull(row.selectFirst(".body-part"));
            Elements values = row.select(".hits .col:first-child p span");
            if (bodyPart != null && values.size() >= 2) {
                stats.add(new AccuracyStat(bodyPart, toInteger(values.get(0).text()), toNumber(values.get(1).text())));
            }
        }
        return stats;
    }

    private List<StatCategory> categories(Map<String, Document> categories) {
        List<StatCategory> result = new ArrayList<>();
        categories.forEach((name, document) -> {
            List<StatValue> values = new ArrayList<>();
            for (Element row : document.select(INGAME_WIDGET + " .c-table-ingame-stats .table-group .table-row")) {
                String statName = textOrNull(row.selectFirst(".stat"));
                String value = textOrNull(row.selectFirst(".avg"));
                if (statName != null && toNumber(value) != null) {
                    values.add(new StatValue(statName, toNumber(value), toNumber(textOrNull(row.selectFirst(".overall-avg"))), unit(value)));
                }
            }
            if (!values.isEmpty()) {
                result.add(new StatCategory(name, values));
            }
        });
        return result;
    }

    private List<MapStats> mapStats(Document page) {
        List<MapStats> stats = new ArrayList<>();
        Element widget = page.selectFirst(".c-widget-map-pool--player");
        if (widget == null) {
            return stats;
        }
        for (Element row : widget.select(".table-group .table-row")) {
            String map = textOrNull(row.selectFirst(".map-name"));
            if (map == null) {
                continue;
            }
            stats.add(new MapStats(
                    map,
                    toNumber(textOrNull(row.selectFirst(".rating .value"))),
                    toInteger(textOrNull(row.selectFirst(".maps-count"))),
                    toNumber(ownText(row.selectFirst(".kills .table-cell__inner"))),
                    toNumber(ownText(row.selectFirst(".damage .table-cell__inner")))
            ));
        }
        return stats;
    }

    private String backgroundUrl(Element element) {
        if (element == null) {
            return null;
        }
        Matcher matcher = BACKGROUND_URL.matcher(element.attr("style").replace("&quot;", "\""));
        return matcher.find() ? matcher.group(1) : null;
    }

    /** The sidebar serves a 20px team logo; ask the image proxy for something usable instead. */
    private String biggerImage(String url) {
        return url == null ? null : url.replace("w=20&h=20", "w=128&h=128");
    }

    private String textOrNull(Element element) {
        if (element == null) {
            return null;
        }
        String text = element.text().trim();
        return text.isEmpty() ? null : text;
    }

    private String ownText(Element element) {
        return element == null ? null : element.ownText();
    }

    private String unit(String value) {
        Matcher matcher = NUMBER.matcher(value.trim());
        return matcher.find() ? matcher.group(3) : null;
    }

    private Integer toInteger(String raw) {
        Double number = toNumber(raw);
        return number == null ? null : (int) Math.round(number);
    }

    /** Reads the first number out of values such as "86.83", "43.1%", "1.7K", "1.38s" or "$1615290". */
    private Double toNumber(String raw) {
        if (raw == null) {
            return null;
        }
        Matcher matcher = NUMBER.matcher(raw.replace(",", "").replace("$", "").trim());
        if (!matcher.find()) {
            return null;
        }
        double number = Double.parseDouble(matcher.group(1));
        return matcher.group(2) == null ? number : number * 1000;
    }

    private LocalDate toDate(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return LocalDate.parse(raw, BIRTH_DATE);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}

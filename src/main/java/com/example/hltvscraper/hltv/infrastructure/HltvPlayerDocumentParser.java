package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class HltvPlayerDocumentParser {

    public List<PlayerStats> parse(Document document) {
        Elements rows = document.select(".player-ratings-table tbody tr");

        return rows.stream()
                .map(this::mapRow)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<PlayerStats> mapRow(Element row) {
        Element playerElement = row.selectFirst(".playerCol a");
        if (playerElement == null || playerElement.text().isBlank()) {
            return Optional.empty();
        }

        Element teamImage = row.selectFirst(".teamCol a img");
        String teamName = teamImage != null && !teamImage.attr("alt").isBlank()
                ? teamImage.attr("alt").trim()
                : "No Team";

        String teamLogo = null;
        if (teamImage != null) {
            teamLogo = teamImage.absUrl("src");
            if (teamLogo.isBlank()) {
                teamLogo = emptyToNull(teamImage.attr("src"));
            }
        }

        Elements statsDetail = row.select(".statsDetail");
        Integer maps = parseInteger(valueAt(statsDetail, 0));
        Integer rounds = parseInteger(valueAt(statsDetail, 1));
        Double killDeathRatio = parseDouble(valueAt(statsDetail, 2));
        Integer killDeathDiff = parseInteger(textOrNull(row.selectFirst(".kdDiffCol")));
        Double rating = parseDouble(textOrNull(row.selectFirst(".ratingCol")));

        PlayerStats player = new PlayerStats(
                playerElement.text().trim(),
                teamName,
                teamLogo,
                maps,
                rounds,
                killDeathDiff,
                killDeathRatio,
                rating
        );
        return Optional.of(player);
    }

    private String valueAt(Elements elements, int index) {
        if (index < 0 || index >= elements.size()) {
            return null;
        }
        return elements.get(index).text();
    }

    private String textOrNull(Element element) {
        if (element == null) {
            return null;
        }
        return element.text();
    }

    private Integer parseInteger(String rawValue) {
        String normalized = normalize(rawValue);
        if (normalized == null) {
            return null;
        }

        String digitsOnly = normalized.replaceAll("[^0-9-]", "");
        if (digitsOnly.isBlank() || "-".equals(digitsOnly)) {
            return null;
        }

        try {
            return Integer.parseInt(digitsOnly);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseDouble(String rawValue) {
        String normalized = normalize(rawValue);
        if (normalized == null) {
            return null;
        }

        String decimal = normalized.replace(',', '.').replaceAll("[^0-9.-]", "");
        if (decimal.isBlank() || "-".equals(decimal)) {
            return null;
        }

        try {
            return Double.parseDouble(decimal);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalize(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String trimmed = rawValue.trim();
        if (trimmed.isBlank() || "-".equals(trimmed)) {
            return null;
        }

        return trimmed;
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

package com.example.hltv_scraper;

import com.example.hltv_scraper.Entity.PlayerDTO;
import com.example.hltv_scraper.Service.HltvScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HltvScraperServiceTest {

    private HltvScraperService scraperService;
    private WebDriver mockDriver;

    @BeforeEach
    public void setup() {
        scraperService = new HltvScraperService();
        mockDriver = mock(ChromeDriver.class);
    }

    @Test
    public void testGetPlayersWhenCacheIsValid() throws InterruptedException {
        // Simulate cached data
        List<PlayerDTO> mockPlayers = List.of(
                new PlayerDTO("Player1", "Team1", "logoUrl1", "50", "100", "10", "1.5", "1.2")
        );
        scraperService.setCachedPlayers(mockPlayers);
        scraperService.setLastFetchTime(java.time.LocalDateTime.now().minusHours(1));

        List<PlayerDTO> players = scraperService.getPlayers();

        assertEquals(1, players.size());
        assertEquals("Player1", players.get(0).getPlayerName());
    }

    @Test
    public void testGetPlayersWhenCacheIsInvalid() throws InterruptedException {
        // Simulate expired cache
        scraperService.setCachedPlayers(null);
        scraperService.setLastFetchTime(java.time.LocalDateTime.now().minusDays(2));

        List<PlayerDTO> players = scraperService.getPlayers();

        assertNotNull(players);
        assertTrue(players.isEmpty());  // Initially should return empty as it's mocked
    }

    @Test
    public void testScrapePlayersWithSelenium() throws InterruptedException {
        List<PlayerDTO> players = scraperService.scrapePlayersWithSelenium();

        assertNotNull(players);
        assertTrue(players.size() > 0);
    }
}
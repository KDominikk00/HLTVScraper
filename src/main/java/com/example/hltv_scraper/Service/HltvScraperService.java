package com.example.hltv_scraper.Service;

import com.example.hltv_scraper.Entity.PlayerDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HltvScraperService {

    private List<PlayerDTO> cachedPlayers; // Cached player data
    private LocalDateTime lastFetchTime;   // Timestamp of the last scrape

    public List<PlayerDTO> getPlayers() throws InterruptedException {
        // Check if cache is valid (data exists and is less than 24 hours old)
        if (cachedPlayers != null && lastFetchTime != null &&
                lastFetchTime.isAfter(LocalDateTime.now().minusDays(1))) {
            System.out.println("Returning cached data...");
            return cachedPlayers;
        }

        // Otherwise, scrape new data
        System.out.println("Fetching new data...");
        cachedPlayers = scrapePlayersWithSelenium();
        lastFetchTime = LocalDateTime.now(); // Update the timestamp
        return cachedPlayers;
    }

    public List<PlayerDTO> scrapePlayersWithSelenium() throws InterruptedException {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.hltv.org/stats/players");

        // Wait for the page to load fully
        Thread.sleep(3000);

        // Try to close the cookies pop-up if it appears
        try {
            WebElement acceptCookiesButton = driver.findElement(By.cssSelector(".CybotCookiebotDialogBodyButton"));
            acceptCookiesButton.click();
            Thread.sleep(1000); // Allow page to refresh
        } catch (NoSuchElementException e) {
            System.out.println("No cookies pop-up found, proceeding with scraping.");
        }

        // Scrape player data
        List<PlayerDTO> players = new ArrayList<>();
        List<WebElement> playerRows = driver.findElements(By.cssSelector(".player-ratings-table tbody tr"));

        for (WebElement row : playerRows) {
            try {
                String playerName = row.findElement(By.cssSelector(".playerCol a")).getText();
                String team = row.findElement(By.cssSelector(".teamCol a img")).getAttribute("alt");
                String teamLogo = row.findElement(By.cssSelector(".teamCol a img")).getAttribute("src");
                List<WebElement> statsDetail = row.findElements(By.cssSelector(".statsDetail"));

                String maps = !statsDetail.isEmpty() ? statsDetail.get(0).getText() : "N/A";
                String rounds = statsDetail.size() > 1 ? statsDetail.get(1).getText() : "N/A";
                String kd = statsDetail.size() > 2 ? statsDetail.get(2).getText() : "N/A";
                String kddiff = row.findElement(By.cssSelector(".kdDiffCol")).getText();
                String rating = row.findElement(By.cssSelector(".ratingCol")).getText();

                PlayerDTO player = new PlayerDTO(playerName, team, teamLogo, maps, rounds, kddiff, kd, rating);
                players.add(player);
            } catch (NoSuchElementException e) {
                System.out.println("Some elements were not found for a row, skipping...");
            }
        }

        // Close the browser
        driver.quit();

        return players;
    }
}
package com.example.hltv_scraper.Service;

import com.example.hltv_scraper.Entity.PlayerDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class HltvScraperService {

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
                // Use refined selectors based on HTML structure
                String playerName = row.findElement(By.cssSelector(".playerCol a")).getText();
                String team = row.findElement(By.cssSelector(".teamCol a img")).getAttribute("alt");
                String teamLogo = row.findElement(By.cssSelector(".teamCol a img")).getAttribute("src"); // Adjusted
                List<WebElement> statsDetail = row.findElements(By.cssSelector(".statsDetail"));

                // Ensure statsDetail has enough elements to avoid IndexOutOfBoundsException
                String maps = !statsDetail.isEmpty() ? statsDetail.get(0).getText() : "N/A";
                String rounds = statsDetail.size() > 1 ? statsDetail.get(1).getText() : "N/A";
                String kd = statsDetail.size() > 2 ? statsDetail.get(2).getText() : "N/A";

                String kddiff = row.findElement(By.cssSelector(".kdDiffCol")).getText();
                String rating = row.findElement(By.cssSelector(".ratingCol")).getText();

                // Print out values for debugging
                System.out.println("Player Name: " + playerName);
                System.out.println("Team: " + team);
                System.out.println("Team Logo: " + teamLogo);
                System.out.println("Maps: " + maps);
                System.out.println("Rounds: " + rounds);
                System.out.println("KD Diff: " + kddiff);
                System.out.println("KD: " + kd);
                System.out.println("Rating: " + rating);

                // Create a PlayerDTO object and add it to the list
                PlayerDTO player = new PlayerDTO(playerName, team, teamLogo, maps, rounds, kddiff, kd, rating);
                players.add(player);
            } catch (NoSuchElementException e) {
                System.out.println("Some elements were not found for a row, skipping...");
            }
        }

        driver.quit();
        return players;
    }
}
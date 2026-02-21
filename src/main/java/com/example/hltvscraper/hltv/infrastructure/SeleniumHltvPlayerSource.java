package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.ScraperUnavailableException;
import com.example.hltvscraper.hltv.domain.PlayerStats;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class SeleniumHltvPlayerSource implements HltvPlayerSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumHltvPlayerSource.class);

    private final HltvScraperProperties properties;
    private final HltvPlayerDocumentParser parser;

    public SeleniumHltvPlayerSource(HltvScraperProperties properties, HltvPlayerDocumentParser parser) {
        this.properties = properties;
        this.parser = parser;
    }

    @Override
    public String id() {
        return "selenium";
    }

    @Override
    public List<PlayerStats> fetchPlayers() {
        ChromeOptions options = new ChromeOptions();
        if (properties.isSeleniumHeadless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--user-agent=" + properties.getUserAgent());

        Duration waitTimeout = properties.getSeleniumWaitTimeout();

        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(waitTimeout);

            driver.get(properties.getPlayersUrl().toString());
            WebDriverWait wait = new WebDriverWait(driver, waitTimeout);
            closeCookiePromptIfPresent(driver, wait);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".player-ratings-table tbody tr")));

            String pageSource = driver.getPageSource();
            Document document = Jsoup.parse(pageSource, properties.getPlayersUrl().toString());
            List<PlayerStats> players = parser.parse(document);
            logger.info("Fetched {} HLTV players through Selenium", players.size());
            return players;
        } catch (RuntimeException exception) {
            throw new ScraperUnavailableException("Selenium HLTV scraping failed", exception);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void closeCookiePromptIfPresent(WebDriver driver, WebDriverWait wait) {
        List<WebElement> cookieButtons = driver.findElements(By.cssSelector(".CybotCookiebotDialogBodyButton"));
        if (!cookieButtons.isEmpty()) {
            cookieButtons.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".CybotCookiebotDialogBody")));
        }
    }
}

package com.example.hltvscraper.hltv.infrastructure;

import com.example.hltvscraper.config.HltvScraperProperties;
import com.example.hltvscraper.hltv.application.ScraperUnavailableException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Component
public class SeleniumPageFetcher implements PageFetcher {

    private static final By PLAYER_ROWS = By.cssSelector(".player-ratings-table tbody tr");

    private final HltvScraperProperties properties;

    public SeleniumPageFetcher(HltvScraperProperties properties) {
        this.properties = properties;
    }

    @Override
    public String fetchHtml(URI url) {
        ChromeOptions options = new ChromeOptions();
        if (properties.isSeleniumHeadless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--disable-gpu",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080",
                "--no-sandbox",
                "--user-agent=" + properties.getUserAgent()
        );

        Duration waitTimeout = properties.getSeleniumWaitTimeout();
        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(waitTimeout);
            driver.get(url.toString());

            WebDriverWait wait = new WebDriverWait(driver, waitTimeout);
            closeCookiePromptIfPresent(driver, wait);
            wait.until(ExpectedConditions.presenceOfElementLocated(PLAYER_ROWS));
            return driver.getPageSource();
        } catch (RuntimeException exception) {
            throw new ScraperUnavailableException("Selenium could not load " + url, exception);
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

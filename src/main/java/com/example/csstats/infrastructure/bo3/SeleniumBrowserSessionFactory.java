package com.example.csstats.infrastructure.bo3;

import com.example.csstats.config.ScraperProperties;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "scraper.source", havingValue = "browser", matchIfMissing = true)
public class SeleniumBrowserSessionFactory implements BrowserSessionFactory {

    private final ScraperProperties properties;

    public SeleniumBrowserSessionFactory(ScraperProperties properties) {
        this.properties = properties;
    }

    @Override
    public BrowserSession open() {
        ChromeOptions options = new ChromeOptions();
        if (properties.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080", "--disable-dev-shm-usage", "--no-sandbox");
        // Selenium Manager finds (or downloads) a matching Chrome and driver on its own.
        ChromeDriver driver = new ChromeDriver(options);
        return new SeleniumBrowserSession(driver, properties);
    }
}

package com.example.csstats.infrastructure.bo3;

import com.example.csstats.config.ScraperProperties;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

@Component
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
        return new SeleniumBrowserSession(new ChromeDriver(options), properties);
    }
}

package com.example.csstats.infrastructure.bo3;

import com.example.csstats.config.ScraperProperties;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.io.File;

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
        if (properties.chromeBinary() != null) {
            options.setBinary(properties.chromeBinary());
        }

        // Without explicit paths Selenium Manager finds or downloads a matching Chrome and driver itself.
        ChromeDriver driver = properties.chromedriverPath() == null
                ? new ChromeDriver(options)
                : new ChromeDriver(new ChromeDriverService.Builder()
                        .usingDriverExecutable(new File(properties.chromedriverPath()))
                        .build(), options);
        return new SeleniumBrowserSession(driver, properties);
    }
}

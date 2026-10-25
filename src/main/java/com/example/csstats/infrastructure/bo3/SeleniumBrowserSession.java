package com.example.csstats.infrastructure.bo3;

import com.example.csstats.application.ScraperUnavailableException;
import com.example.csstats.config.ScraperProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;

class SeleniumBrowserSession implements BrowserSession {

    private static final String FIRST_STAT = ".c-table-ingame-stats .table-group .table-row .stat";

    private final WebDriver driver;
    private final ScraperProperties properties;
    private long lastLoadAt;

    SeleniumBrowserSession(WebDriver driver, ScraperProperties properties) {
        this.driver = driver;
        this.properties = properties;
        this.driver.manage().timeouts().pageLoadTimeout(properties.pageTimeout());
    }

    @Override
    public Document load(URI url, String... readySelectors) {
        pause();
        try {
            driver.get(url.toString());
            WebDriverWait wait = new WebDriverWait(driver, properties.pageTimeout());
            for (String selector : readySelectors) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
            }
            return Jsoup.parse(driver.getPageSource(), url.toString());
        } catch (WebDriverException exception) {
            throw new ScraperUnavailableException("Could not load " + url, exception);
        }
    }

    @Override
    public Document selectOption(String widgetSelector, String optionText) {
        try {
            WebElement widget = driver.findElement(By.cssSelector(widgetSelector));
            String before = firstStat(widget);

            // The dropdown only reacts to a mousedown on its toggle, and the sticky cookie banner can cover
            // the real element, so events are sent from script rather than by moving the mouse.
            WebElement toggle = widget.findElement(By.cssSelector(".vs__dropdown-toggle"));
            dispatch("mousedown", toggle);

            WebDriverWait wait = new WebDriverWait(driver, properties.pageTimeout());
            By option = By.xpath(".//li[contains(@class,'vs__dropdown-option')][normalize-space()='" + optionText + "']");
            WebElement target = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(widget, option));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", target);

            // The table is briefly empty while the new category loads, so wait for the first row to change.
            wait.until(ignored -> {
                String current = firstStat(widget);
                return !current.isEmpty() && !current.equals(before);
            });
            return Jsoup.parse(widget.getAttribute("outerHTML"));
        } catch (WebDriverException exception) {
            throw new ScraperUnavailableException("Could not select '" + optionText + "'", exception);
        }
    }

    @Override
    public void close() {
        driver.quit();
    }

    private String firstStat(WebElement widget) {
        return widget.findElements(By.cssSelector(FIRST_STAT)).stream()
                .findFirst()
                .map(element -> element.getAttribute("textContent"))
                .orElse("");
    }

    private void dispatch(String eventType, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new MouseEvent(arguments[1], {bubbles: true}))", element, eventType);
    }

    /** Keeps the request rate low: at least {@code requestDelay} between two page loads. */
    private void pause() {
        long wait = lastLoadAt + properties.requestDelay().toMillis() - System.currentTimeMillis();
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ScraperUnavailableException("Interrupted while waiting between requests", exception);
            }
        }
        lastLoadAt = System.currentTimeMillis();
    }
}

package com.example.csstats.infrastructure.bo3;

import org.jsoup.nodes.Document;

import java.net.URI;

/** One browser window that loads pages one after another. */
public interface BrowserSession extends AutoCloseable {

    /** Opens the page and returns it once an element matching every selector in {@code readySelectors} exists. */
    Document load(URI url, String... readySelectors);

    /** Picks an option in the dropdown of the widget and returns the widget as it looks afterwards. */
    Document selectOption(String widgetSelector, String optionText);

    @Override
    void close();
}

package com.example.hltvscraper.hltv.application;

public class ScraperUnavailableException extends RuntimeException {

    public ScraperUnavailableException(String message) {
        super(message);
    }

    public ScraperUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

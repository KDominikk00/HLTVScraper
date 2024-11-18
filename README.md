

![HLTVScraper](https://i.imgur.com/x8nwTXl.png)

# HLTVScraper

HLTVScraper is a powerful web scraper and RESTful API built with Spring Boot, designed to gather and serve statistics for professional CS2 players from HLTV.org. It provides an easy-to-use endpoint that delivers detailed player information, making it ideal for developers and analysts who need access to competitive gaming statistics.

## Features

- **Web Scraping**: Automatically scrapes player statistics from HLTV.org.
- **RESTful API**: Provides a clean and efficient API for accessing player data.
- **Player Stats**: Retrieves a comprehensive range of statistics for CS2 professional players.
- **Easy Integration**: Simple to integrate into your applications or data analysis workflows.

  ## Technologies Used

- **REST API**:
  - **Spring Boot**: A powerful framework for building production-grade applications with ease.
  - **Spring JPA**: Simplifies database interactions using Java Persistence API (JPA).
  - **Lombok**: A Java library used to reduce boilerplate code.

- **Web Scraper**:
  - **Selenium**: Used for automating web browsers to scrape player data.
  - **ChromeDriver**: Required by Selenium for controlling the Chrome browser during the scraping process.

## Integration with CSComparison

HLTVScraper serves as a crucial component of the **CSComparison** app, which allows users to compare the statistics of different CS2 professional players. By utilizing the `/hltv/players` endpoint provided by HLTVScraper, CSComparison can fetch real-time player data, enabling users to make informed decisions based on up-to-date statistics. This integration enhances the overall user experience by providing a seamless way to access and analyze player performance.

## Getting Started

### Prerequisites

Before you begin, ensure you have met the following requirements:

- Java Development Kit (JDK) 11 or higher installed on your machine.
- Apache Maven installed for dependency management and building the project.
- Access to the internet to scrape player data.

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/KDominikk00/HLTVScraper.git
   cd HLTVScraper

2. Build the project using Maven

    ```bash
    mvn clean install

3. Run the application
    ```bash
    mvn spring-boot:run

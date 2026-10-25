FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B package -DskipTests

# The image only serves the API. Scraping runs in the scheduled GitHub workflow, so no browser is needed here.
FROM eclipse-temurin:21-jre
RUN useradd --create-home --uid 10001 app
COPY --from=build /build/target/cs-stats-api-*.jar /app/app.jar

USER app
ENV SCRAPER_SOURCE=remote \
    SCRAPER_SNAPSHOT_URL=https://github.com/KDominikk00/HLTVScraper/releases/download/data/players-snapshot.json \
    SCRAPER_REFRESH_INTERVAL=6h \
    SCRAPER_SNAPSHOT_PATH=/tmp/players-snapshot.json \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

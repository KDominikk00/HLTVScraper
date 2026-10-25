FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B package -DskipTests

# Debian rather than the Temurin image: Ubuntu's chromium package is a snap stub that does not run in containers.
FROM debian:trixie-slim
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-21-jre-headless chromium chromium-driver fonts-liberation curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --uid 10001 app \
    && mkdir /data && chown app /data

COPY --from=build /build/target/cs-stats-api-*.jar /app/app.jar

USER app
ENV SCRAPER_SNAPSHOT_PATH=/data/players-snapshot.json \
    SCRAPER_CHROME_BINARY=/usr/bin/chromium \
    SCRAPER_CHROMEDRIVER_PATH=/usr/bin/chromedriver \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=40"
VOLUME /data
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -fs http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

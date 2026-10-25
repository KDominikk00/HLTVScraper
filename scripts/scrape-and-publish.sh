#!/usr/bin/env bash
# Scrapes bo3.gg from this machine and uploads the snapshot to the "data" release, which the deployed API downloads.
# Runs from a home connection on purpose: bo3.gg shows a Cloudflare challenge to datacenter IPs such as GitHub Actions.
#
# Usage: scripts/scrape-and-publish.sh [max-players]
set -euo pipefail

cd "$(dirname "$0")/.."
export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21+ 2>/dev/null || true)}"
export SCRAPER_MAX_PLAYERS="${1:-${SCRAPER_MAX_PLAYERS:-300}}"

./mvnw -B -q -DskipTests package

mkdir -p out
rm -f out/players-snapshot.json
java -jar target/cs-stats-api-*.jar \
  --spring.profiles.active=scrape \
  --scraper.snapshot-path=out/players-snapshot.json

gh release view data >/dev/null 2>&1 \
  || gh release create data --title "Latest player data" --notes "Updated by scripts/scrape-and-publish.sh. Not a software release."
gh release upload data out/players-snapshot.json --clobber
echo "Published $(date -u +%FT%TZ)"

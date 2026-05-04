#!/bin/bash
# scripts/hooks/post-write-format.sh
# Auto-formats Java files immediately after Claude writes them.
# Requires google-java-format on PATH: brew install google-java-format

FILE="${1:-}"

if [[ -z "$FILE" ]]; then
  exit 0
fi

# Format Java files
if [[ "$FILE" == *.java ]]; then
  if command -v google-java-format &>/dev/null; then
    google-java-format --replace "$FILE"
    echo "Formatted: $FILE"
  else
    echo "Warning: google-java-format not found. Install: brew install google-java-format"
  fi
fi

# Sort and normalise pom.xml (sortpom)
if [[ "$FILE" == "pom.xml" ]]; then
  if command -v mvn &>/dev/null; then
    ./mvnw com.github.ekryd.sortpom:sortpom-maven-plugin:sort -q 2>/dev/null || true
  fi
fi

exit 0
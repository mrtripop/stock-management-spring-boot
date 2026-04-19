#!/bin/bash
# scripts/hooks/post-write-checkstyle.sh
# Runs Checkstyle after every Java file write.
# Claude sees violations and self-corrects before moving to the next file.

FILE="${1:-}"
if [[ -z "$FILE" || "$FILE" != *.java ]]; then
  exit 0
fi

# Only lint if pom.xml exists (we're in a Maven project)
if [[ ! -f "pom.xml" ]]; then
  exit 0
fi
echo "Running Checkstyle on $FILE..."

# Run Checkstyle on the specific file only (fast - not full project)
RESULT=$(./mvnw checkstyle:check \
  -Dcheckstyle.includes="$(basename $FILE)" \
  -q 2>&1)
EXIT_CODE=$?

if [[ $EXIT_CODE -ne 0 ]]; then
  echo "Checkstyle violations in $FILE:"
  echo "$RESULT" | grep -E "\[ERROR\]|\[WARN\]" | head -20
  echo ""
  echo "Fix violations before proceeding."
  exit 1
fi

exit 0
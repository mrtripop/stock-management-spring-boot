#!/bin/bash
# scripts/hooks/pre-commit-scan.sh
# Scans staged Java and config files for hardcoded secrets before commit.
# Uses simple pattern matching — add Gitleaks for production-grade scanning.

CMD="${CLAUDE_COMMAND:-}"
# Only run on git commit commands
if ! echo "$CMD" | grep -q "git commit"; then
  exit 0
fi

echo "Running secret scan on staged files..."
STAGED=$(git diff --staged --name-only 2>/dev/null)
if [[ -z "$STAGED" ]]; then
  exit 0
fi

FOUND=0
while IFS= read -r FILE; do
  
# Skip binary files and non-relevant types
  if [[ ! "$FILE" =~ \.(java|yml|yaml|properties|json|xml)$ ]]; then
    continue
  fi
  
  if ! git show ":$FILE" &>/dev/null; then
    continue
  fi
  
# Check for hardcoded secrets in staged content
  if git show ":$FILE" | grep -iE \
    "(password|passwd|secret|api_key|apikey|token|private_key)\s*[=:]\s*['\"][^$\{][^'\"]{5,}"; then
    echo "SECRET DETECTED in staged file: $FILE"
    echo "All secrets must use environment variable references: \${ENV_VAR:}"
    FOUND=1
  fi
  
# Check for hardcoded JDBC connection strings
  if git show ":$FILE" | grep -E "jdbc:(postgresql|mysql|oracle)://[^$\{]"; then
    echo "HARDCODED CONNECTION STRING in staged file: $FILE"
    echo "Use: \${SPRING_DATASOURCE_URL:}"
    FOUND=1
  fi

done <<< "$STAGED"
if [[ $FOUND -eq 1 ]]; then
  echo ""
  echo "Commit blocked. Fix the above issues before committing."
  exit 1
fi

echo "Secret scan passed."
exit 0
#!/bin/bash
# scripts/hooks/pre-bash-guard.sh
# Blocks dangerous commands before Claude executes them.
# Claude sees the output of exit 1 and adjusts its approach.

CMD="${CLAUDE_COMMAND:-}"
# ── Hard blocks ──────────────────────────────────────────────────────────
# These commands are never acceptable - exit 2 surfaces to user immediately
if echo "$CMD" | grep -qE "rm -rf /|DROP TABLE|DROP DATABASE|TRUNCATE.*--"; then
  echo "BLOCKED: Destructive command detected: $CMD"
  echo "Use a Flyway migration for schema changes, never raw DDL via bash."
  exit 2
fi

if echo "$CMD" | grep -qE "curl .* \| (bash|sh)|wget .* \| (bash|sh)"; then
  echo "BLOCKED: Piping remote content to shell is not permitted."
  exit 2
fi

# ── Soft blocks (Claude can fix and retry) ───────────────────────────────
# Prevent running bare mvn - project uses ./mvnw wrapper always
if echo "$CMD" | grep -qE "^mvn "; then
  echo "Use ./mvnw instead of bare mvn (project uses Maven wrapper)."
  exit 1
fi

# Prevent docker run without --rm (dangling containers)
if echo "$CMD" | grep -qE "docker run " && ! echo "$CMD" | grep -q "\-\-rm"; then
  echo "Add --rm to docker run to avoid dangling containers."
  exit 1
fi

exit 0
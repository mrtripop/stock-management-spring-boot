---
name: security-patching
description: Use when scanning this project for security vulnerabilities, patching vulnerable Maven dependencies, checking whether the project is affected by a CVE or advisory, or remediating Trivy findings. Covers trivy fs/config/image/secret scans and BOM-aware pom.xml patching.
---

# Security Patching (Trivy)

## Overview

Guided workflow to find and fix security vulnerabilities using the locally installed
Trivy. Runs the full loop: **scan → triage → patch → verify → draft commit**, biased
toward safe, minimal, Spring-Boot-BOM-aware dependency bumps.

**Core principle:** Only actionable findings (`CRITICAL`/`HIGH`) drive code changes.
Every pom.xml edit is shown as a diff and applied only on user approval. Nothing is
committed automatically.

## When to Use

- "Scan for vulnerabilities" / "run a security scan"
- "Patch the vulnerable dependencies" / "fix the CVEs"
- "Are we affected by CVE-XXXX-YYYY?"
- After a dependency bump, to confirm a finding is cleared

**Not for:** writing new security features, auth logic, or CI setup.

## Workflow

Create a todo per step and work them in order.

### 1. Preflight
- Confirm Trivy is available: `trivy --version`.
- The bundled vuln DB may be stale. Refresh once per session before scanning:
  `trivy image --download-db-only`
  (Needs network. If offline, note the DB date and proceed — results may miss recent CVEs.)

### 2. Scan
Run each target with JSON output to a temp file, severity limited to actionable levels.
Use `--ignore-unfixed` on the dependency scan so triage only surfaces fixable findings.

```bash
trivy fs --scanners vuln --ignore-unfixed -s CRITICAL,HIGH -f json -o /tmp/trivy-fs.json .
trivy fs --scanners secret -f json -o /tmp/trivy-secret.json .
trivy config -s CRITICAL,HIGH -f json -o /tmp/trivy-config.json .
```

An image scan is in scope only if an image is built (a `Dockerfile` exists at repo root).
Ask which tag to scan, or skip and note it:

```bash
trivy image -s CRITICAL,HIGH --ignore-unfixed -f json -o /tmp/trivy-image.json <image:tag>
```

### 3. Triage
Parse the JSON into a prioritized table. Read **references/triage.md** for the jq
extraction and the table shape. `CRITICAL`/`HIGH` dependency findings with a
`FixedVersion` are actionable. `MEDIUM`/`LOW` are listed FYI only. Findings with no
fixed version → report as "no fix available — mitigate/monitor," never invent a version.

### 4. Patch (dependency findings only)
Apply fixes BOM-aware. Read **references/maven-patching.md** for the decision procedure:
locate where each version is governed (parent BOM, a `<properties>` entry, or an explicit
`<version>`), then patch at the correct level. Present the pom.xml diff and apply only on
approval. A **major** version jump is flagged as potentially breaking and needs explicit
sign-off.

Secrets and misconfig findings are reported for manual remediation — they are not version
bumps, so this skill does not auto-patch them.

### 5. Verify
- Build still passes: `./mvnw clean package`
- Finding is gone: re-run the relevant scan from step 2 and confirm the CVE no longer appears.

### 6. Draft commit
Draft a Conventional Commit message (do **not** commit):

```
fix(security): bump <artifact> to <version> to resolve <CVE>

<one line on the vulnerability and impact, imperative mood>

Ticket: <ID>
```

## Red Flags — stop and reconsider

- About to edit pom.xml without checking whether the version comes from the BOM → read references/maven-patching.md first.
- About to bump across a major version silently → flag it, get approval.
- About to report a fix for a finding with no `FixedVersion` → there is no fix; say so.
- About to `git commit` → this skill drafts the message only.

## Common Mistakes

- Pinning an explicit `<version>` on a starter whose version the BOM already manages, causing drift. Override the BOM property or bump the parent instead.
- Scanning without `--ignore-unfixed`, then "patching" findings that have no available fix.
- Editing pom.xml before rescanning to confirm the finding is real and fixable.

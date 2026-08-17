# Security Patching Skill — Design

**Date:** 2026-08-17
**Status:** Approved (design)
**Topic:** A Claude Code skill that drives Trivy to scan the project, triage findings, and apply managed-version-aware dependency patches.

## Purpose

Give the project a repeatable, guided workflow for finding and remediating security
vulnerabilities using the locally installed Trivy (Homebrew, v0.69.3). The skill runs
the full loop: **scan → triage → patch → verify → draft commit**, biased toward safe,
minimal, BOM-aware dependency bumps on this Spring Boot / Maven codebase.

## Scope decision

**Single orchestrating skill** (`security-patching`), not multiple small skills. Scan and
patch are tightly coupled here — you rarely want one without the other — so one cohesive
workflow keeps invocation simple. The four scan targets are just different Trivy
invocations inside the one workflow.

Out of scope: CI/GitHub Actions integration (Trivy is used locally); auto-committing;
auto-remediating secrets or misconfig (those aren't version bumps — report for manual action).

## Location

```
.claude/skills/security-patching/
├── SKILL.md              # trigger + orchestration checklist (lean)
└── references/
    ├── triage.md         # parsing Trivy JSON into a prioritized table
    └── maven-patching.md  # managed-version-aware pom.xml patch playbook
```

Supporting playbooks live in `references/` so `SKILL.md` stays focused on the workflow.

## Workflow

1. **Preflight**
   - Verify `trivy` is on PATH (`trivy --version`).
   - Refresh the vulnerability DB (`trivy image --download-db-only`) — the installed DB
     is stale (last updated 2024-04-05).

2. **Scan** — JSON output, severity filtered to `CRITICAL,HIGH`:
   - `trivy fs .` — Maven dependency vulnerabilities (the patch driver)
   - `trivy config .` — Dockerfile / IaC / YAML misconfiguration
   - `trivy image <name>` — container image incl. OS packages (a `Dockerfile` exists at
     repo root, so this step is live; if no image is built, note and skip)
   - `trivy fs --scanners secret .` — hardcoded secrets/keys

3. **Triage** — parse JSON into a prioritized table: severity, package, installed →
   fixed version, CVE ID. `CRITICAL`/`HIGH` are actionable; `MEDIUM`/`LOW` reported as
   FYI only (per approved severity policy).

4. **Managed-version-aware patch** (dependency findings only) — for each actionable dep,
   determine where its version is governed:
   - Spring Boot parent BOM (inherited), a `<properties>` entry, or `<dependencyManagement>`.
   - Patch at the correct level (override the property, or add/adjust a managed version)
     rather than pinning the artifact ad hoc.
   - Prefer the lowest version that clears the CVE (patch/minor over major).
   - Present the pom.xml diff; **apply only on user approval**.

5. **Verify**
   - `./mvnw clean package` — confirm it still builds.
   - Re-run the relevant Trivy scan — confirm the finding is cleared.

6. **Wrap up** — draft a Conventional Commit message (`fix(security): …`, imperative body
   describing business intent, `Ticket:` footer per project convention). Do **not** commit.

## Guardrails

- Never auto-commit — draft the message only.
- A **major** version jump is flagged as potentially breaking and requires explicit
  approval; never applied silently.
- If a finding has **no `fixedVersion`**, report as "no fix available — mitigate/monitor";
  never invent a version.
- Secrets and misconfig findings are reported for manual remediation, not auto-patched.
- Surgical pom.xml edits only, matching existing style (per AGENTS.md).

## Success criteria

- Invoking the skill runs all four scans and produces a prioritized triage table.
- For a CRITICAL/HIGH dependency finding with a known fix, the skill produces a correct
  BOM/property-aware pom.xml diff, and after approval the project still builds and the
  finding no longer appears in a rescan.
- A ready-to-use Conventional Commit message is drafted; nothing is committed automatically.

# Triage: parsing Trivy JSON

Turn raw Trivy JSON into a prioritized, human-readable table so patching targets the
right findings.

## Extract dependency vulnerabilities

`trivy fs` groups findings under `.Results[].Vulnerabilities[]`. Pull the fields that
matter for patching:

```bash
jq -r '
  .Results[]
  | select(.Vulnerabilities != null)
  | .Target as $target
  | .Vulnerabilities[]
  | [.Severity, .PkgName, .InstalledVersion, (.FixedVersion // "—"), .VulnerabilityID, $target]
  | @tsv
' /tmp/trivy-fs.json
```

Present as a table, most severe first:

| Severity | Package | Installed | Fixed | CVE | Source |
|----------|---------|-----------|-------|-----|--------|
| CRITICAL | org.example:lib | 1.2.0 | 1.2.4 | CVE-2025-1234 | pom.xml |

## Actionability rules

- **Actionable:** `CRITICAL` or `HIGH` **and** `FixedVersion` is present. These drive
  pom.xml patches (step 4).
- **FYI only:** `MEDIUM` / `LOW` — list them, do not auto-patch.
- **No fix available:** `FixedVersion` empty/absent — report as "no fix available —
  mitigate/monitor." Never invent a version. (`--ignore-unfixed` on the scan drops these
  from the dependency scan, but image/OS scans may still surface them.)

## Direct vs transitive

`PkgName` tells you the vulnerable artifact, not whether you declare it directly.
Check pom.xml:

- **Direct** (appears in `<dependencies>`) → patch there or via its property.
- **Transitive** (not in pom.xml) → the fix usually comes from bumping the parent
  it arrives through, or the Spring Boot parent BOM. See maven-patching.md.

## Secrets and misconfig

- `trivy fs --scanners secret` → `.Results[].Secrets[]` (fields: `RuleID`, `Title`,
  `StartLine`, `Match`). Report file + line for manual removal and rotation. Do **not**
  echo the secret value into the commit message or logs.
- `trivy config` → `.Results[].Misconfigurations[]` (fields: `ID`, `Title`, `Severity`,
  `Resolution`). Report with the suggested `Resolution`; these are manual fixes.

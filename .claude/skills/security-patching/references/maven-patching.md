# Maven patching: managed-version-aware

This project inherits from `spring-boot-starter-parent` (currently `3.4.2`). Most
dependency versions come from that BOM, not from pom.xml. Patch at the level where the
version is actually governed — otherwise you create version drift the BOM will fight.

## Decision procedure

For each actionable finding, find where the vulnerable artifact's version is set, in this
order:

1. **Explicit `<version>` on the dependency in pom.xml**
   → Bump that `<version>` directly to the fixed version.
   Example pinned deps here: `postgresql`, `jjwt-*`, `springdoc-openapi-*`, `commons-csv`,
   `opentelemetry-api`, `lombok`, `totp`.

2. **A `<properties>` entry referenced by `${...}`**
   → Bump the property. Example: `org.mapstruct.version`.

3. **No version in pom.xml — managed by the Spring Boot BOM**
   Two options, in order of preference:
   a. **Override the BOM's version property.** Spring Boot's parent exposes override
      properties (e.g. `<snakeyaml.version>`, `<jackson-bom.version>`, `<netty.version>`).
      Add the property to `<properties>` with the fixed version. Confirm the exact
      property name against the Spring Boot managed-dependencies list before using it.
   b. **Bump the parent** `spring-boot-starter-parent` version — the cleanest fix when a
      newer Spring Boot release already ships the patched transitive dependency, and it
      clears several findings at once. This is a larger change: flag it and get approval.
   c. If neither fits (artifact not managed by the BOM and not declared), add a
      `<dependencyManagement>` entry pinning the fixed version.

## Confirming what governs a version

```bash
# Is it declared with an explicit version in pom.xml?
grep -n -A2 '<artifactId>PKG_ARTIFACT</artifactId>' pom.xml

# What version does Maven actually resolve (and via which path)?
./mvnw dependency:tree -Dincludes=GROUP:ARTIFACT
```

`dependency:tree` shows the resolved version and whether the artifact is direct or
transitive — decisive when pom.xml has no `<version>` for it.

## Choosing the target version

- Prefer the **lowest** version that clears the CVE (Trivy's `FixedVersion`), staying
  within the current major line — lowest breakage risk.
- Same major, higher minor/patch → apply after showing the diff.
- **Different major version** → potentially breaking API change. Flag it explicitly,
  summarize likely impact, and require sign-off before applying.

## After editing

- `./mvnw dependency:tree -Dincludes=GROUP:ARTIFACT` to confirm the resolved version
  changed as intended (catches a property override that didn't take because the artifact
  is managed under a different property name).
- Then run the full verify step (`./mvnw clean package` + rescan) from SKILL.md.

## Style

- Surgical edits only: change the version, nothing else. Match existing pom.xml
  indentation and ordering (per AGENTS.md).
- Do not reformat or reorder unrelated dependencies.

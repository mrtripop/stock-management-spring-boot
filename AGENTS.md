# Agents

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. Expect: asking before assuming, touching fewer lines, and
writing less code. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and
clarifying questions come before implementation rather than after mistakes.

---

## Project Specific

### Tech Stack

Java 17 | Spring Boot 3.4.2 | Maven | PostgreSQL 14.6 | Redis 7.2 | H2 (test)
Lombok | MapStruct 1.6.0 | springdoc-openapi 2.8.6 | Log4j2 | Spring AOP
Google Java Format | Conventional Commits (commitlint)

### Commands

- `./mvnw spring-boot:run` — Start app (needs postgres + redis)
- `./mvnw clean compile` / `./mvnw clean package` — Compile / Build JAR
- `./mvnw test` / `./mvnw test -Dtest=ClassName` — Run tests

### Commit Conventions

Conventional Commits format. Message body describes business intent in imperative mood. Footer references a searchable
ticket ID.

```
feat(product): add scheduled product deletion

Soft-delete products past their expiry date nightly
to keep the catalog clean for pharmacy staff.

Ticket: JIRA-123
```

### Searching Code

Prefer dedicated tools and simple bash commands. Do not use `find -exec` — it triggers permission prompts.

| Goal                     | Use                                                         |
|--------------------------|-------------------------------------------------------------|
| Read a known file        | `Read` tool                                                 |
| Find files by name/glob  | `find . -name "*.java"` or `find . -path "*/controllers/*"` |
| Search file contents     | `grep -rn "pattern" src/` or `grep -rl "pattern" src/`      |
| Find a symbol/definition | `LSP` tool (`goToDefinition`, `workspaceSymbol`)            |
| Find all references      | `LSP` tool (`findReferences`)                               |

**Never** use `find -exec` — run `find` to list files, then use `Read` tool to read them.
**Never** use `cat`, `head`, `tail` — use `Read` tool instead.

### Package Structure

Base: `com.mrtripop`

```
com.mrtripop/
├── product/          # Product catalog
├── clinical/         # Pharmacy — molecules, brands, stores, store-products, audit ledger
├── inventory/        # Stock — batches, store-stock, unit-conversions, FEFO deduction
├── location/         # Addresses, warehouses
├── order/            # Order management
├── transaction/      # Transaction processing
├── users/            # User management
│
├── model/            # ResponseBody<T>, BaseQueryParams
├── exception/        # ApplicationException, NotFoundException, ErrorResponse, CustomControllerAdvice
├── config/           # SecurityConfig, RedisConfig, AppConfig, OpenAPIConfig
├── component/fileparser/  # FileParser strategy (CSV/JSON/XML), FileParserFactory
├── constant/         # BaseStatusCode (interface), ErrorCode, SuccessCode
├── aspect/           # GlobalAspect (AOP method logging)
├── util/             # Shared utilities
```

### Detailed Rules

Each file below contains correct/incorrect examples. **Read the relevant file before implementing.**

| File                            | Scope                                                                                                                  |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `.claude/rules/api-design.md`   | Endpoints: URLs, HTTP methods, request/response DTOs, validation on endpoints, pagination, response wrapping, file ops |
| `.claude/rules/coding-style.md` | Java language: naming, formatting, comments, magic numbers/strings, immutability, collections, streams, concurrency    |
| `.claude/rules/testing.md`      | Tests: naming, @Nested grouping, AAA pattern, fixtures with constants, unit/integration setup                          |
| `.claude/rules/security.md`     | Security: input validation, SQL injection, XSS, auth, secrets, CORS, response filtering, logging                       |
| `.claude/rules/performance.md`  | Performance: N+1 prevention, DB indexes, lazy loading, batch operations, connection management, async                  |

<!-- gitnexus:start -->

# GitNexus — Code Intelligence

This project is indexed by GitNexus as **stock-management-spring-boot** (3554 symbols, 9210 relationships, 296 execution
flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run
  `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected
  processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and
  execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping.
  It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use
  `gitnexus_context({name: "symbolName"})`.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Resources

| Resource                                                      | Use for                                  |
|---------------------------------------------------------------|------------------------------------------|
| `gitnexus://repo/stock-management-spring-boot/context`        | Codebase overview, check index freshness |
| `gitnexus://repo/stock-management-spring-boot/clusters`       | All functional areas                     |
| `gitnexus://repo/stock-management-spring-boot/processes`      | All execution flows                      |
| `gitnexus://repo/stock-management-spring-boot/process/{name}` | Step-by-step execution trace             |

## CLI

| Task                                         | Read this skill file                                        |
|----------------------------------------------|-------------------------------------------------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md`       |
| Blast radius / "What breaks if I change X?"  | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?"             | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md`       |
| Rename / extract / split / refactor          | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md`     |
| Tools, resources, schema reference           | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md`           |
| Index, status, clean, wiki CLI commands      | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md`             |

<!-- gitnexus:end -->

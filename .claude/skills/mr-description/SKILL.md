---
name: mr-description
description: Use when writing or updating a pull request (GitHub) or merge request (GitLab) description — fills an industry-standard template (Summary/Related Issue/Changes/Testing/Checklist) from the actual diff and test run, then posts it via gh or glab (rtk-proxied when the host needs it), gated on approval.
---

# Pull / Merge Request Description

Fills a standard PR/MR description template from ground truth (the real diff,
the real test run) and posts it to the host — GitHub (`gh`) or GitLab
(`glab`). Not a place to restate the title — each section earns its place by
answering a question a reviewer would otherwise have to ask.

## Platforms

This skill covers both hosts. The workflow is identical; only the CLI and the
terminology differ.

| | GitHub | GitLab |
|---|---|---|
| Unit | pull request (PR), `#5` | merge request (MR), `!5` |
| CLI | `gh` | `glab` (via `rtk proxy` behind a corporate proxy) |
| Field | PR body | MR `description` |

Detect the host from `git remote -v` (github.com → GitHub, a GitLab host →
GitLab); if it's ambiguous, ask which host before posting. **Below, "MR" is
shorthand for "the PR or MR"** — the guidance applies to both.

**Write for the reviewer's judgment call, not for a complete record.** Every
line should change what the reviewer decides or how confidently they decide
it. Cut anything that's true but doesn't move that needle — how you
personally validated a test (which timezones you ran it under, which IDE
diagnostics were stale noise, how many drafts it took) is process trivia, not
review input. If a detail doesn't help someone approve, request changes, or
ask a sharper question, it's cognitive load with no payoff — leave it out
even if it's accurate and even if it took real effort to establish.

## When to Use

- "Write the PR/MR description for #5 (GitHub) or !5 (GitLab)"
- "Summarize what we tested to the PR"
- "Update the description on <PR/MR url>"

## When NOT to Use

- Responding to review comments/threads on an existing PR/MR — that's
  `addressing-review-comments`.
- Writing commit messages — different audience, different length.

## Prerequisites

- The host CLI is installed and authenticated: `gh auth status` (GitHub) or
  `glab auth status` (GitLab).
- Every CLI call that reaches the host runs with the sandbox disabled — it
  needs network (and, for signing, the keyring).
- **Corporate TLS proxy:** if the host sits behind a proxy that breaks cert
  verification inside the sandbox (typical for self-managed GitLab), wrap the
  CLI in `rtk proxy <cli>` (`rtk-ai/rtk`, not the Rust Type Kit name
  collision) and confirm `rtk --version` first. Public github.com over `gh`
  usually needs no proxy — use plain `gh`.

## The Template

```markdown
## Summary
<!-- What does this change do, and why? One short paragraph, not a changelog. -->

## Related Issue
<!-- Jira/GitHub/issue link. Omit the section if there isn't one. -->

## Changes
<!-- Bullet list, one line per unit of behavior. Lead each bullet with its main
     action (a verb: "Added", "Tightened", "Regenerated" …), then only the detail
     that makes it checkable. Compact — business or technical wording, whichever
     reads clearer for the change; not a class/file rename log. Reading the diff
     already tells an engineer *what* changed; this section gives the *intent*
     without re-deriving it. Name the concrete class/field/format only where it
     makes the bullet checkable (a field name, an exact format string) — never
     lead with it, and cut anything the action already implies. -->

## Testing
<!-- What was actually run, and what did it prove? State plainly if only unit
     tests ran and manual/integration testing did not happen; don't let the
     section's presence imply more coverage than there is. -->

**`<TestClassName>`** (`<passing>/<total>` passing)

| Given | When | Then | Result |
|---|---|---|---|
| <starting state / input> | <action invoked> | <expected outcome> | ✅ Pass |

## Integration Testing
<!-- Only if the user ran it manually — never fabricate this section. A real
     captured input/output message pair (JSON + screenshot if available) is
     stronger evidence than any synthetic table; paste it in as-is. -->

- Msg input
  ```json
  { ... actual message consumed ... }
  ```
- Msg output
  ```json
  { ... actual message produced ... }
  ```

## Checklist
- [ ] Unit tests added/updated and passing
- [ ] Integration / manual testing (state N/A explicitly if skipped, don't leave unchecked with no note)
- [ ] Documentation updated (if needed)
- [ ] Self-reviewed the diff
```

Sections are additive, not mandatory scaffolding — drop `Related Issue` if
there's no ticket, drop `Testing` only if the MR truly has zero test surface
(rare; justify it in `Changes` if so). Never ship a section with placeholder
text still in it — an empty section is more honest than a templated lie.

`Changes` bullets: lead with the action, keep to one compact line, business or
technical wording — not a class-by-class log. State the fact and skip the
justification clause once the fact alone is checkable against the diff:

```
# Class-log shape (avoid — a reviewer can read this straight from the diff)
- `TransformationHelper.toUtcIso8601(Date)`: converts epoch-ms to ISO-8601 UTC.
- `ItemLocDTO`: added `productStatusUpdateDate` field.

# Business shape, with a justification tail (avoid — the "so that" clause is
# speculation about downstream consumers the diff doesn't actually establish)
- Every item-location message now tells consumers when the item's status at
  that store last changed (`productStatusUpdateDate`), so pricing/labeling
  can react to a status change without querying RMS directly.

# Business shape, fact only (use — trust the reviewer to draw the "so that")
- Item-location messages now include when the item's status last changed
  (`productStatusUpdateDate`).
```
The field/class name still belongs in the bullet — as the detail that makes
the business claim checkable against the diff — it just isn't the subject
of the sentence. Keep the "so that" tail only when the benefit itself is the
thing being reviewed (e.g. a perf fix where the *reason* is the whole point);
drop it when it's just narrating an assumed downstream use case.

`Testing` uses one Given/When/Then table per test class/suite, one row per
test case — this is the industry-standard shape for readable test summaries
(mirrors Gherkin/BDD scenario tables) and lets a reviewer scan what's covered
without opening the test file. Group rows under a `**\`ClassName\`** (x/y
passing)` heading per suite; don't merge unrelated suites into one table —
*unless* a real `Integration Testing` capture now proves that suite's
scenario end-to-end, in which case drop the redundant synthetic table for
that scenario (see below) rather than keeping two proofs of the same thing.

## Integration Testing: Draft Scenarios, Don't Fabricate Results

You have no access to the real broker, test environment, or whatever system
actually carries the end-to-end message — so you cannot produce a real
`Integration Testing` capture yourself. Don't approximate one. Two honest
options:

- **The user already ran it and pasted the result** (a real input/output
  message pair, a screenshot). Treat that as ground truth — drop it into the
  `Integration Testing` section as-is, and if it fully demonstrates a
  scenario your synthetic `Testing` table also covers, remove the redundant
  table rows for that scenario rather than leaving two proofs side by side.
  Edge cases the real capture *doesn't* reach (e.g. a null-fallback path, a
  precision edge unreachable from real input) stay in the unit-level table —
  say so explicitly if it's not obvious why a case is unit-only.

- **It hasn't been run yet.** Draft the scenario list and hand it back to the
  user to execute — don't post anything to as if it happened. Each
  scenario needs enough for them to actually act on it without re-deriving it:

  1. **What to set up** — the exact input value/state to produce (e.g. "an
     ITEM_LOC message with `STATUS_UPDATE_DATE` null or absent"), and where it
     needs to enter the system (topic, table, tool).
  2. **What to check** — the specific output field and the shape of a correct
     result (e.g. "`productStatusUpdateDate` is close to send time, in UTC,
     `+00:00` offset — not a fixed literal, since it's a fallback-to-now
     case").
  3. **Why it matters enough to bother** — one clause, only if not obvious
     from the scenario name (e.g. skip a case entirely, with a note, if it's
     already proven by a unit test and unreachable from real input — don't
     pad the list with scenarios nothing depends on).

  Present this as a plain list in the conversation (or as a "Pending" note
  in the MR only if asked) — not as a filled-in `Integration Testing` section
  with invented JSON.

## Filling It In: Ground Truth, Not Vibes

Before writing a word, gather the actual facts — don't reconstruct the diff
from memory of the conversation:

```bash
git log --oneline origin/<target-branch>..HEAD          # commits in scope
git diff origin/<target-branch>...HEAD --stat            # what changed
git show <commit> -- <file>                               # exact diff per file, if summarizing one commit
```

For the Testing section specifically, run the tests yourself and cite the
real numbers — the same rule `addressing-review-comments` enforces for
verification before resolving a thread applies here: **you run it, not the
user, and if a count looks surprising, find out why before citing it.**

```bash
mvn -o test -Dtest=<ClassName>      # or the project's real test command
```

If the project's "run everything" command doesn't actually run everything
(stale surefire config, tag filters, whatever) — and the individual class
runs disagree with the full-suite count — don't paper over it. Cite the
class-level numbers you verified and note the discrepancy to the user in
your own reply; don't advertise unverified project-wide coverage.

## Posting It

The description is a single field on the PR/MR — update it in place rather
than appending a comment, unless the user asked for a comment specifically.
Write the drafted markdown to a file first, then set it. Whichever host,
**read the rendered result back — don't just trust a 200 / success exit.**

### GitHub (`gh`)

`gh pr edit` reads a file correctly with `--body-file` (no `@file` gotcha):

```bash
gh pr edit <n> --body-file pr-description.md
gh pr view <n>          # read the rendered body back to verify
```

### GitLab (`glab`)

**Known gotcha:** `glab api --field description=@path/to/file` looks like it
should read the file (glab's own help says `@filename` is supported for
`--field`), but in practice this has been observed to post the literal string
`@path/to/file` as the description instead of the file's contents — the read
silently doesn't happen. Don't trust `@file` here. Load the content into a
shell variable and pass it as a `--raw-field`, which sends it byte for byte
with no type coercion:

```bash
DESC="$(cat mr-description.md)"
rtk proxy glab api -X PUT "projects/<url-encoded-fullpath>/merge_requests/<iid>" \
  --raw-field "description=$DESC"

rtk proxy glab mr view <iid> -R <group/subgroup/repo>   # read it back to verify
```

`--raw-field` (unlike `--field`) never tries to interpret the value — no
`@file` handling, no `true`/`false`/number coercion — so there's no
silent-failure mode to hit. (Drop the `rtk proxy` prefix if this GitLab host
isn't behind the corporate proxy — see Prerequisites.)

## Approval Gate

Same as `addressing-review-comments`: this is an outward-facing write visible
to every reviewer on the PR/MR. Draft the full filled-in description, show it,
and post only on an explicit yes — even when the user asked you to "just do
it", a one-line confirmation of the drafted content costs little against the
cost of a wrong or embarrassing description sitting in front of a reviewer.
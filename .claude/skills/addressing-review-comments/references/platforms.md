# Per-host commands: fetch review comments, reply, resolve

Detect the host from `git remote -v`, confirm the CLI is authenticated, then use the
matching section. Extract only needed fields to keep context lean. `<n>` / `<iid>` is the
PR / MR number.

---

## GitHub — `gh`

`gh api` substitutes `{owner}` and `{repo}` from the current repository automatically.

```bash
# Auth check
gh auth status

# PR metadata (note head/base branch for the commit step)
gh pr view <n> --json title,headRefName,baseRefName,url,state,body,files

# Inline review comments (path, line, body, and the diff hunk they anchor to)
gh api "repos/{owner}/{repo}/pulls/<n>/comments" \
  --jq '.[] | {user:.user.login, path:.path, line:.line, body:.body, diff_hunk:.diff_hunk}'

# General PR comments (the conversation tab)
gh api "repos/{owner}/{repo}/issues/<n>/comments" \
  --jq '.[] | {user:.user.login, body:.body}'

# Review summaries (APPROVED / CHANGES_REQUESTED / COMMENTED)
gh api "repos/{owner}/{repo}/pulls/<n>/reviews" \
  --jq '.[] | {user:.user.login, state:.state, body:.body}'
```

Reply / resolve (optional, step 10 — confirm before posting):

```bash
# Reply under a specific inline review comment thread (needs the comment's id)
gh api "repos/{owner}/{repo}/pulls/<n>/comments/<comment_id>/replies" -f body="..."

# Post a general PR comment
gh pr comment <n> --body "..."

# Resolve a review thread (advanced — thread ids come from GraphQL reviewThreads)
gh api graphql -f query='mutation {
  resolveReviewThread(input:{threadId:"<threadId>"}) { thread { isResolved } }
}'
```

---

## GitLab — `glab`

GitLab calls them **merge requests**; comment threads are **discussions**.

```bash
# Auth check
glab auth status

# MR details (title, source/target branch, state, description)
glab mr view <iid>

# All discussions as JSON (each has notes[] with body, author, position)
glab mr note list <iid> -F json

# Inline (diff) comments only; or just the unresolved ones
glab mr note list <iid> --type diff
glab mr note list <iid> --state unresolved
```

Reply / resolve (optional, step 10 — confirm before posting):

```bash
# Add a general comment
glab mr note create <iid> -m "..."

# Reply to an existing discussion (discussion id from `glab mr note list -F json`,
# 8-char prefix is enough)
glab mr note create <iid> --reply <discussionId> -m "..."

# Resolve / unresolve a thread
glab mr note create <iid> --resolve   --reply <discussionId> -m "Done"
glab mr note create <iid> --unresolve --reply <discussionId> -m "Reopening"

# Diff comment on a specific line
glab mr note create <iid> --file <path> --line <n> -m "..."
```

---

## Other hosts (Bitbucket, Azure DevOps, self-managed)

No assumed CLI. **Ask the user** for the host, API base URL, and a token, then drive the
REST API with `curl` (or the host's official CLI if they have one). Confirm the exact
endpoints against the host's current API docs — do not guess paths. Typical shapes:

- **Bitbucket Cloud:** `GET /2.0/repositories/{workspace}/{repo}/pullrequests/{id}` and
  `.../pullrequests/{id}/comments`; POST to the same comments endpoint to reply.
- **Azure DevOps:** `GET .../pullRequests/{id}/threads` (each thread has `comments[]`);
  POST a comment to a thread to reply, PATCH the thread `status` to resolve.

Use the same workflow (fetch → categorize → fix → verify → commit → reply); only the
transport changes.

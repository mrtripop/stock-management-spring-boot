---
name: e2e-audit-generator
description: Use this skill whenever a feature implementation is complete and needs a final verification plan. Trigger this when the user asks for an "audit guide", "E2E test plan", "verification document", or says "the feature is done, now let's verify it". This skill transforms technical implementation details into a rigorous, agent-executable audit guide to ensure zero gaps between backend and frontend behavior.
---

# E2E Audit Generator

You are an expert Quality Assurance Architect. Your goal is to produce a "bulletproof" audit guide that ensures a feature is 100% correct, covering every possible execution path, including those not explicitly mentioned in the original specs.

## The Audit Philosophy
A spec tells you what *should* happen. The code tells you what *actually* happens. A perfect audit guide bridges this gap by finding the "hidden" behaviors in the code (e.g., a specific 400 error for a weird edge case) and turning them into a verification step.

## Workflow

### 1. Evidence Gathering
Analyze the following sources in order:
1. **The Implementation Plan:** To understand the intended goal and task boundaries.
2. **The Design Specs:** To identify the "Happy Path" and stated requirements.
3. **The Final Code (Diffs/Files):** Search for:
   - Error handling blocks (`catch`, `if (error)`, `throw`) that weren't in the spec.
   - Conditional logic (`if/else`) that creates different behavioral branches.
   - Role-based checks (`@PreAuthorize`, `useHasRole`) to identify all required permission levels.
   - External dependencies (Redis, DB, Third-party APIs) that could fail.

### 2. Scenario Expansion
For every technical detail found, generate a test scenario. Do not just list the happy path. You MUST include:
- **The Happy Path:** The ideal user journey.
- **RBAC/Security:** What happens to every role (Admin vs. User vs. Guest)?
- **Edge Cases:** Empty states, maximum limits, invalid inputs.
- **Error States:** How does the UI respond to the specific backend errors found in the code?
- **State Transitions:** Does the UI correctly reflect async transitions (e.g., `IDLE` $\rightarrow$ `PROCESSING` $\rightarrow$ `COMPLETED`)?

### 3. Output Generation
Write the guide to `docs/superpowers/audit/YYYY-MM-DD-<feature>-audit.md`.

#### Mandatory Structure:
# E2E Audit Guide: [Feature Name]

## 1. Feature Overview
A concise summary of what this feature does and why it matters.

## 2. Technical Mapping
A table mapping the Frontend UI components to their corresponding Backend API endpoints.
| UI Element/Action | API Endpoint | Method | Expected Success Code |
| :--- | :--- | :--- | :--- |
| [e.g., Reconcile Button] | `/api/v1/...` | `POST` | `INV2004` |

## 3. Test Scenarios
Group scenarios by intent. Each scenario must be a "recipe" an agent can follow.

### [Scenario Name]
- **Goal:** What are we proving?
- **Preconditions:** (e.g., "User is logged in as Admin", "Store has 0 stock")
- **Steps:** 
  1. [Action] $\rightarrow$ Expected: [Result]
  2. [Action] $\rightarrow$ Expected: [Result]
- **Verification:** How to prove it worked (e.g., "Check DB for X", "Verify Toast message Y").

## 4. Final Verification Checklist
A high-level punch list of all critical "Must-Haves" for the feature to be considered "Shipped".

## 5. Audit Agent Invocation
Provide a specific prompt for a subagent to execute this audit.
**Prompt:**
"I need you to act as a Verification Agent. Read the audit guide at `[path-to-this-file]`. Execute every scenario described. For each step, provide evidence (logs, screenshots, or API responses). Report a final status: PASS, FAIL, or PARTIAL for each scenario."

---

## Quality Gate
Before finalizing the document, ask yourself: *"If I were a malicious or confused user, where would I break this feature?"* Add a scenario for that.

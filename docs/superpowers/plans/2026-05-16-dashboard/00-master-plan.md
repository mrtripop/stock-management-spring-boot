# Pharmacy Dashboard Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the demo-ui pharmacy stock management dashboard with Tailwind CSS, Headless UI, Atomic Design, and React Query.

**Architecture:** Atomic Design component hierarchy (atoms → molecules → organisms → templates → pages) with design tokens for theming. Each page composed from shared organisms. React Query replaces the custom useApi hook for server state.

**Tech Stack:** React 19, Vite 5, Tailwind CSS v4, @headlessui/react, @heroicons/react, @tanstack/react-query, sonner, Storybook

---

## File Structure

```
demo-ui/src/
├── styles/tokens.css              # Design tokens as CSS custom properties
├── foundations/theme.js            # JS token map (not used by Tailwind v4 directly, but for reference)
├── atoms/                         # 6 atom components
├── molecules/                     # 5 molecule components
├── organisms/                     # 7 organism components
├── templates/                     # 3 template components
├── pages/                         # 9 page components (rewritten)
├── lib/api.js                     # UNCHANGED — keep existing API client
├── lib/hooks.js                   # REWRITTEN — React Query wrappers
├── App.jsx                        # REWRITTEN — QueryClient + new router
├── main.jsx                       # UPDATED — QueryClientProvider wrapper
└── index.css                      # REWRITTEN — Tailwind imports only

demo-ui/.storybook/                # NEW — Storybook configuration
demo-ui/vite.config.js             # UPDATED — add Tailwind plugin
demo-ui/package.json               # UPDATED — new dependencies
```

## Task Execution Order

Tasks must be completed in order. Each task depends on the previous one.

| # | Task | Chunk File | Description |
|---|------|-----------|-------------|
| 1 | Infrastructure | [01-infrastructure.md](01-infrastructure.md) | Install deps, Tailwind, Vite, tokens, theme, index.css |
| 2 | Atoms | [02-atoms.md](02-atoms.md) | Button, Badge, Input, Select, Spinner, Icon |
| 3 | Molecules | [03-molecules.md](03-molecules.md) | StatCard, PageHeader, SearchBar, Pagination, FormField |
| 4 | React Query Hooks | [04-hooks.md](04-hooks.md) | Rewrite lib/hooks.js with @tanstack/react-query |
| 5 | Layout Organisms | [05-layout-organisms.md](05-layout-organisms.md) | Sidebar, TopBar |
| 6 | Data Organisms | [06-data-organisms.md](06-data-organisms.md) | DataTable, FormDrawer, AlertDialog |
| 7 | Dashboard Organisms | [07-dashboard-organisms.md](07-dashboard-organisms.md) | ExpiryAlerts, ActivityFeed |
| 8 | Templates + App Root | [08-templates-app.md](08-templates-app.md) | AdminLayout, TablePage, FormPage, App.jsx, main.jsx |
| 9 | Login Page | [09-login-page.md](09-login-page.md) | Login/Register + MFA |
| 10 | Dashboard Page | [10-dashboard-page.md](10-dashboard-page.md) | Quick actions + stats + alerts + activity |
| 11 | Products Page | [11-products-page.md](11-products-page.md) | Product CRUD |
| 12 | Inventory Page | [12-inventory-page.md](12-inventory-page.md) | Batch management + stock-in/deduct |
| 13 | Clinical Page | [13-clinical-page.md](13-clinical-page.md) | 3 tabs: Stores, Molecules, Brands |
| 14 | Read-Only Pages | [14-readonly-pages.md](14-readonly-pages.md) | Orders, Transactions, Locations, Users |
| 15 | Storybook | [15-storybook.md](15-storybook.md) | Configuration + foundation stories |

## Key References

- **Design spec:** `docs/superpowers/specs/2026-05-16-stock-management-dashboard-design.md`
- **API client:** `demo-ui/src/lib/api.js` — DO NOT MODIFY
- **Existing pages:** `demo-ui/src/pages/*.jsx` — all rewritten in tasks 9-14
- **Design tokens:** Defined in spec section 2, implemented in task 1

## Verification

After all tasks complete:
1. `cd demo-ui && npm run dev` — app starts without errors
2. Login page shows teal gradient background with centered card
3. After login, icon sidebar (56px) + topbar (56px) + page outlet renders
4. All 8 pages render with new teal theme
5. CRUD operations work (Products create/edit/delete)
6. React Query devtools show query states
7. `npm run build` completes without errors

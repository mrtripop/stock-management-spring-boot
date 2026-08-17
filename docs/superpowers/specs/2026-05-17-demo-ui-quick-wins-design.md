# Demo-UI Quick Wins — Design Spec

**Date:** 2026-05-17
**Scope:** 7 high-impact, low-risk UI improvements across all four areas (polish, dashboard, tables, navigation)

## Summary

Fix the most visible usability and polish issues in the demo-ui frontend without architectural changes. All changes are scoped to existing components — no new pages, no new dependencies.

---

## 1. Fix Text Sizes (Readability)

**Problem:** Components use text as small as 7-10px, below readable minimums.

**Files affected:** `ExpiryAlerts.jsx`, `ActivityFeed.jsx`, `DataTable.jsx`, `StatCard.jsx`

**Changes:**
- Minimum text size: `11px` (captions, timestamps)
- Secondary text: `12px`
- Body text / table content: `13px`–`14px`
- Table headers: `12px` (already uppercase via tracking, just larger)

**Specific replacements:**

| File | Current | Fixed |
|------|---------|-------|
| `ExpiryAlerts.jsx` line 34 | `text-[10px]` | `text-xs` (12px) |
| `ExpiryAlerts.jsx` line 35 | `text-[8px]` | `text-[11px]` |
| `ExpiryAlerts.jsx` line 40 | `text-[9px]` | `text-[11px]` |
| `ExpiryAlerts.jsx` line 43 | `text-[7px]` | `text-[11px]` |
| `ActivityFeed.jsx` line 27 | `text-[10px]` | `text-xs` (12px) |
| `ActivityFeed.jsx` line 28 | `text-[8px]` | `text-[11px]` |
| `ActivityFeed.jsx` line 29 | `text-[8px]` | `text-[11px]` |
| `DataTable.jsx` line 24 | `text-[10px]` | `text-xs` (12px) |

---

## 2. Replace Emojis with SVG Icons

**Problem:** Emojis render inconsistently across OS and look unprofessional.

**Files affected:** `ActivityFeed.jsx`, `ExpiryAlerts.jsx`, `TopBar.jsx`

**Changes:**

- **ActivityFeed.jsx** — Replace `TYPE_CONFIG` emoji icons with SVG inline icons using the same pattern as `Sidebar.jsx` tooltip icons. Remove emoji from section header title.
- **ExpiryAlerts.jsx** — Replace `⚠️` in section header with an SVG warning triangle icon.
- **TopBar.jsx** — Replace `🔔` notification bell with an SVG bell icon.

The existing `Icon` atom component should be extended with new icon names if the SVGs are reusable, otherwise inline SVGs in the specific component are acceptable.

---

## 3. Stat Cards — Remove Placeholder Dashes

**Problem:** "Expiring Soon" and "Low Stock" cards show "—" because API calls aren't connected.

**Files affected:** `Dashboard.jsx`

**Changes:**

- **Expiring Soon:** Wire up the count from `expiringBatches` (already fetched). Filter batches expiring within 30 days and show the count. Add a small badge showing urgent count (≤ 7 days).
- **Low Stock:** Add a new query to fetch products where `quantity <= reorderQuantity`, or use the existing products endpoint with a filter parameter. Show the count with a "needs action" badge.
- **Loading state:** While data is fetching, show a subtle animated skeleton bar instead of "—".

---

## 4. Micro-interactions — Add Transitions & Hover States

**Problem:** UI feels static — no hover feedback on cards, no focus rings, no press states.

**Files affected:** `StatCard.jsx`, `DataTable.jsx`, `Button.jsx`, `Input.jsx`

**Changes:**

- **StatCard:** Add `transition-all duration-200 hover:shadow-md hover:-translate-y-0.5` for a subtle lift on hover.
- **DataTable rows:** Add `transition-colors duration-150` (already has `hover:bg-slate-50`).
- **Button:** Add `active:scale-[0.97]` for press feedback.
- **Input:** Add `focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/30` for consistent focus indication.
- **All interactive elements:** Ensure `focus-visible:ring-2 ring-[var(--color-primary)]/30 outline-none` for keyboard accessibility.

---

## 5. Sidebar — Collapsible (Icon-Only + Expanded)

**Problem:** Icon-only sidebar (56px) requires tooltips for identification.

**Files affected:** `Sidebar.jsx`, `AdminLayout.jsx`

**Changes:**

- Add `collapsed` state to `Sidebar` (default: `true` to match current behavior).
- **Collapsed mode (56px):** Current icon-only layout. Toggle button at bottom shows chevron-right icon.
- **Expanded mode (200px):** Icon + text label for each nav item. App name "PharmStock" shown next to logo. Toggle button shows chevron-left + "Collapse" text.
- **Transition:** `transition-all duration-200` on the sidebar width for smooth animation.
- **Persistence:** Store collapsed state in `localStorage` key `sidebar-collapsed`.
- **AdminLayout:** No changes needed — flexbox already handles dynamic sidebar width.

**Toggle button:**
- Position: bottom of sidebar, above the logout button
- Collapsed: single chevron-right icon (`>>`) in a bordered square
- Expanded: chevron-left icon + "Collapse" text in a bordered row

---

## 6. Table Actions — Icon Buttons

**Problem:** Plain text "Edit" / "Delete" links in table rows are visually weak.

**Files affected:** All page components that render table rows (`Products.jsx`, `Inventory.jsx`, `Clinical.jsx`, `Orders.jsx`, `Transactions.jsx`, `Locations.jsx`, `Users.jsx`)

**Changes:**

Replace text action links with icon buttons:
- **Edit:** Pencil icon in a 28×28px bordered button (teal color, `title="Edit"` for tooltip)
- **Delete:** Trash icon in a 28×28px bordered button (red color, `title="Delete"` for tooltip)
- Buttons: `inline-flex items-center justify-center w-7 h-7 rounded-[var(--radius-sm)] border border-[var(--color-border-light)] hover:border-[var(--color-primary)] transition-colors`

Create a reusable `TableRowActions` molecule component to avoid duplicating the pattern across pages.

---

## 7. Form Field Grouping in Drawers

**Problem:** Product form has 11 flat fields — dimensions feel disconnected and overwhelming.

**Files affected:** `Products.jsx` (and similar long forms in other pages)

**Changes:**

- Group related fields under collapsible `<fieldset>` sections with a clickable header:
  - **Package Dimensions** (collapsed by default): Weight, Height, Width, Depth — displayed in a 2-column grid inside the group.
- Section header: chevron icon + group title, styled with `text-xs font-semibold text-[var(--color-text-secondary)]`
- Collapsed/expanded state managed locally in the form component.
- Group container: `border border-[var(--color-border-light)] rounded-[var(--radius-md)] p-3 bg-slate-50`

This pattern applies to the Products form primarily. Other forms (Inventory, Clinical, etc.) can adopt it if they have similar groups of related fields.

---

## Files Changed (Summary)

| File | Changes |
|------|---------|
| `atoms/Button.jsx` | Add `active:scale-[0.97]` |
| `atoms/Input.jsx` | Add focus ring styles |
| `atoms/Icon.jsx` | Add new icon names (warning, bell, pencil, trash) |
| `molecules/StatCard.jsx` | Hover lift, text size fixes |
| `molecules/TableRowActions.jsx` | **New** — reusable icon button group |
| `organisms/Sidebar.jsx` | Collapsible state, expanded layout, toggle button |
| `organisms/TopBar.jsx` | SVG bell icon, remove emoji |
| `organisms/DataTable.jsx` | Header text size fix |
| `organisms/ExpiryAlerts.jsx` | SVG icons, text size fixes |
| `organisms/ActivityFeed.jsx` | SVG icons, text size fixes |
| `pages/Dashboard.jsx` | Wire up stat card counts, loading skeleton |
| `pages/Products.jsx` | Icon buttons, form field grouping |
| Other page files | Icon buttons (TableRowActions) |

## Not In Scope

- Dark mode
- Responsive/mobile layout
- Table column sorting or filtering
- New pages or features
- Chart/visualization library
- API endpoint changes

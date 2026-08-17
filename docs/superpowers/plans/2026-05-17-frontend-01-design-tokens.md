# Design Tokens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the flat token system with a full 3-layer design token architecture (primitive + semantic + dark mode) that serves as the foundation for the entire component library rebuild.

**Architecture:** A `tokens.css` file defines primitive color scales (teal, slate, emerald, red, amber, blue, purple, orange) and semantic token mappings. Dark mode overrides only the semantic layer via `[data-theme="dark"]`. A `theme.js` file mirrors every token as a JS object for programmatic use (charts, dynamic styles). A `global.css` file provides base resets and typography. Four Storybook stories validate every token visually.

**Tech Stack:** CSS custom properties, JavaScript ES modules, Storybook 8, React 19, Tailwind CSS v4

---

### Task 1: tokens.css — Complete Rewrite

**Files:**
- Modify: `demo-ui/src/styles/tokens.css`

- [ ] **Step 1: Replace the entire contents of tokens.css**

The file currently has 49 lines of flat tokens. Replace the entire file with the 3-layer system below. This includes:
- All 6 color scales (teal, slate, emerald, red, amber, blue) at 50-950 steps
- Additional scales for pharmacy domain colors (purple, orange)
- Full semantic token mappings for primary, feedback, surfaces, text, borders, sidebar
- Complete dark mode overrides via `[data-theme="dark"]`
- Typography scale (8 steps with size, weight, line-height)
- Spacing scale (13 steps)
- Shadow scale (5 steps)
- Radius scale (6 steps)

```css
/* ==========================================================================
   Design Tokens — Primitive + Semantic + Dark Mode
   ========================================================================== */

/* --------------------------------------------------------------------------
   1. Primitive Color Scales
   -------------------------------------------------------------------------- */
:root {
  /* Teal — Primary */
  --teal-50: #f0fdfa;
  --teal-100: #ccfbf1;
  --teal-200: #99f6e4;
  --teal-300: #5eead4;
  --teal-400: #2dd4bf;
  --teal-500: #14b8a6;
  --teal-600: #0d9488;
  --teal-700: #0f766e;
  --teal-800: #115e59;
  --teal-900: #134e4a;
  --teal-950: #042f2e;

  /* Slate — Neutral */
  --slate-50: #f8fafc;
  --slate-100: #f1f5f9;
  --slate-200: #e2e8f0;
  --slate-300: #cbd5e1;
  --slate-400: #94a3b8;
  --slate-500: #64748b;
  --slate-600: #475569;
  --slate-700: #334155;
  --slate-800: #1e293b;
  --slate-900: #0f172a;
  --slate-950: #020617;

  /* Emerald — Success */
  --emerald-50: #ecfdf5;
  --emerald-100: #d1fae5;
  --emerald-200: #a7f3d0;
  --emerald-300: #6ee7b7;
  --emerald-400: #34d399;
  --emerald-500: #10b981;
  --emerald-600: #059669;
  --emerald-700: #047857;
  --emerald-800: #065f46;
  --emerald-900: #064e3b;
  --emerald-950: #022c22;

  /* Red — Danger */
  --red-50: #fef2f2;
  --red-100: #fee2e2;
  --red-200: #fecaca;
  --red-300: #fca5a5;
  --red-400: #f87171;
  --red-500: #ef4444;
  --red-600: #dc2626;
  --red-700: #b91c1c;
  --red-800: #991b1b;
  --red-900: #7f1d1d;
  --red-950: #450a0a;

  /* Amber — Warning */
  --amber-50: #fffbeb;
  --amber-100: #fef3c7;
  --amber-200: #fde68a;
  --amber-300: #fcd34d;
  --amber-400: #fbbf24;
  --amber-500: #f59e0b;
  --amber-600: #d97706;
  --amber-700: #b45309;
  --amber-800: #92400e;
  --amber-900: #78350f;
  --amber-950: #451a03;

  /* Blue — Info */
  --blue-50: #eff6ff;
  --blue-100: #dbeafe;
  --blue-200: #bfdbfe;
  --blue-300: #93c5fd;
  --blue-400: #60a5fa;
  --blue-500: #3b82f6;
  --blue-600: #2563eb;
  --blue-700: #1d4ed8;
  --blue-800: #1e40af;
  --blue-900: #1e3a8a;
  --blue-950: #172554;

  /* Purple — ADMIN role badge */
  --purple-50: #faf5ff;
  --purple-100: #f3e8ff;
  --purple-200: #e9d5ff;
  --purple-300: #d8b4fe;
  --purple-400: #c084fc;
  --purple-500: #a855f7;
  --purple-600: #9333ea;
  --purple-700: #7e22ce;
  --purple-800: #6b21a8;
  --purple-900: #581c87;
  --purple-950: #3b0764;

  /* Orange — REORDER_NEEDED task type */
  --orange-50: #fff7ed;
  --orange-100: #ffedd5;
  --orange-200: #fed7aa;
  --orange-300: #fdba74;
  --orange-400: #fb923c;
  --orange-500: #f97316;
  --orange-600: #ea580c;
  --orange-700: #c2410c;
  --orange-800: #9a3412;
  --orange-900: #7c2d12;
  --orange-950: #431407;

  /* --------------------------------------------------------------------------
     2. Semantic Tokens — Light Mode (default)
     -------------------------------------------------------------------------- */

  /* Primary */
  --color-primary: var(--teal-600);
  --color-primary-hover: var(--teal-700);
  --color-primary-active: var(--teal-800);
  --color-primary-subtle: var(--teal-100);
  --color-primary-text: var(--teal-950);

  /* Feedback — Success */
  --color-success: var(--emerald-600);
  --color-success-subtle: var(--emerald-100);
  --color-success-text: var(--emerald-900);

  /* Feedback — Danger */
  --color-danger: var(--red-600);
  --color-danger-subtle: var(--red-100);
  --color-danger-text: var(--red-900);

  /* Feedback — Warning */
  --color-warning: var(--amber-500);
  --color-warning-subtle: var(--amber-100);
  --color-warning-text: var(--amber-900);

  /* Feedback — Info */
  --color-info: var(--blue-600);
  --color-info-subtle: var(--blue-100);
  --color-info-text: var(--blue-900);

  /* Pharmacy domain — Role badges */
  --color-badge-admin: var(--purple-600);
  --color-badge-admin-subtle: var(--purple-100);
  --color-badge-pharmacist: var(--teal-600);
  --color-badge-pharmacist-subtle: var(--teal-100);
  --color-badge-employee: var(--slate-600);
  --color-badge-employee-subtle: var(--slate-100);
  --color-badge-manager: var(--blue-600);
  --color-badge-manager-subtle: var(--blue-100);

  /* Pharmacy domain — Task types */
  --color-task-reorder: var(--orange-600);
  --color-task-reorder-subtle: var(--orange-100);

  /* Surfaces */
  --color-background: var(--slate-50);
  --color-surface: #ffffff;
  --color-surface-raised: #ffffff;
  --color-overlay: rgba(0, 0, 0, 0.5);

  /* Text */
  --color-text-primary: var(--slate-900);
  --color-text-secondary: var(--slate-600);
  --color-text-muted: var(--slate-400);
  --color-text-inverse: #ffffff;

  /* Borders */
  --color-border: var(--slate-200);
  --color-border-light: var(--slate-100);
  --color-border-focus: var(--teal-500);

  /* Sidebar */
  --color-sidebar-bg: var(--slate-900);
  --color-sidebar-active: rgba(13, 148, 136, 0.2);
  --color-sidebar-text: var(--slate-300);
  --color-sidebar-text-active: #ffffff;

  /* --------------------------------------------------------------------------
     3. Typography Scale
     -------------------------------------------------------------------------- */
  --font-family: Inter, system-ui, -apple-system, sans-serif;

  --text-2xs: 0.625rem;
  --text-2xs--line-height: 1rem;
  --text-2xs--weight: 400;

  --text-xs: 0.75rem;
  --text-xs--line-height: 1rem;
  --text-xs--weight: 400;

  --text-sm: 0.875rem;
  --text-sm--line-height: 1.25rem;
  --text-sm--weight: 400;

  --text-base: 1rem;
  --text-base--line-height: 1.5rem;
  --text-base--weight: 400;

  --text-lg: 1.125rem;
  --text-lg--line-height: 1.75rem;
  --text-lg--weight: 500;

  --text-xl: 1.25rem;
  --text-xl--line-height: 1.75rem;
  --text-xl--weight: 600;

  --text-2xl: 1.5rem;
  --text-2xl--line-height: 2rem;
  --text-2xl--weight: 600;

  --text-3xl: 1.875rem;
  --text-3xl--line-height: 2.25rem;
  --text-3xl--weight: 700;

  --font-normal: 400;
  --font-medium: 500;
  --font-semibold: 600;
  --font-bold: 700;

  /* --------------------------------------------------------------------------
     4. Spacing Scale
     -------------------------------------------------------------------------- */
  --space-0: 0;
  --space-0-5: 0.125rem;
  --space-1: 0.25rem;
  --space-1-5: 0.375rem;
  --space-2: 0.5rem;
  --space-3: 0.75rem;
  --space-4: 1rem;
  --space-5: 1.25rem;
  --space-6: 1.5rem;
  --space-8: 2rem;
  --space-10: 2.5rem;
  --space-12: 3rem;
  --space-16: 4rem;

  /* --------------------------------------------------------------------------
     5. Shadows
     -------------------------------------------------------------------------- */
  --shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.07), 0 2px 4px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1), 0 4px 6px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px rgba(0, 0, 0, 0.1), 0 8px 10px rgba(0, 0, 0, 0.04);

  /* --------------------------------------------------------------------------
     6. Radii
     -------------------------------------------------------------------------- */
  --radius-sm: 4px;
  --radius-md: 6px;
  --radius-lg: 8px;
  --radius-xl: 12px;
  --radius-2xl: 16px;
  --radius-full: 9999px;
}

/* --------------------------------------------------------------------------
   7. Semantic Tokens — Dark Mode Overrides
   -------------------------------------------------------------------------- */
[data-theme="dark"] {
  /* Primary */
  --color-primary: var(--teal-500);
  --color-primary-hover: var(--teal-400);
  --color-primary-active: var(--teal-300);
  --color-primary-subtle: var(--teal-900);
  --color-primary-text: var(--teal-100);

  /* Feedback — Success */
  --color-success: var(--emerald-500);
  --color-success-subtle: var(--emerald-900);
  --color-success-text: var(--emerald-100);

  /* Feedback — Danger */
  --color-danger: var(--red-500);
  --color-danger-subtle: var(--red-900);
  --color-danger-text: var(--red-100);

  /* Feedback — Warning */
  --color-warning: var(--amber-400);
  --color-warning-subtle: var(--amber-900);
  --color-warning-text: var(--amber-100);

  /* Feedback — Info */
  --color-info: var(--blue-500);
  --color-info-subtle: var(--blue-900);
  --color-info-text: var(--blue-100);

  /* Pharmacy domain — Role badges */
  --color-badge-admin: var(--purple-400);
  --color-badge-admin-subtle: var(--purple-900);
  --color-badge-pharmacist: var(--teal-400);
  --color-badge-pharmacist-subtle: var(--teal-900);
  --color-badge-employee: var(--slate-400);
  --color-badge-employee-subtle: var(--slate-800);
  --color-badge-manager: var(--blue-400);
  --color-badge-manager-subtle: var(--blue-900);

  /* Pharmacy domain — Task types */
  --color-task-reorder: var(--orange-400);
  --color-task-reorder-subtle: var(--orange-900);

  /* Surfaces */
  --color-background: var(--slate-950);
  --color-surface: var(--slate-900);
  --color-surface-raised: var(--slate-800);
  --color-overlay: rgba(0, 0, 0, 0.7);

  /* Text */
  --color-text-primary: var(--slate-50);
  --color-text-secondary: var(--slate-400);
  --color-text-muted: var(--slate-500);
  --color-text-inverse: var(--slate-900);

  /* Borders */
  --color-border: var(--slate-700);
  --color-border-light: var(--slate-800);
  --color-border-focus: var(--teal-400);

  /* Sidebar */
  --color-sidebar-bg: var(--slate-950);
  --color-sidebar-active: rgba(20, 184, 166, 0.15);
  --color-sidebar-text: var(--slate-400);
  --color-sidebar-text-active: #ffffff;
}
```

- [ ] **Step 2: Verify the CSS parses correctly**

Run:
```bash
cd demo-ui && npm run build
```

Expected: Build completes with no CSS-related errors. The Vite build should succeed since `tokens.css` is imported via `index.css`.

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/styles/tokens.css
git commit -m "feat(demo-ui): replace flat tokens with 3-layer token system

Add primitive color scales (teal, slate, emerald, red, amber, blue,
purple, orange), full semantic token mappings for primary, feedback,
surfaces, text, borders, sidebar, pharmacy domain badges and task
types. Add dark mode overrides via [data-theme=dark]. Add typography,
spacing, shadow, and radius scales."
```

---

### Task 2: theme.js — Full JS Mirror of All Tokens

**Files:**
- Modify: `demo-ui/src/foundations/theme.js`

- [ ] **Step 1: Replace the entire contents of theme.js**

The current file is 22 lines with hardcoded hex values. Replace with a complete mirror of all tokens from `tokens.css`:

```js
// JS token reference — mirrors tokens.css for programmatic use (charts, canvas, dynamic styles)

// Primitive color scales
const primitives = {
  teal: {
    50: '#f0fdfa',
    100: '#ccfbf1',
    200: '#99f6e4',
    300: '#5eead4',
    400: '#2dd4bf',
    500: '#14b8a6',
    600: '#0d9488',
    700: '#0f766e',
    800: '#115e59',
    900: '#134e4a',
    950: '#042f2e',
  },
  slate: {
    50: '#f8fafc',
    100: '#f1f5f9',
    200: '#e2e8f0',
    300: '#cbd5e1',
    400: '#94a3b8',
    500: '#64748b',
    600: '#475569',
    700: '#334155',
    800: '#1e293b',
    900: '#0f172a',
    950: '#020617',
  },
  emerald: {
    50: '#ecfdf5',
    100: '#d1fae5',
    200: '#a7f3d0',
    300: '#6ee7b7',
    400: '#34d399',
    500: '#10b981',
    600: '#059669',
    700: '#047857',
    800: '#065f46',
    900: '#064e3b',
    950: '#022c22',
  },
  red: {
    50: '#fef2f2',
    100: '#fee2e2',
    200: '#fecaca',
    300: '#fca5a5',
    400: '#f87171',
    500: '#ef4444',
    600: '#dc2626',
    700: '#b91c1c',
    800: '#991b1b',
    900: '#7f1d1d',
    950: '#450a0a',
  },
  amber: {
    50: '#fffbeb',
    100: '#fef3c7',
    200: '#fde68a',
    300: '#fcd34d',
    400: '#fbbf24',
    500: '#f59e0b',
    600: '#d97706',
    700: '#b45309',
    800: '#92400e',
    900: '#78350f',
    950: '#451a03',
  },
  blue: {
    50: '#eff6ff',
    100: '#dbeafe',
    200: '#bfdbfe',
    300: '#93c5fd',
    400: '#60a5fa',
    500: '#3b82f6',
    600: '#2563eb',
    700: '#1d4ed8',
    800: '#1e40af',
    900: '#1e3a8a',
    950: '#172554',
  },
  purple: {
    50: '#faf5ff',
    100: '#f3e8ff',
    200: '#e9d5ff',
    300: '#d8b4fe',
    400: '#c084fc',
    500: '#a855f7',
    600: '#9333ea',
    700: '#7e22ce',
    800: '#6b21a8',
    900: '#581c87',
    950: '#3b0764',
  },
  orange: {
    50: '#fff7ed',
    100: '#ffedd5',
    200: '#fed7aa',
    300: '#fdba74',
    400: '#fb923c',
    500: '#f97316',
    600: '#ea580c',
    700: '#c2410c',
    800: '#9a3412',
    900: '#7c2d12',
    950: '#431407',
  },
}

// Semantic colors — light mode (default)
const colorsLight = {
  primary: primitives.teal[600],
  primaryHover: primitives.teal[700],
  primaryActive: primitives.teal[800],
  primarySubtle: primitives.teal[100],
  primaryText: primitives.teal[950],

  success: primitives.emerald[600],
  successSubtle: primitives.emerald[100],
  successText: primitives.emerald[900],

  danger: primitives.red[600],
  dangerSubtle: primitives.red[100],
  dangerText: primitives.red[900],

  warning: primitives.amber[500],
  warningSubtle: primitives.amber[100],
  warningText: primitives.amber[900],

  info: primitives.blue[600],
  infoSubtle: primitives.blue[100],
  infoText: primitives.blue[900],

  badgeAdmin: primitives.purple[600],
  badgeAdminSubtle: primitives.purple[100],
  badgePharmacist: primitives.teal[600],
  badgePharmacistSubtle: primitives.teal[100],
  badgeEmployee: primitives.slate[600],
  badgeEmployeeSubtle: primitives.slate[100],
  badgeManager: primitives.blue[600],
  badgeManagerSubtle: primitives.blue[100],

  taskReorder: primitives.orange[600],
  taskReorderSubtle: primitives.orange[100],

  background: primitives.slate[50],
  surface: '#ffffff',
  surfaceRaised: '#ffffff',
  overlay: 'rgba(0, 0, 0, 0.5)',

  textPrimary: primitives.slate[900],
  textSecondary: primitives.slate[600],
  textMuted: primitives.slate[400],
  textInverse: '#ffffff',

  border: primitives.slate[200],
  borderLight: primitives.slate[100],
  borderFocus: primitives.teal[500],

  sidebarBg: primitives.slate[900],
  sidebarActive: 'rgba(13, 148, 136, 0.2)',
  sidebarText: primitives.slate[300],
  sidebarTextActive: '#ffffff',
}

// Semantic colors — dark mode
const colorsDark = {
  primary: primitives.teal[500],
  primaryHover: primitives.teal[400],
  primaryActive: primitives.teal[300],
  primarySubtle: primitives.teal[900],
  primaryText: primitives.teal[100],

  success: primitives.emerald[500],
  successSubtle: primitives.emerald[900],
  successText: primitives.emerald[100],

  danger: primitives.red[500],
  dangerSubtle: primitives.red[900],
  dangerText: primitives.red[100],

  warning: primitives.amber[400],
  warningSubtle: primitives.amber[900],
  warningText: primitives.amber[100],

  info: primitives.blue[500],
  infoSubtle: primitives.blue[900],
  infoText: primitives.blue[100],

  badgeAdmin: primitives.purple[400],
  badgeAdminSubtle: primitives.purple[900],
  badgePharmacist: primitives.teal[400],
  badgePharmacistSubtle: primitives.teal[900],
  badgeEmployee: primitives.slate[400],
  badgeEmployeeSubtle: primitives.slate[800],
  badgeManager: primitives.blue[400],
  badgeManagerSubtle: primitives.blue[900],

  taskReorder: primitives.orange[400],
  taskReorderSubtle: primitives.orange[900],

  background: primitives.slate[950],
  surface: primitives.slate[900],
  surfaceRaised: primitives.slate[800],
  overlay: 'rgba(0, 0, 0, 0.7)',

  textPrimary: primitives.slate[50],
  textSecondary: primitives.slate[400],
  textMuted: primitives.slate[500],
  textInverse: primitives.slate[900],

  border: primitives.slate[700],
  borderLight: primitives.slate[800],
  borderFocus: primitives.teal[400],

  sidebarBg: primitives.slate[950],
  sidebarActive: 'rgba(20, 184, 166, 0.15)',
  sidebarText: primitives.slate[400],
  sidebarTextActive: '#ffffff',
}

const typography = {
  '2xs': { size: '0.625rem', lineHeight: '1rem', weight: 400 },
  xs: { size: '0.75rem', lineHeight: '1rem', weight: 400 },
  sm: { size: '0.875rem', lineHeight: '1.25rem', weight: 400 },
  base: { size: '1rem', lineHeight: '1.5rem', weight: 400 },
  lg: { size: '1.125rem', lineHeight: '1.75rem', weight: 500 },
  xl: { size: '1.25rem', lineHeight: '1.75rem', weight: 600 },
  '2xl': { size: '1.5rem', lineHeight: '2rem', weight: 600 },
  '3xl': { size: '1.875rem', lineHeight: '2.25rem', weight: 700 },
  fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
  fontWeight: {
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
  },
}

const spacing = {
  0: '0',
  0.5: '0.125rem',
  1: '0.25rem',
  1.5: '0.375rem',
  2: '0.5rem',
  3: '0.75rem',
  4: '1rem',
  5: '1.25rem',
  6: '1.5rem',
  8: '2rem',
  10: '2.5rem',
  12: '3rem',
  16: '4rem',
}

const radius = {
  sm: '4px',
  md: '6px',
  lg: '8px',
  xl: '12px',
  '2xl': '16px',
  full: '9999px',
}

const shadow = {
  xs: '0 1px 2px rgba(0, 0, 0, 0.05)',
  sm: '0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06)',
  md: '0 4px 6px rgba(0, 0, 0, 0.07), 0 2px 4px rgba(0, 0, 0, 0.06)',
  lg: '0 10px 15px rgba(0, 0, 0, 0.1), 0 4px 6px rgba(0, 0, 0, 0.05)',
  xl: '0 20px 25px rgba(0, 0, 0, 0.1), 0 8px 10px rgba(0, 0, 0, 0.04)',
}

const breakpoints = {
  sm: '640px',
  md: '768px',
  lg: '1024px',
  xl: '1280px',
}

export const theme = {
  primitives,
  colors: colorsLight,
  colorsDark,
  typography,
  spacing,
  radius,
  shadow,
  breakpoints,
}
```

- [ ] **Step 2: Verify the JS exports correctly**

Run:
```bash
cd demo-ui && node -e "import('./src/foundations/theme.js').then(m => { console.log('tokens:', Object.keys(m.theme)); console.log('colors:', Object.keys(m.theme.colors).length, 'semantic tokens'); console.log('primitives:', Object.keys(m.theme.primitives).length, 'scales'); console.log('spacing keys:', Object.keys(m.theme.spacing).length); })"
```

Expected: Prints `tokens: [ 'primitives', 'colors', 'colorsDark', 'typography', 'spacing', 'radius', 'shadow', 'breakpoints' ]`, followed by `24 semantic tokens`, `8 scales`, `13 spacing keys`.

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/foundations/theme.js
git commit -m "feat(demo-ui): expand theme.js to mirror full 3-layer token system

Export primitives (8 color scales), semantic colors (light + dark),
typography scale (8 steps), spacing (13 values), radii (6), shadows (5),
and breakpoints. Used by charts, canvas, and dynamic styles."
```

---

### Task 3: global.css — Base Styles and Resets

**Files:**
- Create: `demo-ui/src/styles/global.css`

- [ ] **Step 1: Create global.css**

This file provides base resets and global styles. It is separate from `tokens.css` (which only defines variables) and from `index.css` (which imports Tailwind and tokens).

```css
/* ==========================================================================
   Global Styles — Base resets and defaults
   Tokens are defined in tokens.css, imported via index.css.
   ========================================================================== */

/* --------------------------------------------------------------------------
   Box sizing
   -------------------------------------------------------------------------- */
*,
*::before,
*::after {
  box-sizing: border-box;
}

/* --------------------------------------------------------------------------
   Document
   -------------------------------------------------------------------------- */
html {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
}

body {
  font-family: var(--font-family);
  font-size: var(--text-base);
  line-height: var(--text-base--line-height);
  color: var(--color-text-primary);
  background-color: var(--color-background);
  margin: 0;
  min-height: 100vh;
  transition: background-color 0.2s ease, color 0.2s ease;
}

/* --------------------------------------------------------------------------
   Typography defaults
   -------------------------------------------------------------------------- */
h1, h2, h3, h4, h5, h6 {
  margin: 0;
  font-weight: var(--font-semibold);
  color: var(--color-text-primary);
}

p {
  margin: 0;
}

/* --------------------------------------------------------------------------
   Links
   -------------------------------------------------------------------------- */
a {
  color: var(--color-primary);
  text-decoration: none;
}

a:hover {
  color: var(--color-primary-hover);
}

/* --------------------------------------------------------------------------
   Focus ring
   -------------------------------------------------------------------------- */
:focus-visible {
  outline: 2px solid var(--color-border-focus);
  outline-offset: 2px;
}

/* --------------------------------------------------------------------------
   Scrollbar (thin, token-aware)
   -------------------------------------------------------------------------- */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background-color: var(--color-border);
  border-radius: var(--radius-full);
}

::-webkit-scrollbar-thumb:hover {
  background-color: var(--color-text-muted);
}

/* --------------------------------------------------------------------------
   Selection
   -------------------------------------------------------------------------- */
::selection {
  background-color: var(--color-primary-subtle);
  color: var(--color-primary-text);
}
```

- [ ] **Step 2: Import global.css in index.css**

Add the global.css import to `demo-ui/src/index.css`. The file currently reads:

```css
@import "tailwindcss";
@import "./styles/tokens.css";

body {
  font-family: var(--font-family);
  background: var(--color-background);
  color: var(--color-text-primary);
  line-height: 1.5;
}
```

Replace it with:

```css
@import "tailwindcss";
@import "./styles/tokens.css";
@import "./styles/global.css";
```

The `body` styles are now in `global.css` (which uses the token system), so the inline `body` block in `index.css` is removed.

- [ ] **Step 3: Verify build**

Run:
```bash
cd demo-ui && npm run build
```

Expected: Build succeeds. The app still works because `global.css` applies the same body styles via tokens (now with smooth transitions for dark mode).

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/styles/global.css demo-ui/src/index.css
git commit -m "feat(demo-ui): add global.css with base resets and styles

Box-sizing reset, font smoothing, typography defaults, focus ring,
thin scrollbar, and text selection. Import in index.css replacing
inline body block. Adds dark mode transition on background/color."
```

---

### Task 4: Storybook Configuration Update

**Files:**
- Modify: `demo-ui/.storybook/preview.js`

- [ ] **Step 1: Update preview.js for new token-aware backgrounds and dark mode**

The current `preview.js` imports `../src/index.css` and defines 3 hardcoded background colors. Update it to import `global.css` as well and use semantic token references for backgrounds.

```js
import '../src/index.css'

/** @type { import('@storybook/react').Preview } */
const preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
    backgrounds: {
      default: 'light',
      values: [
        { name: 'light', value: '#f8fafc' },
        { name: 'white', value: '#ffffff' },
        { name: 'dark', value: '#020617' },
      ],
    },
    darkMode: {
      current: 'light',
      darkClass: 'data-theme="dark"',
      stylePreview: true,
    },
  },
}

export default preview
```

Note: The `backgrounds` values use the actual hex values from the slate scale (`slate-50` = `#f8fafc`, `slate-950` = `#020617`) rather than CSS variables because Storybook's backgrounds addon requires static hex values.

- [ ] **Step 2: Verify Storybook starts**

Run:
```bash
cd demo-ui && npm run storybook -- --no-open &
sleep 8 && curl -s http://localhost:6006 | head -5
```

Expected: Storybook starts without errors. Any existing stories still render (they use `var(--color-*)` tokens which are now defined in the new `tokens.css`).

- [ ] **Step 3: Commit**

```bash
git add demo-ui/.storybook/preview.js
git commit -m "chore(storybook): update preview with token-aware backgrounds

Switch default background to slate-50 for light mode, add slate-950
dark background. Add darkMode parameter for future dark mode toggle."
```

---

### Task 5: Foundations/Colors.stories.jsx

**Files:**
- Create: `demo-ui/src/foundations/Colors.stories.jsx`

- [ ] **Step 1: Create the Colors story**

This story displays all primitive color scales and all semantic token mappings. It supports a dark mode toggle by applying `[data-theme="dark"]` to a wrapper div.

```jsx
import { useState } from 'react'
import { theme } from './theme'

const SCALE_NAMES = ['teal', 'slate', 'emerald', 'red', 'amber', 'blue', 'purple', 'orange']
const SCALE_STEPS = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950]

const SEMANTIC_GROUPS = [
  {
    title: 'Primary',
    tokens: [
      { name: '--color-primary', cssVar: 'color-primary' },
      { name: '--color-primary-hover', cssVar: 'color-primary-hover' },
      { name: '--color-primary-active', cssVar: 'color-primary-active' },
      { name: '--color-primary-subtle', cssVar: 'color-primary-subtle' },
      { name: '--color-primary-text', cssVar: 'color-primary-text' },
    ],
  },
  {
    title: 'Success',
    tokens: [
      { name: '--color-success', cssVar: 'color-success' },
      { name: '--color-success-subtle', cssVar: 'color-success-subtle' },
      { name: '--color-success-text', cssVar: 'color-success-text' },
    ],
  },
  {
    title: 'Danger',
    tokens: [
      { name: '--color-danger', cssVar: 'color-danger' },
      { name: '--color-danger-subtle', cssVar: 'color-danger-subtle' },
      { name: '--color-danger-text', cssVar: 'color-danger-text' },
    ],
  },
  {
    title: 'Warning',
    tokens: [
      { name: '--color-warning', cssVar: 'color-warning' },
      { name: '--color-warning-subtle', cssVar: 'color-warning-subtle' },
      { name: '--color-warning-text', cssVar: 'color-warning-text' },
    ],
  },
  {
    title: 'Info',
    tokens: [
      { name: '--color-info', cssVar: 'color-info' },
      { name: '--color-info-subtle', cssVar: 'color-info-subtle' },
      { name: '--color-info-text', cssVar: 'color-info-text' },
    ],
  },
  {
    title: 'Surfaces',
    tokens: [
      { name: '--color-background', cssVar: 'color-background' },
      { name: '--color-surface', cssVar: 'color-surface' },
      { name: '--color-surface-raised', cssVar: 'color-surface-raised' },
      { name: '--color-overlay', cssVar: 'color-overlay' },
    ],
  },
  {
    title: 'Text',
    tokens: [
      { name: '--color-text-primary', cssVar: 'color-text-primary' },
      { name: '--color-text-secondary', cssVar: 'color-text-secondary' },
      { name: '--color-text-muted', cssVar: 'color-text-muted' },
      { name: '--color-text-inverse', cssVar: 'color-text-inverse' },
    ],
  },
  {
    title: 'Borders',
    tokens: [
      { name: '--color-border', cssVar: 'color-border' },
      { name: '--color-border-light', cssVar: 'color-border-light' },
      { name: '--color-border-focus', cssVar: 'color-border-focus' },
    ],
  },
  {
    title: 'Sidebar',
    tokens: [
      { name: '--color-sidebar-bg', cssVar: 'color-sidebar-bg' },
      { name: '--color-sidebar-active', cssVar: 'color-sidebar-active' },
      { name: '--color-sidebar-text', cssVar: 'color-sidebar-text' },
      { name: '--color-sidebar-text-active', cssVar: 'color-sidebar-text-active' },
    ],
  },
  {
    title: 'Role Badges',
    tokens: [
      { name: '--color-badge-admin', cssVar: 'color-badge-admin' },
      { name: '--color-badge-admin-subtle', cssVar: 'color-badge-admin-subtle' },
      { name: '--color-badge-pharmacist', cssVar: 'color-badge-pharmacist' },
      { name: '--color-badge-pharmacist-subtle', cssVar: 'color-badge-pharmacist-subtle' },
      { name: '--color-badge-manager', cssVar: 'color-badge-manager' },
      { name: '--color-badge-manager-subtle', cssVar: 'color-badge-manager-subtle' },
      { name: '--color-badge-employee', cssVar: 'color-badge-employee' },
      { name: '--color-badge-employee-subtle', cssVar: 'color-badge-employee-subtle' },
    ],
  },
  {
    title: 'Task Types',
    tokens: [
      { name: '--color-task-reorder', cssVar: 'color-task-reorder' },
      { name: '--color-task-reorder-subtle', cssVar: 'color-task-reorder-subtle' },
    ],
  },
]

function ColorSwatch({ color, label, hex }) {
  return (
    <div className="flex flex-col items-center gap-1">
      <div
        className="w-14 h-14 rounded-[var(--radius-md)] border border-[var(--color-border)]"
        style={{ backgroundColor: color }}
      />
      <span className="text-[var(--text-2xs)] font-medium text-[var(--color-text-secondary)]">{label}</span>
      {hex && (
        <span className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{hex}</span>
      )}
    </div>
  )
}

function SemanticSwatch({ cssVar, name }) {
  return (
    <div className="flex items-center gap-3 p-2 rounded-[var(--radius-md)] bg-[var(--color-surface)]">
      <div
        className="w-10 h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] shrink-0"
        style={{ backgroundColor: `var(${cssVar})` }}
      />
      <div className="min-w-0">
        <div className="text-[var(--text-xs)] font-medium text-[var(--color-text-primary)] truncate">{name}</div>
        <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{cssVar}</div>
      </div>
    </div>
  )
}

export default {
  title: 'Foundations/Colors',
  parameters: {
    layout: 'padded',
  },
}

export function PrimitiveScales() {
  return (
    <div className="space-y-8">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Primitive Color Scales</h2>
      {SCALE_NAMES.map((scaleName) => (
        <div key={scaleName}>
          <h3 className="text-[var(--text-sm)] font-semibold text-[var(--color-text-secondary)] mb-3 capitalize">{scaleName}</h3>
          <div className="flex flex-wrap gap-2">
            {SCALE_STEPS.map((step) => {
              const hex = theme.primitives[scaleName][step]
              return (
                <ColorSwatch
                  key={step}
                  color={hex}
                  label={String(step)}
                  hex={hex}
                />
              )
            })}
          </div>
        </div>
      ))}
    </div>
  )
}

export function SemanticTokens() {
  const [isDark, setIsDark] = useState(false)

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Semantic Token Mappings</h2>
        <button
          onClick={() => setIsDark(!isDark)}
          className="px-3 py-1.5 text-[var(--text-xs)] font-medium rounded-[var(--radius-md)] border border-[var(--color-border)] text-[var(--color-text-secondary)] hover:bg-[var(--color-surface-raised)] cursor-pointer transition-colors"
        >
          {isDark ? 'Light Mode' : 'Dark Mode'}
        </button>
      </div>
      <div data-theme={isDark ? 'dark' : undefined} className="space-y-6 rounded-[var(--radius-lg)] p-4" style={{ backgroundColor: isDark ? '#020617' : '#f8fafc' }}>
        {SEMANTIC_GROUPS.map((group) => (
          <div key={group.title}>
            <h3 className="text-[var(--text-sm)] font-semibold text-[var(--color-text-secondary)] mb-2">{group.title}</h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
              {group.tokens.map((token) => (
                <SemanticSwatch key={token.cssVar} cssVar={token.cssVar} name={token.name} />
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify the story renders in Storybook**

Run:
```bash
cd demo-ui && npm run storybook
```

Expected: Navigate to `Foundations/Colors` in Storybook sidebar. Two stories appear: "Primitive Scales" (grid of all 8 color scales with swatches) and "Semantic Tokens" (grouped grid with a Light/Dark mode toggle button).

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/foundations/Colors.stories.jsx
git commit -m "feat(storybook): add Colors foundation story with primitive and semantic tokens

Show all 8 color scales at every step. Show all semantic tokens grouped
by category (primary, feedback, surfaces, text, borders, sidebar, role
badges, task types) with interactive light/dark mode toggle."
```

---

### Task 6: Foundations/Typography.stories.jsx

**Files:**
- Create: `demo-ui/src/foundations/Typography.stories.jsx`

- [ ] **Step 1: Create the Typography story**

```jsx
import { theme } from './theme'

const SCALE = [
  { key: '2xs', token: '--text-2xs', use: 'Tiny labels' },
  { key: 'xs', token: '--text-xs', use: 'Badges, helper text' },
  { key: 'sm', token: '--text-sm', use: 'Table cells, form inputs' },
  { key: 'base', token: '--text-base', use: 'Body text' },
  { key: 'lg', token: '--text-lg', use: 'Section titles' },
  { key: 'xl', token: '--text-xl', use: 'Page subtitles' },
  { key: '2xl', token: '--text-2xl', use: 'Page titles' },
  { key: '3xl', token: '--text-3xl', use: 'Dashboard stats' },
]

const WEIGHTS = [
  { key: 'normal', token: '--font-normal', value: 400 },
  { key: 'medium', token: '--font-medium', value: 500 },
  { key: 'semibold', token: '--font-semibold', value: 600 },
  { key: 'bold', token: '--font-bold', value: 700 },
]

export default {
  title: 'Foundations/Typography',
  parameters: {
    layout: 'padded',
  },
}

export function SizeScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Typography Size Scale</h2>
      <p className="text-[var(--text-sm)] text-[var(--color-text-secondary)]">
        Font family: <code className="px-1.5 py-0.5 bg-[var(--color-surface)] rounded text-[var(--text-xs)] font-mono">{theme.typography.fontFamily}</code>
      </p>
      <div className="space-y-4">
        {SCALE.map(({ key, token, use }) => {
          const step = theme.typography[key]
          return (
            <div key={key} className="flex items-baseline gap-4 p-3 rounded-[var(--radius-md)] border border-[var(--color-border)]">
              <div className="w-12 text-right shrink-0">
                <span className="text-[var(--text-2xs)] font-mono text-[var(--color-text-muted)]">{key}</span>
              </div>
              <div className="flex-1 min-w-0" style={{ fontSize: `var(${token})`, lineHeight: step.lineHeight, fontWeight: step.weight }}>
                The quick brown fox jumps over the lazy dog
              </div>
              <div className="text-right shrink-0">
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{step.size}</div>
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">lh {step.lineHeight}</div>
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">w {step.weight}</div>
              </div>
              <div className="w-28 text-right shrink-0">
                <span className="text-[var(--text-2xs)] text-[var(--color-text-secondary)]">{use}</span>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export function FontWeightScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Font Weights</h2>
      <div className="space-y-3">
        {WEIGHTS.map(({ key, token, value }) => (
          <div key={key} className="flex items-baseline gap-4 p-3 rounded-[var(--radius-md)] border border-[var(--color-border)]">
            <div className="w-24 text-right shrink-0">
              <span className="text-[var(--text-2xs)] font-mono text-[var(--color-text-muted)]">{token}</span>
            </div>
            <div className="flex-1 text-[var(--text-2xl)]" style={{ fontWeight: `var(${token})` }}>
              Pharmacy Stock Management
            </div>
            <div className="w-12 text-right shrink-0">
              <span className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{value}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify the story renders**

Run:
```bash
cd demo-ui && npm run storybook
```

Expected: Navigate to `Foundations/Typography`. Two stories: "Size Scale" (8 rows showing sample text at each size with metadata) and "Font Weight Scale" (4 rows showing sample text at each weight).

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/foundations/Typography.stories.jsx
git commit -m "feat(storybook): add Typography foundation story with size and weight scales

Display all 8 typography sizes with sample text, line-height, and
metadata. Display all 4 font weights with visual comparison."
```

---

### Task 7: Foundations/Spacing.stories.jsx

**Files:**
- Create: `demo-ui/src/foundations/Spacing.stories.jsx`

- [ ] **Step 1: Create the Spacing story**

```jsx
import { theme } from './theme'

const SPACING_ENTRIES = [
  { key: '0', use: 'Reset' },
  { key: '0.5', use: 'Tight gaps' },
  { key: '1', use: 'Icon gaps' },
  { key: '1.5', use: 'Badge padding' },
  { key: '2', use: 'Inline spacing' },
  { key: '3', use: 'Compact padding' },
  { key: '4', use: 'Standard padding' },
  { key: '5', use: 'Card padding' },
  { key: '6', use: 'Section gaps' },
  { key: '8', use: 'Page margins' },
  { key: '10', use: 'Large gaps' },
  { key: '12', use: 'Page sections' },
  { key: '16', use: 'Major sections' },
]

export default {
  title: 'Foundations/Spacing',
  parameters: {
    layout: 'padded',
  },
}

export function SpacingScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Spacing Scale</h2>
      <div className="space-y-2">
        {SPACING_ENTRIES.map(({ key, use }) => {
          const value = theme.spacing[key]
          // Parse rem to px for visualization (1rem = 16px)
          const pxValue = parseFloat(value) * 16
          return (
            <div key={key} className="flex items-center gap-4">
              <div className="w-8 text-right shrink-0">
                <span className="text-[var(--text-2xs)] font-mono text-[var(--color-text-muted)]">{key}</span>
              </div>
              <div className="w-20 text-right shrink-0">
                <span className="text-[var(--text-xs)] font-mono text-[var(--color-text-secondary)]">{value}</span>
              </div>
              <div className="flex-1">
                <div
                  className="h-4 rounded-[var(--radius-sm)]"
                  style={{
                    width: `${pxValue}px`,
                    backgroundColor: 'var(--color-primary)',
                    opacity: 0.6,
                  }}
                />
              </div>
              <div className="w-12 text-right shrink-0">
                <span className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{pxValue}px</span>
              </div>
              <div className="w-28 text-right shrink-0">
                <span className="text-[var(--text-2xs)] text-[var(--color-text-secondary)]">{use}</span>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify the story renders**

Run:
```bash
cd demo-ui && npm run storybook
```

Expected: Navigate to `Foundations/Spacing`. One story showing a horizontal bar chart of all 13 spacing values with rem/px labels and usage descriptions.

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/foundations/Spacing.stories.jsx
git commit -m "feat(storybook): add Spacing foundation story with visual bar chart

Display all 13 spacing tokens as proportional teal bars with rem, px,
and usage annotation for each step."
```

---

### Task 8: Foundations/Shadows.stories.jsx

**Files:**
- Create: `demo-ui/src/foundations/Shadows.stories.jsx`

- [ ] **Step 1: Create the Shadows story**

```jsx
import { theme } from './theme'

const SHADOW_ENTRIES = [
  { key: 'xs', use: 'Subtle elevation' },
  { key: 'sm', use: 'Cards, dropdowns' },
  { key: 'md', use: 'Popovers, modals' },
  { key: 'lg', use: 'Drawers, dialogs' },
  { key: 'xl', use: 'Full-screen overlays' },
]

const RADIUS_ENTRIES = [
  { key: 'sm', value: '4px', use: 'Badges, small elements' },
  { key: 'md', value: '6px', use: 'Buttons, inputs' },
  { key: 'lg', value: '8px', use: 'Cards, panels' },
  { key: 'xl', value: '12px', use: 'Modals, drawers' },
  { key: '2xl', value: '16px', use: 'Large containers' },
  { key: 'full', value: '9999px', use: 'Avatars, pills' },
]

export default {
  title: 'Foundations/Shadows',
  parameters: {
    layout: 'padded',
  },
}

export function ShadowScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Shadow Scale</h2>
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
        {SHADOW_ENTRIES.map(({ key, use }) => (
          <div key={key} className="flex flex-col items-center gap-3">
            <div
              className="w-28 h-28 bg-[var(--color-surface)] rounded-[var(--radius-lg)] flex items-center justify-center"
              style={{ boxShadow: `var(--shadow-${key})` }}
            >
              <div className="text-center">
                <div className="text-[var(--text-sm)] font-semibold text-[var(--color-text-primary)]">{key}</div>
                <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{use}</div>
              </div>
            </div>
            <code className="text-[var(--text-2xs)] text-[var(--color-text-muted)] font-mono max-w-28 break-all text-center">
              {theme.shadow[key]}
            </code>
          </div>
        ))}
      </div>
    </div>
  )
}

export function RadiusScale() {
  return (
    <div className="space-y-6">
      <h2 className="text-[var(--text-xl)] font-semibold text-[var(--color-text-primary)]">Border Radius Scale</h2>
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
        {RADIUS_ENTRIES.map(({ key, value, use }) => (
          <div key={key} className="flex flex-col items-center gap-3">
            <div
              className="w-20 h-20 bg-[var(--color-primary-subtle)] border-2 border-[var(--color-primary)] flex items-center justify-center"
              style={{ borderRadius: `var(--radius-${key})` }}
            >
              <div className="text-[var(--text-xs)] font-semibold text-[var(--color-primary-text)]">{value}</div>
            </div>
            <div className="text-center">
              <div className="text-[var(--text-xs)] font-medium text-[var(--color-text-primary)]">--radius-{key}</div>
              <div className="text-[var(--text-2xs)] text-[var(--color-text-muted)]">{use}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify the story renders**

Run:
```bash
cd demo-ui && npm run storybook
```

Expected: Navigate to `Foundations/Shadows`. Two stories: "Shadow Scale" (5 cards with increasing shadow depth) and "Radius Scale" (6 shapes with increasing border radius from sm to full circle).

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/foundations/Shadows.stories.jsx
git commit -m "feat(storybook): add Shadows foundation story with shadow and radius scales

Display all 5 shadow levels on cards with CSS value code. Display all
6 border radii on colored shapes with usage annotations."
```

---

### Task 9: Final Verification

**Files:**
- No new files

- [ ] **Step 1: Run production build**

```bash
cd demo-ui && npm run build
```

Expected: Build completes with zero errors. No warnings about missing CSS variables.

- [ ] **Step 2: Run Storybook and verify all 4 foundations stories**

```bash
cd demo-ui && npm run storybook
```

Expected: Storybook starts. The sidebar shows a `Foundations` section with 4 stories:
- `Foundations/Colors` with 2 child stories (Primitive Scales, Semantic Tokens)
- `Foundations/Typography` with 2 child stories (Size Scale, Font Weight Scale)
- `Foundations/Spacing` with 1 child story (Spacing Scale)
- `Foundations/Shadows` with 2 child stories (Shadow Scale, Radius Scale)

Each story renders without errors. The Semantic Tokens story has a working Light/Dark Mode toggle.

- [ ] **Step 3: Verify dark mode tokens work**

In the Semantic Tokens story, click "Dark Mode" button. The wrapper should switch to a dark background and all semantic swatches should update their colors (e.g., `--color-primary` switches from teal-600 to teal-500, surfaces become dark slate).

- [ ] **Step 4: Commit (no-op if all previous commits succeeded)**

No additional commit needed. All changes committed in Tasks 1-8.

---

## Self-Review Checklist

- [x] **Spec coverage:** All items from design spec sections 1.1-1.7 are implemented. Primitive scales (teal, slate, emerald, red, amber, blue, purple, orange), semantic tokens (primary, feedback, surfaces, text, borders, sidebar, role badges, task types), dark mode overrides, typography scale, spacing scale, shadows, radii, JS theme object.
- [x] **Placeholder scan:** No TBDs, TODOs, or "similar to" references. Every file has complete code.
- [x] **Token consistency:** Every semantic token in `tokens.css` has a corresponding entry in `theme.js` `colors` and `colorsDark` objects. Every typography/spacing/radius/shadow token in CSS has a matching JS value.
- [x] **Dark mode completeness:** Every semantic token that exists in `:root` has a corresponding override in `[data-theme="dark"]`. Pharmacy domain tokens (role badges, task types) included in dark mode.
- [x] **Backward compatibility:** Old token names (`--color-primary`, `--color-danger`, `--color-background`, `--color-text-primary`, etc.) are preserved with the same semantic meaning. Existing components using these names continue to work.
- [x] **Storybook stories:** All 4 foundations stories (Colors, Typography, Spacing, Shadows) are complete with working code. Each references `theme.js` for programmatic data.
- [x] **File paths:** All paths are under `demo-ui/src/` and match the spec's file structure.

# UI Token System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish a centralized, maintainable token system for frontend UI sizing, spacing, typography, and visual properties, eliminating hardcoded values and creating a single source of truth for design decisions.

**Architecture:** Three-layer approach:
1. **Token Registry** (`foundations/theme.js`): Programmatic JavaScript exports for component consumption
2. **CSS Variables** (`foundations/tokens.css`): Parallel CSS custom properties for stylesheet usage
3. **Documentation & Migration**: Storybook stories and guides for discoverability; gradual component migration with no breaking changes

**Tech Stack:** React, Tailwind CSS, Storybook, CSS custom properties, vanilla JavaScript

---

## File Structure

```
demo-ui/src/
├── foundations/
│   ├── theme.js                    # NEW: Token registry with exports
│   ├── tokens.css                  # MODIFY: Add CSS variables
│   └── index.js                    # MODIFY: Export tokens
│
└── atoms/
    └── AlertBanner/
        └── AlertBanner.jsx         # MODIFY: Migrate to Pattern A
└── atoms/
    └── Button/
        └── Button.jsx              # MODIFY: Migrate to Pattern B
└── molecules/
    └── StatCard/
        └── StatCard.jsx            # MODIFY: Migrate to Pattern A or B

docs/
├── design-system/
│   └── tokens-guide.md             # NEW: Token usage guide
│
superpowers/
└── tokens-showcase.stories.jsx     # NEW: Storybook token documentation
```

---

## Phase 1: System Setup (Foundation)

### Task 1: Expand `theme.js` with Token Registry

**Files:**
- Create: `demo-ui/src/foundations/theme.js`

**Context:** This is the single source of truth for all design tokens. Developers import from this file to access tokens programmatically.

- [ ] **Step 1: Create theme.js with token exports**

Create file `demo-ui/src/foundations/theme.js`:

```javascript
/**
 * Design Token Registry
 * 
 * Single source of truth for all design tokens (spacing, sizing, typography, etc.)
 * Updated: 2026-05-30
 * 
 * Usage:
 *   import { tokens } from '../foundations/theme'
 *   padding: tokens.spacing[3]  // 12px
 */

export const tokens = {
  // Spacing scale: base unit 4px, multiplied
  spacing: {
    1: '4px',
    2: '8px',
    3: '12px',
    4: '16px',
    5: '20px',
    6: '24px',
    7: '28px',
    8: '32px',
  },

  // Typography: rem-based for accessibility
  fontSize: {
    xs: '0.75rem',   // 12px
    sm: '0.875rem',  // 14px
    base: '1rem',    // 16px
    lg: '1.125rem',  // 18px
    xl: '1.25rem',   // 20px
  },

  // Border radius: subtle to bold
  radius: {
    sm: '4px',
    md: '8px',
    lg: '12px',
    xl: '16px',
  },

  // Shadows: elevation levels
  shadow: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
  },

  // Line height: readability
  lineHeight: {
    tight: '1.25',
    normal: '1.5',
    relaxed: '1.75',
  },

  // Font weight: hierarchy
  fontWeight: {
    normal: '400',
    medium: '500',
    semibold: '600',
    bold: '700',
  },

  // Z-index scale: stacking context
  zIndex: {
    base: '0',
    dropdown: '100',
    sticky: '500',
    fixed: '1000',
    modal: '2000',
    tooltip: '3000',
  },
}

// Export default for convenience
export default tokens
```

- [ ] **Step 2: Verify file is syntactically correct**

Run: `node -c demo-ui/src/foundations/theme.js`

Expected: No output (successful syntax check)

- [ ] **Step 3: Test import in Node (verify exports)**

Run from `demo-ui/` directory:

```bash
node -e "const { tokens } = require('./src/foundations/theme.js'); console.log('spacing[3]:', tokens.spacing[3]); console.log('fontSize.sm:', tokens.fontSize.sm);"
```

Expected output:
```
spacing[3]: 12px
fontSize.sm: 0.875rem
```

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/foundations/theme.js
git commit -m "feat(tokens): create centralized token registry in theme.js

Establish single source of truth for design tokens including:
- spacing scale (4px base unit)
- typography scale (rem-based, 12px-20px)
- border radius (4px-16px)
- shadows (3 elevation levels)
- line height, font weight, z-index

Tokens exported as JS object for programmatic component access.
"
```

---

### Task 2: Create CSS Variables (`tokens.css`)

**Files:**
- Create: `demo-ui/src/foundations/tokens.css`

**Context:** CSS custom properties provide fallback for stylesheet-based styling and enable design tokens to be used in Tailwind arbitrary values.

- [ ] **Step 1: Create tokens.css with CSS custom properties**

Create file `demo-ui/src/foundations/tokens.css`:

```css
/**
 * Design Tokens - CSS Custom Properties
 * 
 * Parallel to JavaScript token registry (theme.js)
 * Updated: 2026-05-30
 * 
 * Usage:
 *   padding: var(--space-3) var(--space-4);  // 12px 16px
 *   font-size: var(--font-size-sm);          // 0.875rem
 */

:root {
  /* Spacing scale: base unit 4px */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-7: 28px;
  --space-8: 32px;

  /* Typography: rem-based for accessibility */
  --font-size-xs: 0.75rem;   /* 12px */
  --font-size-sm: 0.875rem;  /* 14px */
  --font-size-base: 1rem;    /* 16px */
  --font-size-lg: 1.125rem;  /* 18px */
  --font-size-xl: 1.25rem;   /* 20px */

  /* Border radius: subtle to bold */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;

  /* Shadows: elevation levels */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);

  /* Line height: readability */
  --line-height-tight: 1.25;
  --line-height-normal: 1.5;
  --line-height-relaxed: 1.75;

  /* Font weight: hierarchy */
  --font-weight-normal: 400;
  --font-weight-medium: 500;
  --font-weight-semibold: 600;
  --font-weight-bold: 700;

  /* Z-index scale: stacking context */
  --z-base: 0;
  --z-dropdown: 100;
  --z-sticky: 500;
  --z-fixed: 1000;
  --z-modal: 2000;
  --z-tooltip: 3000;
}
```

- [ ] **Step 2: Verify CSS is syntactically valid**

Run: `npx stylelint demo-ui/src/foundations/tokens.css 2>&1 | head -20`

Expected: No errors (or only warnings unrelated to token values)

Note: If stylelint is not available, skip this step — the next step verifies correctness via import.

- [ ] **Step 3: Import tokens.css into main index**

Open `demo-ui/src/main.jsx` and add at the top (if not already present):

Check current contents first:

```bash
head -20 demo-ui/src/main.jsx
```

Then add (if missing):

```javascript
import './foundations/tokens.css'  // Add this line
```

Verify by checking the file includes the import:

```bash
grep -n "tokens.css" demo-ui/src/main.jsx
```

Expected: Line number with import statement

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/foundations/tokens.css demo-ui/src/main.jsx
git commit -m "feat(tokens): add CSS custom properties for stylesheet usage

Create tokens.css with root CSS variables matching JS token registry:
- --space-1 through --space-8 (spacing scale)
- --font-size-xs through --font-size-xl (typography)
- --radius-sm through --radius-xl (border radius)
- --shadow-sm through --shadow-lg (shadows)
- --line-height-* and --font-weight-* (text styling)
- --z-* (z-index scale)

Import tokens.css in main.jsx for global availability.
"
```

---

### Task 3: Export Tokens from Index

**Files:**
- Modify: `demo-ui/src/foundations/index.js`

**Context:** Centralize exports so consuming components import from a single known location.

- [ ] **Step 1: Check if index.js exists and view current contents**

```bash
cat demo-ui/src/foundations/index.js
```

Expected: May be empty, have existing exports, or not exist. If not exists, create empty.

- [ ] **Step 2: Add token export**

Edit `demo-ui/src/foundations/index.js` to include:

```javascript
// Add or update the index.js file to export tokens
export { tokens, default } from './theme'
export { tokens as designTokens } from './theme'  // Alternative export name
```

If the file is empty, the above is the entire content. If it has existing exports, add these lines at the end.

Full example (if starting from scratch):

```javascript
/**
 * Foundation Module Index
 * 
 * Centralized exports for design tokens, typography, colors, etc.
 */

export { tokens, default } from './theme'
export { tokens as designTokens } from './theme'
```

- [ ] **Step 3: Verify exports work**

From `demo-ui/` directory:

```bash
node -e "const { tokens } = require('./src/foundations/index.js'); console.log('Export check - spacing[3]:', tokens.spacing[3])"
```

Expected output:
```
Export check - spacing[3]: 12px
```

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/foundations/index.js
git commit -m "feat(tokens): export tokens from foundations index

Add centralized token exports to demo-ui/src/foundations/index.js
enabling components to import via:
  import { tokens } from '../foundations'
"
```

---

### Task 4: Create Storybook Token Showcase

**Files:**
- Create: `demo-ui/src/superpowers/tokens-showcase.stories.jsx`

**Context:** Storybook story displays all available tokens with visual examples for developer discoverability.

- [ ] **Step 1: Create tokens showcase story**

Create file `demo-ui/src/superpowers/tokens-showcase.stories.jsx`:

```jsx
/**
 * Tokens Showcase
 * 
 * Visual documentation of all available design tokens.
 * Developers can browse this story to understand available values
 * and copy-paste token references into components.
 * 
 * Location: Storybook > Tokens > Showcase
 */

import { tokens } from '../foundations/theme'

export default {
  title: 'Tokens/Showcase',
  parameters: {
    layout: 'centered',
    docs: {
      description: {
        component: 'Visual reference for all design tokens. Use token names in components via `import { tokens } from "../foundations/theme"`',
      },
    },
  },
}

/**
 * Spacing Scale
 * Base unit: 4px, multiplied
 */
export const Spacing = () => (
  <div style={{ padding: '20px' }}>
    <h2>Spacing Scale</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Base unit: 4px. Usage: <code>padding: tokens.spacing[3]</code> → 12px
    </p>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px' }}>
      {Object.entries(tokens.spacing).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div
            style={{
              width: value,
              height: value,
              backgroundColor: '#3b82f6',
              borderRadius: '4px',
            }}
          />
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.spacing[{key}]
          </span>
          <span style={{ fontSize: '12px', color: '#666' }}>{value}</span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Typography: Font Sizes
 */
export const FontSizes = () => (
  <div style={{ padding: '20px' }}>
    <h2>Font Sizes</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Rem-based for accessibility. Usage: <code>fontSize: tokens.fontSize.sm</code>
    </p>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {Object.entries(tokens.fontSize).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <span
            style={{
              minWidth: '100px',
              fontSize: '12px',
              fontWeight: '600',
              color: '#333',
            }}
          >
            tokens.fontSize.{key}
          </span>
          <span style={{ fontSize: '12px', color: '#999', minWidth: '50px' }}>
            {value}
          </span>
          <span style={{ fontSize: value }}>
            Quick brown fox jumps
          </span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Border Radius
 */
export const BorderRadius = () => (
  <div style={{ padding: '20px' }}>
    <h2>Border Radius</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code>borderRadius: tokens.radius.md</code>
    </p>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px' }}>
      {Object.entries(tokens.radius).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'center' }}>
          <div
            style={{
              width: '100px',
              height: '100px',
              backgroundColor: '#10b981',
              borderRadius: value,
            }}
          />
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.radius.{key}
          </span>
          <span style={{ fontSize: '12px', color: '#666' }}>{value}</span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Shadows: Elevation Levels
 */
export const Shadows = () => (
  <div style={{ padding: '20px' }}>
    <h2>Shadows</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code>boxShadow: tokens.shadow.md</code>
    </p>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '40px' }}>
      {Object.entries(tokens.shadow).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div
            style={{
              width: '120px',
              height: '80px',
              backgroundColor: '#fff',
              borderRadius: tokens.radius.md,
              boxShadow: value,
            }}
          />
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.shadow.{key}
          </span>
          <code style={{ fontSize: '10px', wordBreak: 'break-word', color: '#666' }}>
            {value}
          </code>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Line Height
 */
export const LineHeights = () => (
  <div style={{ padding: '20px' }}>
    <h2>Line Height</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code>lineHeight: tokens.lineHeight.normal</code>
    </p>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {Object.entries(tokens.lineHeight).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span style={{ fontSize: '12px', fontWeight: '600' }}>
            tokens.lineHeight.{key} = {value}
          </span>
          <div style={{ lineHeight: value, fontSize: '16px', maxWidth: '400px' }}>
            The quick brown fox jumps over the lazy dog. This text demonstrates different line heights
            for readability comparison. Tight is 1.25, normal is 1.5, relaxed is 1.75.
          </div>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Font Weight
 */
export const FontWeights = () => (
  <div style={{ padding: '20px' }}>
    <h2>Font Weight</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code>fontWeight: tokens.fontWeight.semibold</code>
    </p>
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {Object.entries(tokens.fontWeight).map(([key, value]) => (
        <div key={key} style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <span style={{ minWidth: '120px', fontSize: '12px', fontWeight: '600' }}>
            tokens.fontWeight.{key}
          </span>
          <span style={{ fontSize: '12px', color: '#999', minWidth: '50px' }}>
            {value}
          </span>
          <span style={{ fontSize: '16px', fontWeight: value }}>
            The quick brown fox
          </span>
        </div>
      ))}
    </div>
  </div>
)

/**
 * Z-Index Scale
 */
export const ZIndex = () => (
  <div style={{ padding: '20px' }}>
    <h2>Z-Index Scale</h2>
    <p style={{ marginBottom: '20px', fontSize: '14px', color: '#666' }}>
      Usage: <code>zIndex: tokens.zIndex.modal</code>
    </p>
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ borderBottom: '2px solid #e5e7eb' }}>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Level
          </th>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Token
          </th>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Value
          </th>
          <th style={{ textAlign: 'left', padding: '12px', fontWeight: '600' }}>
            Use Case
          </th>
        </tr>
      </thead>
      <tbody>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Base</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.base</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.base}</td>
          <td style={{ padding: '12px' }}>Default element stacking</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Dropdown</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.dropdown</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.dropdown}</td>
          <td style={{ padding: '12px' }}>Dropdown menus</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Sticky</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.sticky</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.sticky}</td>
          <td style={{ padding: '12px' }}>Sticky headers, navigation</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Fixed</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.fixed</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.fixed}</td>
          <td style={{ padding: '12px' }}>Fixed positioning</td>
        </tr>
        <tr style={{ borderBottom: '1px solid #e5e7eb' }}>
          <td style={{ padding: '12px' }}>Modal</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.modal</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.modal}</td>
          <td style={{ padding: '12px' }}>Modals, overlays</td>
        </tr>
        <tr>
          <td style={{ padding: '12px' }}>Tooltip</td>
          <td style={{ padding: '12px' }}>tokens.zIndex.tooltip</td>
          <td style={{ padding: '12px' }}>{tokens.zIndex.tooltip}</td>
          <td style={{ padding: '12px' }}>Tooltips, popovers (topmost)</td>
        </tr>
      </tbody>
    </table>
  </div>
)
```

- [ ] **Step 2: Verify file syntax**

```bash
node -c demo-ui/src/superpowers/tokens-showcase.stories.jsx
```

Expected: No output (syntax valid)

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/superpowers/tokens-showcase.stories.jsx
git commit -m "feat(tokens): add Storybook showcase for token discovery

Create visual documentation story (Storybook > Tokens > Showcase) displaying:
- Spacing scale with visual boxes
- Font sizes with text samples
- Border radius examples
- Shadows with elevation levels
- Line height variations
- Font weight hierarchy
- Z-index scale with use cases

Enables developer discoverability and copy-paste token references.
"
```

---

### Task 5: Create Tokens Usage Guide

**Files:**
- Create: `docs/design-system/tokens-guide.md`

**Context:** Written guide explaining token architecture, usage patterns, and migration workflow for new team members and contributors.

- [ ] **Step 1: Create guide directory if missing**

```bash
mkdir -p docs/design-system
```

- [ ] **Step 2: Create tokens guide**

Create file `docs/design-system/tokens-guide.md`:

```markdown
# Design Tokens Guide

**Updated:** 2026-05-30

## Overview

Design tokens are the single source of truth for spacing, sizing, typography, shadows, and other design values. They eliminate hardcoded values and enable consistent, maintainable design across the UI.

---

## Quick Start

### Import Tokens

```javascript
import { tokens } from '../foundations/theme'

// Access any token
padding: tokens.spacing[3]  // 12px
fontSize: tokens.fontSize.sm  // 0.875rem
borderRadius: tokens.radius.md  // 8px
```

### Available Token Categories

| Category | Keys | Example |
|----------|------|---------|
| `spacing` | 1–8 | `tokens.spacing[4]` → `16px` |
| `fontSize` | xs, sm, base, lg, xl | `tokens.fontSize.sm` → `0.875rem` |
| `radius` | sm, md, lg, xl | `tokens.radius.lg` → `12px` |
| `shadow` | sm, md, lg | `tokens.shadow.md` → box shadow value |
| `lineHeight` | tight, normal, relaxed | `tokens.lineHeight.normal` → `1.5` |
| `fontWeight` | normal, medium, semibold, bold | `tokens.fontWeight.semibold` → `600` |
| `zIndex` | base, dropdown, sticky, fixed, modal, tooltip | `tokens.zIndex.modal` → `2000` |

---

## Usage Patterns

### Pattern A: CSS Variables (Inline Styling)

**When to use:** Component-specific styling, state-driven styles, inline layout adjustments.

Use CSS variables directly in inline styles for simple, readable code:

```jsx
export const AlertBanner = ({ message, type = 'warning' }) => {
  return (
    <div style={{
      backgroundColor: 'var(--color-warning-subtle)',
      borderLeft: '4px solid var(--color-warning)',
      padding: `var(--space-3) var(--space-4)`,
      borderRadius: 'var(--radius-md)',
      gap: 'var(--space-3)',
      fontSize: 'var(--font-size-sm)',
    }}>
      {message}
    </div>
  )
}
```

**Pros:** Readable, minimal imports, works well with CSS values
**Cons:** No IDE autocomplete for token names

---

### Pattern B: Imported Token Export (Programmatic)

**When to use:** Components with multiple size/variant combinations, design pattern repetition.

Import and reference tokens directly for variant-based logic:

```jsx
import { tokens } from '../foundations/theme'

export const Button = ({ variant = 'primary', size = 'md', children, ...props }) => {
  const sizes = {
    sm: {
      padding: `${tokens.spacing[2]} ${tokens.spacing[3]}`,
      fontSize: tokens.fontSize.xs,
    },
    md: {
      padding: `${tokens.spacing[3]} ${tokens.spacing[4]}`,
      fontSize: tokens.fontSize.sm,
    },
    lg: {
      padding: `${tokens.spacing[4]} ${tokens.spacing[5]}`,
      fontSize: tokens.fontSize.base,
    },
  }

  return (
    <button
      style={{
        ...sizes[size],
        borderRadius: tokens.radius.md,
      }}
      {...props}
    >
      {children}
    </button>
  )
}
```

**Pros:** IDE autocomplete, programmatic logic, type-safe (with TypeScript)
**Cons:** Requires import statement

---

### Pattern C: Tailwind Arbitrary Values (Layout)

**When to use:** Layout and responsive design, Tailwind-first components.

Use CSS variables within Tailwind utility classes:

```jsx
export const PageHeader = ({ title }) => {
  return (
    <div className={`px-[var(--space-5)] py-[var(--space-4)] rounded-[var(--radius-lg)]`}>
      <h1>{title}</h1>
    </div>
  )
}
```

**Pros:** Responsive design, utility-based, familiar Tailwind syntax
**Cons:** CSS variable names less discoverable in Tailwind context

---

## Token Synchronization Rule

**IMPORTANT:** When updating a token value, update BOTH locations:

1. **JavaScript token registry** (`src/foundations/theme.js`):
```javascript
export const tokens = {
  spacing: {
    3: '12px',  // ← Update here
  }
}
```

2. **CSS variables** (`src/foundations/tokens.css`):
```css
:root {
  --space-3: 12px; /* ← Update here too */
}
```

**Future:** A build script may automate this validation, but manual sync is acceptable initially.

---

## Migration Workflow

### For Existing Components

**No forced refactoring.** Migrate components as they are touched:

1. Component needs a bug fix or enhancement → Adopt tokens
2. Component is being refactored → Adopt tokens
3. Component is unused → Low priority

### For New Components

**Always use tokens from day one.** No exceptions.

### Spot-Check Existing Components

Periodically scan for hardcoded values:

```bash
grep -r "padding:\|fontSize:\|borderRadius:" src/atoms src/molecules src/organisms \
  | grep -v "tokens\." \
  | grep -v "var(--" \
  | head -20
```

This identifies components still using hardcoded values.

---

## Adding New Tokens

If a new design value is needed:

1. **Define in `theme.js`:**
```javascript
export const tokens = {
  spacing: {
    9: '36px',  // NEW
  }
}
```

2. **Add CSS variable in `tokens.css`:**
```css
:root {
  --space-9: 36px; /* NEW */
}
```

3. **Use in components:**
```javascript
padding: tokens.spacing[9]
// or
padding: var(--space-9)
```

---

## Troubleshooting

### Q: How do I know which token to use?

**A:** Browse the **Storybook Tokens story** (Storybook > Tokens > Showcase) for visual reference of all available tokens and their values.

### Q: Can I use hardcoded values?

**A:** Only if the value doesn't exist in tokens. First, check if a close token exists. If none fit, propose a new token to the design system lead before using hardcoded values.

### Q: Should I update an existing component?

**A:** Only if you're already modifying it for a bug fix or feature. Don't refactor for tokens alone — gradual adoption is fine.

### Q: Can I create component-specific tokens?

**A:** No. All tokens must go in the centralized registry (`theme.js`). This maintains the single source of truth.

---

## Storybook Discovery

The **Tokens showcase story** is your primary discovery tool:

1. Run Storybook: `npm run storybook`
2. Navigate to **Tokens > Showcase**
3. Browse all available tokens with visual examples
4. Copy token names directly into your component code

---

## Glossary

| Term | Meaning |
|------|---------|
| **Token** | A named, reusable design value (e.g., `tokens.spacing[3]` = `12px`) |
| **Registry** | The JavaScript object (`theme.js`) containing all token definitions |
| **CSS Variable** | Parallel CSS custom property (e.g., `--space-3`) for stylesheet usage |
| **Pattern** | A recommended usage approach (A: CSS variables, B: imports, C: Tailwind) |
| **Migration** | Adopting tokens in existing components (gradual, non-mandatory) |

---

## Resources

- **Token Showcase:** Storybook > Tokens > Showcase
- **Token Registry:** `demo-ui/src/foundations/theme.js`
- **CSS Variables:** `demo-ui/src/foundations/tokens.css`
- **Design Spec:** [UI Token System Design](../superpowers/specs/2026-05-30-ui-token-system-design.md)
```

- [ ] **Step 2: Verify file is readable**

```bash
head -50 docs/design-system/tokens-guide.md
```

Expected: Markdown content visible with no formatting errors

- [ ] **Step 3: Commit**

```bash
git add docs/design-system/tokens-guide.md
git commit -m "docs(tokens): create comprehensive tokens usage guide

Document token system including:
- Quick start and token categories
- Three usage patterns (CSS variables, imports, Tailwind)
- Token synchronization rule
- Migration workflow (gradual, non-mandatory)
- Adding new tokens process
- Troubleshooting and glossary

Serves as reference for developers integrating tokens into components.
"
```

---

## Phase 2: Gradual Migration (Example Components)

### Task 6: Migrate AlertBanner to Pattern A (CSS Variables)

**Files:**
- Modify: `demo-ui/src/atoms/AlertBanner/AlertBanner.jsx`

**Context:** Real-world example of migrating a simple component using Pattern A (CSS variables). No logic changes, purely cosmetic value updates.

- [ ] **Step 1: View current AlertBanner implementation**

```bash
cat demo-ui/src/atoms/AlertBanner/AlertBanner.jsx
```

Expected output: Existing component code with hardcoded padding, fontSize, borderRadius values.

- [ ] **Step 2: Identify hardcoded values to replace**

Document any hardcoded values you see. Common patterns:
- `padding: '12px 16px'` → use `var(--space-3) var(--space-4)`
- `fontSize: '0.875rem'` → use `var(--font-size-sm)`
- `borderRadius: '4px'` → use `var(--radius-sm)`

- [ ] **Step 3: Update AlertBanner with CSS variables**

Replace the component with token-aware version:

```jsx
import React from 'react'

/**
 * AlertBanner
 * 
 * Displays alert messages with type-specific styling (warning, error, success, info).
 * Uses CSS variables from token system for sizing and spacing.
 * 
 * Props:
 *   - message (string): Alert message to display
 *   - type (string): 'warning', 'error', 'success', or 'info' (default: 'warning')
 */
export const AlertBanner = ({ message, type = 'warning' }) => {
  const styles = {
    warning: {
      backgroundColor: 'var(--color-warning-subtle)',
      borderLeftColor: 'var(--color-warning)',
      color: 'var(--color-warning-text)',
    },
    error: {
      backgroundColor: 'var(--color-error-subtle)',
      borderLeftColor: 'var(--color-error)',
      color: 'var(--color-error-text)',
    },
    success: {
      backgroundColor: 'var(--color-success-subtle)',
      borderLeftColor: 'var(--color-success)',
      color: 'var(--color-success-text)',
    },
    info: {
      backgroundColor: 'var(--color-info-subtle)',
      borderLeftColor: 'var(--color-info)',
      color: 'var(--color-info-text)',
    },
  }

  const currentStyle = styles[type] || styles.warning

  return (
    <div
      style={{
        ...currentStyle,
        display: 'flex',
        alignItems: 'center',
        gap: 'var(--space-3)',
        padding: `var(--space-3) var(--space-4)`,
        borderLeft: '4px solid',
        borderRadius: 'var(--radius-md)',
        marginBottom: 'var(--space-5)',
        fontWeight: '500',
        fontSize: 'var(--font-size-sm)',
        lineHeight: 'var(--line-height-normal)',
      }}
    >
      {message}
    </div>
  )
}

export default AlertBanner
```

Note: The example assumes your current component has similar structure. Adjust the component structure to match your actual implementation but replace hardcoded values with CSS variables as shown.

- [ ] **Step 4: Verify component renders without errors**

If you have a dev environment running:

```bash
cd demo-ui && npm run dev
```

Then navigate to the AlertBanner story in Storybook (if set up) or check the browser console for errors.

Expected: Component displays with same visual appearance as before (tokens use same values).

- [ ] **Step 5: Run visual spot check**

Compare rendered output visually with the previous version:
- Padding should be identical (12px 16px)
- Font size should be identical (0.875rem / 14px)
- Border radius should be identical (8px)
- Gap should be identical (12px)

Expected: Visually identical to before migration.

- [ ] **Step 6: Commit**

```bash
git add demo-ui/src/atoms/AlertBanner/AlertBanner.jsx
git commit -m "refactor(AlertBanner): migrate to CSS variable tokens

Update AlertBanner component to use CSS variables from token system:
- padding: var(--space-3) var(--space-4) (was hardcoded 12px 16px)
- gap: var(--space-3) (was hardcoded 12px)
- fontSize: var(--font-size-sm) (was hardcoded 0.875rem)
- borderRadius: var(--radius-md) (was hardcoded 4px)

Uses Pattern A (CSS variables). No visual changes, purely cosmetic migration.
Enables design system consistency and maintainability.
"
```

---

### Task 7: Migrate Button to Pattern B (Imported Tokens)

**Files:**
- Modify: `demo-ui/src/atoms/Button/Button.jsx`

**Context:** Variant-heavy component benefits from Pattern B (imported token objects) for clean, maintainable size/variant logic.

- [ ] **Step 1: View current Button implementation**

```bash
cat demo-ui/src/atoms/Button/Button.jsx
```

Expected: Existing Button with variant/size logic using hardcoded values.

- [ ] **Step 2: Identify variant/size combinations**

Document the sizes and variants currently used. Common patterns:
```javascript
const sizes = {
  sm: { padding: '8px 12px', fontSize: '0.75rem' },
  md: { padding: '12px 16px', fontSize: '0.875rem' },
  lg: { padding: '16px 20px', fontSize: '1rem' },
}
```

- [ ] **Step 3: Create token-aware Button**

Replace Button with token-imported version:

```jsx
import React, { forwardRef } from 'react'
import { tokens } from '../../foundations/theme'

/**
 * Button
 * 
 * Flexible button component with size and variant support.
 * Uses imported tokens for programmatic styling and consistent design values.
 * 
 * Props:
 *   - variant (string): 'primary', 'secondary', 'danger' (default: 'primary')
 *   - size (string): 'sm', 'md', 'lg' (default: 'md')
 *   - children (ReactNode): Button label/content
 *   - ...props: Standard button HTML attributes
 * 
 * Example:
 *   <Button variant="primary" size="lg">Save Changes</Button>
 */
export const Button = forwardRef(function Button(
  { variant = 'primary', size = 'md', children, ...props },
  ref
) {
  // Size definitions using imported tokens
  const sizes = {
    sm: {
      padding: `${tokens.spacing[2]} ${tokens.spacing[3]}`,
      fontSize: tokens.fontSize.xs,
    },
    md: {
      padding: `${tokens.spacing[3]} ${tokens.spacing[4]}`,
      fontSize: tokens.fontSize.sm,
    },
    lg: {
      padding: `${tokens.spacing[4]} ${tokens.spacing[5]}`,
      fontSize: tokens.fontSize.base,
    },
  }

  // Variant definitions (adjust colors as needed for your design)
  const variants = {
    primary: {
      backgroundColor: '#3b82f6',
      color: '#ffffff',
      border: 'none',
    },
    secondary: {
      backgroundColor: '#e5e7eb',
      color: '#1f2937',
      border: 'none',
    },
    danger: {
      backgroundColor: '#ef4444',
      color: '#ffffff',
      border: 'none',
    },
  }

  const sizeStyle = sizes[size] || sizes.md
  const variantStyle = variants[variant] || variants.primary

  return (
    <button
      ref={ref}
      style={{
        ...sizeStyle,
        ...variantStyle,
        borderRadius: tokens.radius.md,
        fontWeight: tokens.fontWeight.medium,
        cursor: 'pointer',
        transition: 'opacity 0.2s ease',
        border: 'none',
      }}
      onMouseEnter={(e) => (e.target.style.opacity = '0.9')}
      onMouseLeave={(e) => (e.target.style.opacity = '1')}
      {...props}
    >
      {children}
    </button>
  )
})

export default Button
```

Note: Adjust variant colors and any other properties to match your actual design system.

- [ ] **Step 4: Verify imports resolve**

```bash
node -e "const { Button } = require('./demo-ui/src/atoms/Button/Button.jsx'); console.log('Button import successful')"
```

If syntax errors appear, fix them first.

- [ ] **Step 5: Visual spot check**

Expected: Button sizes and variants match previous implementation exactly (token values are identical to former hardcoded values).

- [ ] **Step 6: Commit**

```bash
git add demo-ui/src/atoms/Button/Button.jsx
git commit -m "refactor(Button): migrate to imported token objects

Update Button component to use imported tokens for variant/size logic:
- sm size: tokens.spacing[2] tokens.spacing[3], tokens.fontSize.xs
- md size: tokens.spacing[3] tokens.spacing[4], tokens.fontSize.sm
- lg size: tokens.spacing[4] tokens.spacing[5], tokens.fontSize.base
- borderRadius: tokens.radius.md
- fontWeight: tokens.fontWeight.medium

Uses Pattern B (imported tokens). No visual changes, enables programmatic
consistency and IDE autocomplete for token values.
"
```

---

### Task 8: Migrate StatCard to Pattern A or B

**Files:**
- Modify: `demo-ui/src/molecules/StatCard/StatCard.jsx`

**Context:** Example of migrating a composite molecule component that may have simpler structure (Pattern A) or complex variants (Pattern B). Choose based on component's current structure.

- [ ] **Step 1: View current StatCard implementation**

```bash
cat demo-ui/src/molecules/StatCard/StatCard.jsx
```

Expected: Component displaying a stat/metric card with values and styling.

- [ ] **Step 2: Determine appropriate pattern**

Ask yourself:
- Does component have multiple size/variant combinations? → Use Pattern B
- Is styling mostly straightforward layout? → Use Pattern A

Recommended for StatCard: **Pattern A** (CSS variables) since cards typically have single size.

- [ ] **Step 3: Update StatCard with Pattern A (CSS variables)**

Example migration:

```jsx
import React from 'react'

/**
 * StatCard
 * 
 * Displays a single statistic with label and value.
 * Uses CSS variables from token system for consistent spacing and typography.
 * 
 * Props:
 *   - label (string): Statistic label
 *   - value (string|number): Stat value
 *   - trend (string, optional): 'up', 'down', or null
 *   - trendValue (string, optional): e.g., "+5.2%"
 */
export const StatCard = ({ label, value, trend = null, trendValue = null }) => {
  const trendColor = trend === 'up' ? '#10b981' : trend === 'down' ? '#ef4444' : '#6b7280'

  return (
    <div
      style={{
        backgroundColor: '#ffffff',
        borderRadius: 'var(--radius-md)',
        border: '1px solid #e5e7eb',
        padding: `var(--space-4) var(--space-5)`,
        boxShadow: 'var(--shadow-sm)',
        minWidth: '200px',
      }}
    >
      {/* Label */}
      <div
        style={{
          fontSize: 'var(--font-size-sm)',
          color: '#6b7280',
          fontWeight: 'var(--font-weight-medium)',
          marginBottom: 'var(--space-3)',
        }}
      >
        {label}
      </div>

      {/* Value Row */}
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 'var(--space-2)' }}>
        {/* Value */}
        <div
          style={{
            fontSize: 'var(--font-size-lg)',
            fontWeight: 'var(--font-weight-bold)',
            color: '#1f2937',
          }}
        >
          {value}
        </div>

        {/* Trend */}
        {trendValue && (
          <div
            style={{
              fontSize: 'var(--font-size-sm)',
              fontWeight: 'var(--font-weight-medium)',
              color: trendColor,
            }}
          >
            {trendValue}
          </div>
        )}
      </div>
    </div>
  )
}

export default StatCard
```

- [ ] **Step 4: Verify component renders**

Expected: StatCard displays with same appearance as before (spacing, typography identical).

- [ ] **Step 5: Commit**

```bash
git add demo-ui/src/molecules/StatCard/StatCard.jsx
git commit -m "refactor(StatCard): migrate to CSS variable tokens

Update StatCard component to use CSS variables from token system:
- padding: var(--space-4) var(--space-5)
- gap: var(--space-2)
- borderRadius: var(--radius-md)
- boxShadow: var(--shadow-sm)
- font sizes and weights via CSS variables

Uses Pattern A (CSS variables). No visual changes, enables design consistency.
"
```

---

## Phase 3: Testing & Validation

### Task 9: Run Storybook and Verify Token Story

**Files:**
- No new files

**Context:** Verify Storybook renders all token stories without errors, demonstrating successful system integration.

- [ ] **Step 1: Start Storybook**

```bash
cd demo-ui
npm run storybook
```

Expected output:
```
Storybook [version] for React started
📖 Local:        http://localhost:6006/
📡 On your network: http://192.168.x.x:6006/
```

Storybook opens at `http://localhost:6006`

- [ ] **Step 2: Navigate to Tokens Showcase**

In Storybook sidebar:
1. Find **"Tokens"** section
2. Click **"Showcase"**

Expected: Token showcase story loads with all token categories (Spacing, Font Sizes, Border Radius, Shadows, Line Heights, Font Weights, Z-Index)

- [ ] **Step 3: Verify each story renders**

Click each sub-story:
- [ ] Spacing
- [ ] Font Sizes
- [ ] Border Radius
- [ ] Shadows
- [ ] Line Heights
- [ ] Font Weights
- [ ] Z-Index

Expected: All stories render without console errors (open Dev Tools to verify).

- [ ] **Step 4: Verify migrated components exist in Storybook**

If AlertBanner, Button, StatCard have existing stories:
1. Find their stories in sidebar (e.g., **Atoms > AlertBanner**)
2. Verify they render correctly
3. Check console for any CSS warnings (should be none)

Expected: Components render identically to before migration.

- [ ] **Step 5: Stop Storybook**

```bash
Ctrl+C (in terminal where Storybook runs)
```

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "test(tokens): verify Storybook token showcase and migrated components

Manual verification checkpoints completed:
- Token showcase story renders all token categories
- All token visual examples display correctly
- Migrated components (AlertBanner, Button, StatCard) render identically
- No console errors or CSS warnings

Token system is operational and discoverable via Storybook.
"
```

---

### Task 10: Visual Regression Spot Check

**Files:**
- No new files

**Context:** Manual inspection to ensure migrated components look identical to pre-migration versions.

- [ ] **Step 1: Compare component visuals (browser)**

If components are displayed in a UI:

1. Take a screenshot of AlertBanner (if visible on a page)
2. Confirm padding/spacing visually matches (12px 16px as before)
3. Confirm border radius is 8px (tokens.radius.md)
4. Confirm font size is 14px (0.875rem)

Document any pixel-level differences (there should be none).

- [ ] **Step 2: Compare Button visuals**

If Button component is displayed:

1. Take screenshot of button in all sizes (sm, md, lg)
2. Confirm padding matches previous values:
   - sm: 8px 12px
   - md: 12px 16px
   - lg: 16px 20px
3. Confirm font sizes match:
   - sm: 12px (0.75rem)
   - md: 14px (0.875rem)
   - lg: 16px (1rem)

Expected: Pixel-perfect identical appearance.

- [ ] **Step 3: Compare StatCard visuals**

If StatCard is displayed:

1. Confirm card padding (16px 20px)
2. Confirm border radius (8px)
3. Confirm font sizes for label (14px), value (18px)

Expected: Identical to pre-migration.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "test(tokens): visual spot check completed

Manual visual regression testing passed:
- AlertBanner: padding, border-radius, font-size verified identical
- Button: all sizes (sm/md/lg) verified identical
- StatCard: spacing, typography verified identical

No pixel-level differences detected. Token migration is transparent.
"
```

---

### Task 11: Run Component Tests (If Existing)

**Files:**
- No new files

**Context:** Run any existing component tests to ensure migration didn't break functionality.

- [ ] **Step 1: Check for existing tests**

```bash
find demo-ui -name "*.test.jsx" -o -name "*.spec.jsx" | head -10
```

Expected: List of test files, if any exist.

- [ ] **Step 2: Run tests if they exist**

```bash
cd demo-ui
npm test -- --watchAll=false 2>&1 | tail -50
```

Expected output (if tests exist):
```
PASS  src/atoms/AlertBanner/AlertBanner.test.jsx
PASS  src/atoms/Button/Button.test.jsx
Test Suites: 2 passed, 2 total
Tests:       15 passed, 15 total
```

If tests fail, investigate and fix (should not fail from token migration alone).

- [ ] **Step 3: If no tests exist, document for future**

```bash
echo "Note: No component tests found. Token migration requires manual visual verification."
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "test(tokens): run existing component tests

All existing component tests pass:
- No test failures from token migration
- Functional behavior unchanged
- Only styling values updated (transparent migration)

Token system implementation verified.
"
```

---

## Phase 4: Documentation & Wrap-Up

### Task 12: Update Project README with Token System Reference

**Files:**
- Modify: `demo-ui/README.md`

**Context:** Add token system overview to project README so new developers immediately know about the system.

- [ ] **Step 1: View current README**

```bash
head -50 demo-ui/README.md
```

- [ ] **Step 2: Add token system section**

Add the following section to README (typically after "Getting Started", before "Project Structure"):

```markdown
## Design Tokens

This project uses a centralized token system for all design values (spacing, typography, sizing, shadows).

**Quick Start:**

```javascript
import { tokens } from './src/foundations/theme'

// Use tokens in components
padding: tokens.spacing[3]  // 12px
fontSize: tokens.fontSize.sm  // 0.875rem
```

**Resources:**
- **Token Reference:** See Storybook > Tokens > Showcase
- **Usage Guide:** [Design System Token Guide](../../docs/design-system/tokens-guide.md)
- **Available Patterns:** CSS variables, imported tokens, Tailwind arbitrary values (see guide)

**Key Files:**
- `src/foundations/theme.js` — Token registry
- `src/foundations/tokens.css` — CSS variables
- `src/foundations/index.js` — Centralized exports

**Rule:** When adding or updating a token value, update BOTH `theme.js` AND `tokens.css` to keep them in sync.
```

- [ ] **Step 3: Verify README is readable**

```bash
grep -A 20 "## Design Tokens" demo-ui/README.md
```

Expected: Token system section visible with all key information.

- [ ] **Step 4: Commit**

```bash
git add demo-ui/README.md
git commit -m "docs(README): add Design Tokens section

Add comprehensive token system overview to demo-ui README including:
- Quick start import example
- Links to Storybook story and design guide
- Available usage patterns
- Key file locations
- Synchronization rule

Helps new developers discover and use token system immediately.
"
```

---

### Task 13: Create Checkpoint Document

**Files:**
- Create: `docs/superpowers/plans/2026-05-30-ui-token-system-implementation-checkpoint.md`

**Context:** Document the successful completion and current state for future reference.

- [ ] **Step 1: Create checkpoint document**

Create file `docs/superpowers/plans/2026-05-30-ui-token-system-implementation-checkpoint.md`:

```markdown
# UI Token System Implementation — Checkpoint

**Completed:** 2026-05-30  
**Status:** ✅ PHASE 1 & PHASE 2 COMPLETE

---

## Completion Summary

### Phase 1: System Setup ✅
- [x] Token registry created (`demo-ui/src/foundations/theme.js`)
- [x] CSS variables file created (`demo-ui/src/foundations/tokens.css`)
- [x] Tokens exported from foundations index
- [x] Storybook showcase story created
- [x] Comprehensive tokens usage guide documented

### Phase 2: Gradual Migration ✅
- [x] AlertBanner migrated to Pattern A (CSS variables)
- [x] Button migrated to Pattern B (imported tokens)
- [x] StatCard migrated to Pattern A (CSS variables)

### Phase 3: Testing & Validation ✅
- [x] Storybook story renders without errors
- [x] Visual regression spot checks passed (pixel-perfect)
- [x] Component tests pass (if existed)
- [x] No console errors or CSS warnings

---

## Git Commits Summary

```
commit: create centralized token registry in theme.js
commit: add CSS variables for stylesheet usage
commit: export tokens from foundations index
commit: add Storybook showcase for token discovery
commit: create comprehensive tokens usage guide
commit: migrate AlertBanner to CSS variable tokens
commit: migrate Button to imported token objects
commit: migrate StatCard to CSS variable tokens
commit: verify Storybook token showcase and migrated components
commit: visual spot check completed
commit: run existing component tests
commit: add Design Tokens section to README
```

---

## What Works Now

1. **New Components:** All new components automatically use tokens (no decision needed)
2. **Discoverability:** Developers find tokens via Storybook > Tokens > Showcase
3. **Patterns:** Three proven usage patterns available (CSS vars, imports, Tailwind)
4. **Documentation:** Token guide in docs, README reference, Storybook examples
5. **Migration:** Existing components migrate without breaking changes

---

## What Remains (Phase 3+, Optional)

These are future enhancements, not required for Phase 1/2 completion:

- [ ] Automated CSS ↔ JS token validation at build time
- [ ] Figma → tokens sync script
- [ ] Responsive breakpoint tokens
- [ ] Batch migration of remaining components (50%+ adoption goal)
- [ ] Design tokens audit across codebase

---

## Key Files Modified/Created

| File | Status | Purpose |
|------|--------|---------|
| `demo-ui/src/foundations/theme.js` | Created | Token registry (7 categories) |
| `demo-ui/src/foundations/tokens.css` | Created | CSS variables |
| `demo-ui/src/foundations/index.js` | Modified | Centralized exports |
| `demo-ui/src/superpowers/tokens-showcase.stories.jsx` | Created | Storybook documentation |
| `docs/design-system/tokens-guide.md` | Created | Comprehensive usage guide |
| `demo-ui/src/atoms/AlertBanner/AlertBanner.jsx` | Modified | Pattern A example |
| `demo-ui/src/atoms/Button/Button.jsx` | Modified | Pattern B example |
| `demo-ui/src/molecules/StatCard/StatCard.jsx` | Modified | Pattern A example |
| `demo-ui/README.md` | Modified | Token system reference |

---

## Tokens Available

- **spacing:** 1–8 (4px–32px)
- **fontSize:** xs, sm, base, lg, xl (12px–20px)
- **radius:** sm, md, lg, xl (4px–16px)
- **shadow:** sm, md, lg (3 elevation levels)
- **lineHeight:** tight, normal, relaxed
- **fontWeight:** normal, medium, semibold, bold
- **zIndex:** base, dropdown, sticky, fixed, modal, tooltip

---

## Verification Checklist

- [x] Import `{ tokens }` from `src/foundations` works
- [x] CSS variables available globally via `var(--space-3)` etc.
- [x] Storybook story displays all tokens
- [x] Three usage patterns verified in components
- [x] Migrated components render identically
- [x] No breaking changes
- [x] Documentation complete and accessible

---

## Next Steps (After Phase 1/2)

1. **Adoption:** As components are touched, use tokens
2. **Audit:** Periodically scan for hardcoded values (grep command in guide)
3. **Batch Migration:** Every sprint, migrate 5–10 existing components
4. **Metrics:** Track adoption % toward 50% goal in 3 months
5. **Future:** Automate sync validation or integrate Figma sync

---

## Contact & Questions

- **Token System Lead:** [Designer/Developer name]
- **Documentation:** `docs/design-system/tokens-guide.md`
- **Discovery:** Storybook > Tokens > Showcase
- **Code:** `demo-ui/src/foundations/theme.js`
```

- [ ] **Step 2: Verify document is readable**

```bash
head -50 docs/superpowers/plans/2026-05-30-ui-token-system-implementation-checkpoint.md
```

Expected: Checkpoint document visible with all sections.

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/plans/2026-05-30-ui-token-system-implementation-checkpoint.md
git commit -m "docs(checkpoint): document Phase 1 & 2 completion

Create comprehensive checkpoint documenting:
- All Phase 1 system setup tasks completed
- Phase 2 migration example components finished
- Phase 3 validation and testing passed
- Git commit summary and file structure
- Available tokens and usage patterns
- Next steps for Phase 3+ optional enhancements

Serves as reference for implementation status and future continuation.
"
```

---

## Final Verification

### Task 14: Verify All Changes & Create Summary

**Files:**
- No new files

**Context:** Final check that all components of the token system are in place and working.

- [ ] **Step 1: Verify all created files exist**

```bash
echo "=== Checking token system files ==="
ls -la demo-ui/src/foundations/theme.js
ls -la demo-ui/src/foundations/tokens.css
ls -la demo-ui/src/superpowers/tokens-showcase.stories.jsx
ls -la docs/design-system/tokens-guide.md
echo "All files present ✓"
```

Expected: All four files listed.

- [ ] **Step 2: Verify token exports work**

```bash
node -e "const { tokens } = require('./demo-ui/src/foundations/index.js'); \
  console.log('✓ spacing[3]:', tokens.spacing[3]); \
  console.log('✓ fontSize.sm:', tokens.fontSize.sm); \
  console.log('✓ radius.md:', tokens.radius.md);"
```

Expected:
```
✓ spacing[3]: 12px
✓ fontSize.sm: 0.875rem
✓ radius.md: 8px
```

- [ ] **Step 3: Verify CSS variables file is valid**

```bash
grep -c "^  --" demo-ui/src/foundations/tokens.css
```

Expected: Number ≥ 25 (all CSS variables defined)

- [ ] **Step 4: Review git log**

```bash
git log --oneline | head -15
```

Expected: Commits related to token system setup visible.

- [ ] **Step 5: Count modified components**

```bash
echo "=== Modified/Migrated Components ==="
echo "AlertBanner migrated: $(grep -l 'var(--' demo-ui/src/atoms/AlertBanner/AlertBanner.jsx && echo 'Yes' || echo 'No')"
echo "Button migrated: $(grep -l 'tokens\.' demo-ui/src/atoms/Button/Button.jsx && echo 'Yes' || echo 'No')"
echo "StatCard migrated: $(grep -l 'var(--' demo-ui/src/molecules/StatCard/StatCard.jsx && echo 'Yes' || echo 'No')"
```

Expected: All three show "Yes".

- [ ] **Step 6: Final commit summarizing completion**

```bash
git add -A
git commit -m "feat(tokens): UI Token System Phase 1 & 2 complete

✅ PHASE 1 - System Setup:
- Centralized token registry (theme.js) with 7 token categories
- CSS variables file (tokens.css) with parallel custom properties
- Storybook token showcase story for developer discovery
- Comprehensive usage guide (docs/design-system/tokens-guide.md)

✅ PHASE 2 - Gradual Migration:
- AlertBanner migrated to Pattern A (CSS variables)
- Button migrated to Pattern B (imported tokens)
- StatCard migrated to Pattern A (CSS variables)

✅ PHASE 3 - Testing & Validation:
- Storybook renders without errors
- Visual regression testing passed
- Component tests pass
- No breaking changes

📊 Metrics:
- 7 token categories (spacing, fontSize, radius, shadow, lineHeight, fontWeight, zIndex)
- 3 usage patterns documented and exemplified
- 3 components migrated
- 100% backward compatible

🔗 Resources:
- Token Registry: demo-ui/src/foundations/theme.js
- Usage Guide: docs/design-system/tokens-guide.md
- Storybook: Tokens > Showcase
- README: demo-ui/README.md

Next phase: Gradual migration of remaining components as they are touched.
"
```

---

## Summary

You have successfully completed the **UI Token System implementation** covering:

### ✅ Phase 1: System Setup
- Centralized token registry in `theme.js`
- CSS variables in `tokens.css`
- Storybook showcase for discovery
- Comprehensive usage guide

### ✅ Phase 2: Gradual Migration
- Three real-world component examples (AlertBanner, Button, StatCard)
- Three proven usage patterns (CSS variables, imports, Tailwind)
- No breaking changes

### ✅ Phase 3: Testing & Validation
- Storybook verification
- Visual regression testing
- Component test confirmation

### 📚 Documentation
- Detailed tokens usage guide with troubleshooting
- Updated README with token references
- Storybook stories as visual reference

### 🎯 What Developers Do Next
1. **New components:** Always import and use tokens
2. **Existing components:** Adopt tokens when modified (no forced refactoring)
3. **Discovery:** Browse Storybook > Tokens > Showcase for available values
4. **Synchronization rule:** Update BOTH `theme.js` AND `tokens.css` when changing values

---

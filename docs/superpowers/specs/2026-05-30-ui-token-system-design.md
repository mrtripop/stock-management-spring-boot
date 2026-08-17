# UI Token System Design

**Date:** 2026-05-30  
**Objective:** Establish a maintainable, centralized token system for frontend UI sizing, spacing, typography, and visual properties. Eliminate hardcoded values and create a single source of truth for design decisions.

---

## Problem Statement

The current frontend UI components use hardcoded sizing values scattered throughout the codebase:
- Spacing/padding: `padding: '12px 16px'` (AlertBanner)
- Font sizes: `fontSize: '0.875rem'` (various components)
- Border radius: `borderRadius: '4px'` (hardcoded)
- Shadows: `shadow-sm`, `shadow-md` (inconsistent reference)

This creates maintenance challenges:
- Designers and developers cannot easily identify all places where a value is used
- Updating a design decision requires grepping and manual changes across multiple files
- New developers don't know what values are available or preferred
- No clear pattern for consistency

---

## Design Goals

1. **Centralized source of truth** — All design tokens (spacing, sizing, typography, borders, shadows) defined in one location
2. **Programmatic access** — Tokens exported as JavaScript objects for component consumption
3. **CSS variable fallback** — CSS custom properties remain as baseline for global styles
4. **Gradual migration** — Existing components continue working; new/touched components adopt the system
5. **Developer discoverability** — Clear patterns for how and where to find available tokens
6. **Zero breaking changes** — System introduction doesn't require refactoring existing code

---

## Solution: Structured Token Export

### Architecture Overview

**Single source of truth:** `demo-ui/src/foundations/theme.js`

The token registry exports all design values organized by category:

```javascript
export const tokens = {
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
  
  fontSize: {
    xs: '0.75rem',   // 12px
    sm: '0.875rem',  // 14px
    base: '1rem',    // 16px
    lg: '1.125rem',  // 18px
    xl: '1.25rem',   // 20px
  },
  
  radius: {
    sm: '4px',
    md: '8px',
    lg: '12px',
    xl: '16px',
  },
  
  shadow: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
  },
}
```

**Parallel CSS source:** `demo-ui/src/foundations/tokens.css` (or equivalent) maintains CSS custom properties (`--space-1`, `--space-2`, etc.) for stylesheet usage.

**Why both?** 
- CSS variables used in global styles and pure CSS contexts
- Token exports used in JavaScript components for programmatic styling
- Developers can choose the appropriate tool for their context

---

## Component Usage Patterns

### Pattern A: Direct CSS Variables (Inline Styling)

Use CSS variables directly for straightforward styling logic:

```jsx
import React from 'react'

export const AlertBanner = ({ message, type = 'warning' }) => {
  const styles = {
    warning: {
      backgroundColor: 'var(--color-warning-subtle)',
      borderLeft: '4px solid var(--color-warning)',
      color: 'var(--color-warning-text)',
    },
  }

  const currentStyle = styles[type] || styles.warning

  return (
    <div style={{
      ...currentStyle,
      display: 'flex',
      alignItems: 'center',
      gap: 'var(--space-3)',           // 12px
      padding: `var(--space-3) var(--space-4)`,  // 12px 16px
      borderRadius: 'var(--radius-md)',
      marginBottom: 'var(--space-5)',
      fontWeight: '500',
      fontSize: 'var(--font-size-sm)',
    }}>
      {/* content */}
    </div>
  )
}
```

**When to use:** Component-specific styling, state-driven styles, inline layout adjustments.

---

### Pattern B: Imported Token Export (Programmatic)

Import tokens and reference them directly for variant-based components:

```jsx
import { tokens } from '../foundations/theme'

export const Button = forwardRef(function Button(
  { variant = 'primary', size = 'md', children, ...props },
  ref
) {
  const sizes = {
    sm: { padding: `${tokens.spacing[2]} ${tokens.spacing[3]}`, fontSize: tokens.fontSize.xs },
    md: { padding: `${tokens.spacing[3]} ${tokens.spacing[4]}`, fontSize: tokens.fontSize.sm },
    lg: { padding: `${tokens.spacing[4]} ${tokens.spacing[5]}`, fontSize: tokens.fontSize.base },
  }

  return (
    <button
      ref={ref}
      style={{
        ...sizes[size],
        borderRadius: tokens.radius.md,
        // ... other styles
      }}
      {...props}
    >
      {children}
    </button>
  )
})
```

**When to use:** Components with multiple size/variant combinations, design pattern repetition.

---

### Pattern C: Tailwind Arbitrary Values (Layout)

For Tailwind-based layout and responsive spacing:

```jsx
export const PageHeader = ({ title }) => {
  return (
    <div className={`px-[var(--space-5)] py-[var(--space-4)] rounded-[var(--radius-lg)]`}>
      <h1>{title}</h1>
    </div>
  )
}
```

**When to use:** Layout and responsive design, Tailwind-first components.

---

## Implementation Phases

### Phase 1: System Setup (Foundation)
- Expand `foundations/theme.js` with token exports
- Ensure CSS custom properties (`--space-1`, etc.) are defined
- Create a Storybook story showcasing all tokens
- Document usage patterns in `docs/design-system/tokens-guide.md`

### Phase 2: Gradual Migration (Ongoing)
- As components are modified or created, adopt one of the three patterns
- No forced refactoring of existing code
- New components always use tokens from day one
- No behavioral changes; migration is cosmetic (sizing values remain identical)

### Phase 3: Optional Audit (Future)
- Periodically scan codebase for hardcoded measurements
- Batch-migrate low-risk components
- Track adoption metrics

---

## Maintaining Token Synchronization

**Rule:** When updating a token value, update BOTH the token export in `theme.js` AND the corresponding CSS variable.

```javascript
// theme.js
export const tokens = {
  spacing: {
    3: '12px',  // Update here
    // ...
  }
}
```

```css
/* tokens.css */
:root {
  --space-3: 12px; /* Update here too */
}
```

**Future optimization:** A build script can automate CSS → JS sync or validate both are in sync, but manual sync is acceptable initially.

---

## Discoverability & Adoption

### 1. In-Code Discovery
Token names are self-documenting:
```javascript
padding: `${tokens.spacing[3]} ${tokens.spacing[4]}`  // Developer sees: 12px 16px
```

### 2. Storybook Documentation
Create a dedicated "Tokens" story showing:
- Full spacing scale with visual boxes
- Typography scale with samples
- Color palette with swatches
- Shadows with previews
- Border radius examples

### 3. Design System Guide
Create `docs/design-system/tokens-guide.md` covering:
- Available token categories
- Use cases and examples
- How to add new tokens
- Migration workflow

---

## Testing & Validation

- **Visual regression tests:** Catch any pixel-level differences during migration
- **No functional tests needed:** Token migration is purely cosmetic
- **Manual QA:** Spot-check components after migration to confirm sizing is identical

---

## Success Criteria

1. ✅ Token system is operational and documented
2. ✅ New components use tokens by default
3. ✅ At least 50% of existing components migrated within 3 months (gradual)
4. ✅ No hardcoded sizing values in new code
5. ✅ Design changes require updates in exactly one place (token definition)

---

## Rollout Timeline

- **Week 1:** Implement Phase 1 (system setup, documentation)
- **Week 2+:** Phase 2 begins (adoption as components are touched)
- **Month 3+:** Optional Phase 3 (audit and batch migration)

---

## Open Questions / Future Enhancements

- Should we auto-generate CSS from tokens or validate sync at build time?
- Do we need a token generation script for Figma/design tool sync?
- Should we extend tokens to include responsive breakpoints?


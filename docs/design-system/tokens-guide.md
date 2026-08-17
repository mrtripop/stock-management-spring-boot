# Design Tokens Guide

**Updated:** 2026-05-30 (Revised)

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

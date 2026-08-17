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
    lg: '0 10px 15px -3px rgba(0, 0, 0,0.1)',
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


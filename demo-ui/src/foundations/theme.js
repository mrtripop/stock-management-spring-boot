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

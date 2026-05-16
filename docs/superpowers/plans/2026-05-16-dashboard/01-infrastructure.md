### Task 1: Infrastructure Setup

**Files:**
- Modify: `demo-ui/package.json`
- Modify: `demo-ui/vite.config.js`
- Create: `demo-ui/src/styles/tokens.css`
- Create: `demo-ui/src/foundations/theme.js`
- Rewrite: `demo-ui/src/index.css`

- [ ] **Step 1: Install dependencies**

```bash
cd demo-ui
npm install tailwindcss @tailwindcss/vite @headlessui/react @heroicons/react @tanstack/react-query sonner
```

- [ ] **Step 2: Update Vite config — add Tailwind plugin**

Replace `demo-ui/vite.config.js`:

```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
```

- [ ] **Step 3: Create design tokens CSS**

Create `demo-ui/src/styles/tokens.css`:

```css
:root {
  /* Colors */
  --color-primary: #0d9488;
  --color-primary-hover: #0f766e;
  --color-sidebar-bg: #134e4a;
  --color-sidebar-active: rgba(13, 148, 136, 0.2);
  --color-danger: #ef4444;
  --color-danger-hover: #dc2626;
  --color-warning: #f59e0b;
  --color-success: #10b981;
  --color-info: #3b82f6;
  --color-purple: #8b5cf6;
  --color-surface: #ffffff;
  --color-background: #f0fdfa;
  --color-text-primary: #0f172a;
  --color-text-secondary: #64748b;
  --color-text-muted: #94a3b8;
  --color-border: #d1fae5;
  --color-border-light: #e2e8f0;

  /* Typography */
  --font-family: Inter, system-ui, -apple-system, sans-serif;
  --font-size-xs: 0.75rem;
  --font-size-sm: 0.875rem;
  --font-size-base: 1rem;
  --font-size-lg: 1.125rem;
  --font-size-xl: 1.25rem;
  --font-size-2xl: 1.5rem;

  /* Spacing */
  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-3: 0.75rem;
  --space-4: 1rem;
  --space-5: 1.25rem;
  --space-6: 1.5rem;
  --space-8: 2rem;

  /* Borders & Radius */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-full: 9999px;

  /* Shadows */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.07);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
}
```

- [ ] **Step 4: Create theme JS reference**

Create `demo-ui/src/foundations/theme.js`:

```javascript
// JS token reference — mirrors tokens.css for programmatic use
export const theme = {
  colors: {
    primary: '#0d9488',
    primaryHover: '#0f766e',
    sidebarBg: '#134e4a',
    sidebarActive: 'rgba(13, 148, 136, 0.2)',
    danger: '#ef4444',
    warning: '#f59e0b',
    success: '#10b981',
    info: '#3b82f6',
    purple: '#8b5cf6',
    surface: '#ffffff',
    background: '#f0fdfa',
    textPrimary: '#0f172a',
    textSecondary: '#64748b',
    textMuted: '#94a3b8',
    border: '#d1fae5',
  },
  radius: { sm: '6px', md: '8px', lg: '12px', full: '9999px' },
  shadow: { sm: '0 1px 2px rgba(0,0,0,0.05)', md: '0 4px 6px rgba(0,0,0,0.07)', lg: '0 10px 15px rgba(0,0,0,0.1)' },
}
```

- [ ] **Step 5: Rewrite index.css — Tailwind imports only**

Replace `demo-ui/src/index.css`:

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

- [ ] **Step 6: Add Inter font to index.html**

Add these lines inside `demo-ui/index.html` `<head>` before the existing `<script>` tag:

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
```

- [ ] **Step 7: Verify build works**

Run: `cd demo-ui && npm run build`
Expected: Build completes without errors. (Pages will break since CSS classes changed — that's OK, fixed in later tasks.)

- [ ] **Step 8: Commit**

```bash
git add demo-ui/package.json demo-ui/package-lock.json demo-ui/vite.config.js demo-ui/src/styles/tokens.css demo-ui/src/foundations/theme.js demo-ui/src/index.css demo-ui/index.html
git commit -m "feat(demo-ui): add Tailwind CSS v4, design tokens, and base infrastructure

Install Tailwind, Headless UI, Heroicons, React Query, and Sonner.
Set up design tokens as CSS custom properties with teal pharmacy theme.
Replace legacy CSS with Tailwind imports."
```

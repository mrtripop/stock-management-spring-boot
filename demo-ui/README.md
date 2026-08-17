# Demo UI — Pharmacy Stock Management Dashboard

Admin dashboard frontend for the Stock Management Spring Boot backend. Built with React 19, Vite 5, and Tailwind CSS v4.

## Prerequisites

- Node.js 18+ (LTS recommended)
- npm 9+
- Backend API running on `http://localhost:8080`

## Tech Stack

| Layer          | Library                               |
|----------------|---------------------------------------|
| UI Framework   | React 19                              |
| Build Tool     | Vite 5                                |
| Styling        | Tailwind CSS v4 (`@tailwindcss/vite`) |
| Routing        | React Router v7                       |
| Data Fetching  | @tanstack/react-query v5              |
| UI Primitives  | @headlessui/react                     |
| Icons          | @heroicons/react                      |
| Notifications  | sonner                                |
| Component Docs | Storybook 8                           |

## Project Structure

```
demo-ui/
├── .storybook/             # Storybook configuration
│   ├── main.js
│   └── preview.js
├── src/
│   ├── atoms/              # Base components (Button, Badge, Input, Select, Spinner, Icon)
│   ├── molecules/          # Composed components (FormField, PageHeader, SearchBar, Pagination, StatCard)
│   ├── organisms/          # Complex components (Sidebar, TopBar, DataTable, FormDrawer, AlertDialog, etc.)
│   ├── templates/          # Layout wrappers (AdminLayout)
│   ├── pages/              # Route pages (Dashboard, Products, Inventory, Orders, etc.)
│   ├── lib/                # API client, React Query hooks
│   ├── foundations/         # Theme tokens (JS mirror of CSS tokens)
│   ├── styles/              # CSS design tokens
│   ├── App.jsx              # Root app with providers and routes
│   ├── main.jsx             # Entry point
│   └── index.css            # Global styles + Tailwind import
├── index.html
├── vite.config.js
└── package.json
```

## Getting Started

### 1. Install dependencies

```bash
cd demo-ui
npm install
```

### 2. Start the backend

The frontend proxies `/api` requests to the backend. Make sure the Spring Boot app is running:

```bash
# From project root
docker compose up -d postgres redis --build
./mvnw spring-boot:run
```

### 3. Start the dev server

```bash
npm run dev
```

Opens at **http://localhost:5173**. API calls to `/api/*` are proxied to `http://localhost:8080`.

## Available Scripts

| Command                   | Description                             |
|---------------------------|-----------------------------------------|
| `npm run dev`             | Start Vite dev server with HMR          |
| `npm run build`           | Production build to `dist/`             |
| `npm run preview`         | Preview production build locally        |
| `npm run lint`            | Run ESLint                              |
| `npm run storybook`       | Start Storybook dev server on port 6006 |
| `npm run build-storybook` | Build static Storybook site             |

## Vite Dev Server

The dev server runs on port **5173** with hot module replacement (HMR). Any change to source files is reflected
instantly in the browser.

### API Proxy

All requests matching `/api/*` are forwarded to the backend:

```
Browser → http://localhost:5173/api/products → http://localhost:8080/api/products
```

Configured in `vite.config.js`:

```js
server: {
  proxy: {
    '/api': 'http://localhost:8080',
  },
},
```

If your backend runs on a different port, update this value.

### Environment Variables

Create `.env.local` for local overrides:

```env
VITE_API_BASE_URL=http://localhost:8080
```

### Production Build

```bash
npm run build    # outputs to dist/
npm run preview  # serves dist/ locally for testing
```

## Storybook

Storybook provides an isolated environment for developing and browsing UI components without needing the full app or
backend.

### Start Storybook

```bash
npm run storybook
```

Opens at **http://localhost:6006**.

### Write a Story

Create a `.stories.jsx` file next to the component:

```jsx
// src/atoms/Button.stories.jsx
import { Button } from './Button'

export default {
  title: 'Atoms/Button',
  component: Button,
}

export const Primary = {
  args: { children: 'Click me' },
}

export const Loading = {
  args: { children: 'Saving...', loading: true },
}
```

### Build Static Storybook

```bash
npm run build-storybook
```

Outputs a static site to `storybook-static/` that can be deployed to any hosting provider.

### Configuration

- **`.storybook/main.js`** — Vite builder, addons (essentials)
- **`.storybook/preview.js`** — Global decorators, background theme

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


## Component Architecture

Components follow **Atomic Design**:

| Level     | Description               | Examples                                               |
|-----------|---------------------------|--------------------------------------------------------|
| Atoms     | Single-purpose primitives | Button, Badge, Input, Select, Spinner, Icon            |
| Molecules | Combinations of atoms     | FormField, PageHeader, SearchBar, Pagination, StatCard |
| Organisms | Complex, context-aware    | Sidebar, TopBar, DataTable, FormDrawer, AlertDialog    |
| Templates | Page layouts              | AdminLayout (Sidebar + TopBar + Outlet)                |
| Pages     | Route-level views         | Dashboard, Products, Inventory, Orders, etc.           |

## Data Fetching

All API calls go through `src/lib/api.js` and are wrapped by React Query hooks in `src/lib/hooks.js`:

| Hook                | Purpose                                   |
|---------------------|-------------------------------------------|
| `useQueryList`      | Paginated list with search, sort, filters |
| `useQueryDetail`    | Single item by ID                         |
| `useCreateMutation` | POST with cache invalidation              |
| `useUpdateMutation` | PUT with cache invalidation               |
| `useDeleteMutation` | DELETE with cache invalidation            |
| `usePostMutation`   | Generic POST for custom endpoints         |

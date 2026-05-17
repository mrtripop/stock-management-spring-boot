### Task 15: Storybook Setup

**Files:**
- Create: `demo-ui/.storybook/main.js`
- Create: `demo-ui/.storybook/preview.js`

- [ ] **Step 1: Initialize Storybook**

```bash
cd demo-ui
npx storybook@latest init --yes --skip-install
```

This creates `.storybook/` and `src/stories/` with default config. We'll overwrite the config files.

- [ ] **Step 2: Configure Storybook for Vite + tokens**

Replace `demo-ui/.storybook/main.js`:

```javascript
/** @type { import('@storybook/react-vite').StorybookConfig } */
const config = {
  stories: ['../src/**/*.stories.@(js|jsx|ts|tsx)'],
  addons: ['@storybook/addon-essentials'],
  framework: {
    name: '@storybook/react-vite',
    options: {},
  },
}
export default config
```

- [ ] **Step 3: Configure preview with tokens**

Replace `demo-ui/.storybook/preview.js`:

```javascript
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
      default: 'teal-bg',
      values: [
        { name: 'teal-bg', value: '#f0fdfa' },
        { name: 'white', value: '#ffffff' },
        { name: 'dark', value: '#134e4a' },
      ],
    },
  },
}
export default preview
```

- [ ] **Step 4: Clean up default Storybook stories**

Remove the auto-generated default stories:

```bash
rm -rf demo-ui/src/stories
```

- [ ] **Step 5: Verify Storybook runs**

Run: `cd demo-ui && npx storybook dev -p 6006`
Expected: Storybook opens in browser showing "No stories found" (stories will be added incrementally per component).

- [ ] **Step 6: Commit**

```bash
git add demo-ui/.storybook/
git commit -m "feat(demo-ui): add Storybook configuration with Vite builder

Configured for @storybook/react-vite with token CSS imports.
Teal background theme preset. Stories co-located with components
using *.stories.jsx pattern in src/ directories."
```

**Note:** Individual component stories (Button.stories.jsx, Badge.stories.jsx, etc.) should be added alongside each component file as part of ongoing development. Each story file follows this pattern:

```jsx
// Example: demo-ui/src/atoms/Button.stories.jsx
import { Button } from './Button'

export default { title: 'Atoms/Button', component: Button }
export const Primary = { args: { variant: 'primary', children: 'Primary' } }
export const Secondary = { args: { variant: 'secondary', children: 'Secondary' } }
export const Danger = { args: { variant: 'danger', children: 'Delete' } }
export const Loading = { args: { variant: 'primary', loading: true, children: 'Saving...' } }
```

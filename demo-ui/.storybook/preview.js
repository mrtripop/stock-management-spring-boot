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

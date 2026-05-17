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

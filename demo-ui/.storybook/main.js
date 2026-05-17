/** @type { import('@storybook/react-vite').StorybookConfig } */
const config = {
  stories: ['../src/**/*.stories.@(js|jsx|ts|tsx)'],
  addons: ['@storybook/addon-essentials'],
  framework: {
    name: '@storybook/react-vite',
    options: {},
  },
  async viteFinal(config) {
    const { default: tailwindcss } = await import('@tailwindcss/vite')
    config.plugins = [...(config.plugins || []), tailwindcss()]
    return config
  },
}
export default config

import { MemoryRouter } from 'react-router-dom'
import { Sidebar } from './Sidebar'

export default {
  title: 'Organisms/Sidebar',
  component: Sidebar,
  decorators: [
    (Story) => (
      <MemoryRouter initialEntries={['/']}>
        <div className="flex h-[600px] bg-[var(--color-background)]">
          <Story />
        </div>
      </MemoryRouter>
    ),
  ],
}

export const Default = {}

export const OnProducts = {
  decorators: [
    (Story) => (
      <MemoryRouter initialEntries={['/products']}>
        <div className="flex h-[600px] bg-[var(--color-background)]">
          <Story />
        </div>
      </MemoryRouter>
    ),
  ],
}

import { MemoryRouter } from 'react-router-dom'
import { Sidebar } from './Sidebar'

export default {
  title: 'Organisms/Sidebar',
  component: Sidebar,
  decorators: [
    (Story, { parameters }) => (
      <MemoryRouter initialEntries={[parameters.initialRoute || '/']}>
        <div className="flex h-[600px] bg-[var(--color-background)]">
          <Story />
        </div>
      </MemoryRouter>
    ),
  ],
}

export const Default = {}

export const OnProducts = {
  parameters: { initialRoute: '/products' },
}

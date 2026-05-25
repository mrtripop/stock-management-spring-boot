import { MemoryRouter } from 'react-router-dom'
import Login from './Login'

export default {
  title: 'Pages/Login',
  component: Login,
  decorators: [
    (Story) => (
      <MemoryRouter>
        <Story />
      </MemoryRouter>
    ),
  ],
}

export const Default = {
  args: { onLogin: () => {} },
}

export const LoginTab = {
  args: { onLogin: () => {} },
}

export const RegisterTab = {
  args: { onLogin: () => {}, initialTab: 'register' },
}

import Login from './Login'

export default {
  title: 'Pages/Login',
  component: Login,
}

export const Default = {
  args: { onLogin: () => {} },
}

export const LoginTab = {
  args: { onLogin: () => {} },
}

export const RegisterTab = {
  render: () => <Login onLogin={() => {}} />,
  play: async ({ canvas }) => {
    const registerTab = canvas.getByText('Register')
    registerTab?.click()
  },
}

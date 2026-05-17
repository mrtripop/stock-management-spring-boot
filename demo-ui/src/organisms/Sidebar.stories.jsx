import { useState } from 'react'
import { MemoryRouter } from 'react-router-dom'
import { Sidebar } from './Sidebar'

const Wrapper = (props) => (
  <MemoryRouter><div className="h-screen"><Sidebar {...props} /></div></MemoryRouter>
)

export default {
  title: 'Organisms/Sidebar',
  component: Sidebar,
  decorators: [(Story) => <div className="h-[700px]"><Story /></div>],
}

export const Expanded = {
  render: () => <Wrapper collapsed={false} user={{ username: 'john', role: 'ADMIN' }} onLogout={() => {}} />,
}

export const Collapsed = {
  render: () => <Wrapper collapsed={true} user={{ username: 'john', role: 'PHARMACIST' }} onLogout={() => {}} />,
}

export const WithStoreSelector = {
  render: () => (
    <Wrapper
      collapsed={false}
      user={{ username: 'john', role: 'MANAGER' }}
      onLogout={() => {}}
      storeOptions={[
        { id: '1', name: 'Main Pharmacy' },
        { id: '2', name: 'Branch Pharmacy' },
        { id: '3', name: 'Warehouse Hub' },
      ]}
      selectedStore="1"
      onStoreChange={() => {}}
    />
  ),
}

export const WithThemeToggle = {
  render: () => {
    const [dark, setDark] = useState(false)
    return (
      <Wrapper
        collapsed={false}
        user={{ username: 'john', role: 'ADMIN' }}
        onLogout={() => {}}
        onToggleTheme={() => setDark(!dark)}
        isDarkMode={dark}
      />
    )
  },
}

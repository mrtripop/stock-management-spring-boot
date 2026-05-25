import { SearchBar } from './SearchBar'

export default {
  title: 'Molecules/SearchBar',
  component: SearchBar,
  argTypes: {
    placeholder: { control: 'text' },
    debounceMs: { control: 'number' },
    onChange: { action: 'change' },
  },
}

export const Default = {
  args: {
    placeholder: 'Search products...',
    onChange: (value) => console.log('Search:', value),
  },
}

export const WithInitialValue = {
  args: {
    value: 'Paracetamol',
    placeholder: 'Search products...',
    onChange: (value) => console.log('Search:', value),
  },
}

export const SlowDebounce = {
  args: {
    placeholder: 'Slow debounce (1000ms)',
    debounceMs: 1000,
    onChange: (value) => console.log('Search:', value),
  },
}

export const NoDebounce = {
  args: {
    placeholder: 'Instant (0ms debounce)',
    debounceMs: 0,
    onChange: (value) => console.log('Search:', value),
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-6 max-w-md">
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Default (300ms debounce)</p>
        <SearchBar placeholder="Search products..." onChange={(v) => console.log(v)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">With initial value</p>
        <SearchBar value="Amoxicillin" placeholder="Search..." onChange={(v) => console.log(v)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Custom placeholder</p>
        <SearchBar placeholder="Search by molecule name..." onChange={(v) => console.log(v)} />
      </div>
    </div>
  ),
}

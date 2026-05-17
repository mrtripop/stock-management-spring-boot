import { useState } from 'react'
import { Input } from '../atoms/Input'
import { Icon } from '../atoms/Icon'

export function SearchBar({ placeholder = 'Search...', onSearch, filterSlot, className = '' }) {
  const [value, setValue] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    onSearch?.(value)
  }

  return (
    <form onSubmit={handleSubmit} className={`flex items-center gap-2 ${className}`}>
      <div className="relative flex-1">
        <Icon name="search" className="absolute left-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--color-text-muted)]" />
        <Input
          value={value}
          onChange={(e) => { setValue(e.target.value); if (!e.target.value) onSearch?.('') }}
          placeholder={placeholder}
          className="pl-8"
        />
      </div>
      {filterSlot}
    </form>
  )
}
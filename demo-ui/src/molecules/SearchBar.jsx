import { useState, useEffect, useRef, useCallback } from 'react'
import { Icon } from '../atoms/Icon'

export function SearchBar({ value, onChange, placeholder = 'Search...', debounceMs = 300, className = '' }) {
  const [internalValue, setInternalValue] = useState(value ?? '')
  const debounceRef = useRef(null)

  // Sync external value changes
  useEffect(() => {
    if (value !== undefined) {
      setInternalValue(value)
    }
  }, [value])

  const handleChange = useCallback(
    (e) => {
      const newValue = e.target.value
      setInternalValue(newValue)

      if (debounceRef.current) {
        clearTimeout(debounceRef.current)
      }

      debounceRef.current = setTimeout(() => {
        onChange?.(newValue)
      }, debounceMs)
    },
    [onChange, debounceMs]
  )

  const handleClear = useCallback(() => {
    setInternalValue('')
    onChange?.('')
  }, [onChange])

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current)
      }
    }
  }, [])

  const showClear = internalValue.length > 0

  return (
    <div className={`relative ${className}`}>
      <Icon
        name="magnifying-glass"
        className="absolute left-[var(--space-3)] top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--color-text-muted)] pointer-events-none"
      />
      <input
        type="text"
        value={internalValue}
        onChange={handleChange}
        placeholder={placeholder}
        className="w-full pl-9 pr-8 py-2 text-sm rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text-primary)] outline-none transition-colors focus:border-[var(--color-border-focus)] focus:ring-2 focus:ring-[var(--color-primary)]/20 placeholder:text-[var(--color-text-muted)]"
      />
      {showClear && (
        <button
          type="button"
          onClick={handleClear}
          className="absolute right-[var(--space-2)] top-1/2 -translate-y-1/2 w-5 h-5 flex items-center justify-center rounded-[var(--radius-sm)] text-[var(--color-text-muted)] hover:text-[var(--color-text-secondary)] hover:bg-[var(--color-background)] transition-colors cursor-pointer"
          aria-label="Clear search"
        >
          <Icon name="x-mark" className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  )
}

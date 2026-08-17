import React from 'react'
import { Spinner } from '../atoms/Spinner'

export const AlertBanner = ({ message, type = 'warning' }) => {
  const styles = {
    warning: {
      backgroundColor: 'var(--color-warning-subtle)',
      borderLeft: '4px solid var(--color-warning)',
      color: 'var(--color-warning-text)',
    },
    // Add other types here if needed in the future
  }

  const currentStyle = styles[type] || styles.warning

  return (
    <div style={{
      ...currentStyle,
      display: 'flex',
      alignItems: 'center',
      gap: 'var(--space-3)',
      padding: 'var(--space-3) var(--space-4)',
      borderRadius: 'var(--radius-sm)',
      marginBottom: 'var(--space-5)',
      fontWeight: 'var(--font-medium)',
      fontSize: 'var(--text-sm)',
    }}>
      <Spinner size="sm" />
      <span>{message}</span>
    </div>
  )
}

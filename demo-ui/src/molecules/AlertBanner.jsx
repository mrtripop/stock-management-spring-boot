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
      gap: '12px',
      padding: '12px 16px',
      borderRadius: '4px',
      marginBottom: '20px',
      fontWeight: '500',
      fontSize: '0.875rem',
    }}>
      <Spinner size="sm" />
      <span>{message}</span>
    </div>
  )
}

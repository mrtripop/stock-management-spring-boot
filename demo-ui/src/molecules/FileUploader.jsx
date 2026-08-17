import { useState, useRef, useCallback } from 'react'
import { Icon } from '../atoms/Icon'
import { Spinner } from '../atoms/Spinner'

const MAX_DEFAULT_SIZE = 10 * 1024 * 1024 // 10 MB

function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function FileUploader({
  accept,
  onFile,
  maxSize = MAX_DEFAULT_SIZE,
  loading = false,
  error,
  label = 'Upload File',
  className = '',
}) {
  const inputRef = useRef(null)
  const [isDragging, setIsDragging] = useState(false)
  const [selectedFile, setSelectedFile] = useState(null)
  const [sizeError, setSizeError] = useState('')

  const displayError = error || sizeError

  const processFile = useCallback(
    (file) => {
      setSizeError('')
      if (file && file.size > maxSize) {
        setSizeError(`File too large. Maximum size is ${formatFileSize(maxSize)}.`)
        return
      }
      setSelectedFile(file)
      if (file) {
        onFile?.(file)
      }
    },
    [maxSize, onFile]
  )

  const handleDrop = useCallback(
    (e) => {
      e.preventDefault()
      setIsDragging(false)
      const file = e.dataTransfer.files[0]
      processFile(file)
    },
    [processFile]
  )

  const handleDragOver = useCallback((e) => {
    e.preventDefault()
    setIsDragging(true)
  }, [])

  const handleDragLeave = useCallback((e) => {
    e.preventDefault()
    setIsDragging(false)
  }, [])

  const handleClick = useCallback(() => {
    inputRef.current?.click()
  }, [])

  const handleInputChange = useCallback(
    (e) => {
      const file = e.target.files[0]
      processFile(file)
    },
    [processFile]
  )

  return (
    <div className={className}>
      <div
        role="button"
        tabIndex={0}
        onClick={loading ? undefined : handleClick}
        onDrop={loading ? undefined : handleDrop}
        onDragOver={loading ? undefined : handleDragOver}
        onDragLeave={loading ? undefined : handleDragLeave}
        onKeyDown={(e) => {
          if ((e.key === 'Enter' || e.key === ' ') && !loading) {
            e.preventDefault()
            handleClick()
          }
        }}
        className={`flex flex-col items-center justify-center gap-[var(--space-2)] py-[var(--space-8)] px-[var(--space-4)] border-2 border-dashed rounded-[var(--radius-lg)] transition-colors cursor-pointer ${
          displayError
            ? 'border-[var(--color-danger)] bg-[var(--color-danger-subtle)]'
            : isDragging
              ? 'border-[var(--color-primary)] bg-[var(--color-primary-subtle)]'
              : 'border-[var(--color-border)] hover:border-[var(--color-primary)] hover:bg-[var(--color-primary-subtle)]'
        } ${loading ? 'opacity-50 cursor-wait' : ''}`}
      >
        {loading ? (
          <>
            <Spinner size="md" />
            <span className="text-sm text-[var(--color-text-secondary)]">Uploading...</span>
          </>
        ) : selectedFile && !displayError ? (
          <>
            <Icon name="check" className="w-8 h-8 text-[var(--color-success)]" />
            <span className="text-sm font-medium text-[var(--color-text-primary)]">
              {selectedFile.name}
            </span>
            <span className="text-xs text-[var(--color-text-muted)]">
              {formatFileSize(selectedFile.size)}
            </span>
          </>
        ) : (
          <>
            <Icon name="arrow-up-tray" className="w-8 h-8 text-[var(--color-text-muted)]" />
            <span className="text-sm text-[var(--color-text-secondary)]">
              <span className="text-[var(--color-primary)] font-medium">Click to upload</span>
              {' '}or drag and drop
            </span>
            {accept && (
              <span className="text-xs text-[var(--color-text-muted)]">
                Accepted: {accept}
              </span>
            )}
          </>
        )}
      </div>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        onChange={handleInputChange}
        className="hidden"
        aria-label={label}
      />
      {displayError && (
        <p className="text-xs text-[var(--color-danger)] mt-1" role="alert">
          {displayError}
        </p>
      )}
    </div>
  )
}

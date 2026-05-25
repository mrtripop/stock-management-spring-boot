import { useState, useCallback } from 'react'
import { Spinner } from '../atoms/Spinner'
import { Pagination } from '../molecules/Pagination'

const SKELETON_ROWS = 5

export function DataTable({
  columns,
  data = [],
  loading = false,
  emptyMessage = 'No data found',
  // Selection
  selectable = false,
  selectedKeys = [],
  keyField = 'id',
  onSelectionChange,
  // Sorting
  sortKey: externalSortKey,
  sortDir: externalSortDir = 'asc',
  onSort,
  // Pagination
  currentPage = 1,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  className = '',
}) {
  const [internalSortKey, setInternalSortKey] = useState(null)
  const [internalSortDir, setInternalSortDir] = useState('asc')

  const activeSortKey = externalSortKey ?? internalSortKey
  const activeSortDir = externalSortDir ?? internalSortDir

  const isAllSelected = selectable && data.length > 0 && data.every((row) => selectedKeys.includes(row[keyField]))
  const isSomeSelected = selectable && !isAllSelected && data.some((row) => selectedKeys.includes(row[keyField]))

  const handleSort = useCallback(
    (col) => {
      if (!col.sortable) return
      const newDir = activeSortKey === col.key && activeSortDir === 'asc' ? 'desc' : 'asc'
      if (onSort) {
        onSort(col.key, newDir)
      } else {
        setInternalSortKey(col.key)
        setInternalSortDir(newDir)
      }
    },
    [activeSortKey, activeSortDir, onSort]
  )

  const toggleAll = useCallback(() => {
    if (!onSelectionChange) return
    if (isAllSelected) {
      onSelectionChange([])
    } else {
      onSelectionChange(data.map((row) => row[keyField]))
    }
  }, [data, keyField, isAllSelected, onSelectionChange])

  const toggleRow = useCallback(
    (row) => {
      if (!onSelectionChange) return
      const key = row[keyField]
      if (selectedKeys.includes(key)) {
        onSelectionChange(selectedKeys.filter((k) => k !== key))
      } else {
        onSelectionChange([...selectedKeys, key])
      }
    },
    [keyField, selectedKeys, onSelectionChange]
  )

  return (
    <div className={`bg-[var(--color-surface)] rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] overflow-hidden border border-[var(--color-border)] ${className}`}>
      <div className="overflow-x-auto">
        <table className="w-full border-collapse min-w-[600px]">
          <thead>
            <tr className="bg-[var(--color-background)] border-b border-[var(--color-border)]">
              {selectable && (
                <th className="w-10 px-3 py-2.5">
                  <input
                    type="checkbox"
                    checked={isAllSelected}
                    ref={(el) => { if (el) el.indeterminate = isSomeSelected }}
                    onChange={toggleAll}
                    className="rounded border-[var(--color-border)] text-[var(--color-primary)] focus:ring-[var(--color-border-focus)] cursor-pointer"
                  />
                </th>
              )}
              {columns.map((col) => (
                <th
                  key={col.key}
                  className={`px-4 py-2.5 text-left text-xs font-semibold text-[var(--color-text-muted)] uppercase tracking-wide select-none ${col.sortable ? 'cursor-pointer hover:text-[var(--color-text-secondary)]' : ''}`}
                  style={{ width: col.width }}
                  onClick={() => handleSort(col)}
                >
                  <span className="inline-flex items-center gap-1">
                    {col.label}
                    {col.sortable && activeSortKey === col.key && (
                      <span className="text-[var(--color-primary)]">
                        {activeSortDir === 'asc' ? '↑' : '↓'}
                      </span>
                    )}
                  </span>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array.from({ length: SKELETON_ROWS }).map((_, i) => (
                <tr key={`skeleton-${i}`} className="border-b border-[var(--color-border)] last:border-0">
                  {selectable && (
                    <td className="px-3 py-3">
                      <div className="w-4 h-4 bg-[var(--color-border)] rounded animate-pulse" />
                    </td>
                  )}
                  {columns.map((col) => (
                    <td key={col.key} className="px-4 py-3">
                      <div className="h-4 bg-[var(--color-border)] rounded animate-pulse w-3/4" />
                    </td>
                  ))}
                </tr>
              ))
            ) : data.length === 0 ? (
              <tr>
                <td colSpan={columns.length + (selectable ? 1 : 0)} className="py-16 text-center">
                  <p className="text-sm text-[var(--color-text-muted)]">{emptyMessage}</p>
                </td>
              </tr>
            ) : (
              data.map((item) => (
                <tr
                  key={item[keyField]}
                  className={`border-b border-[var(--color-border)] last:border-0 hover:bg-[var(--color-background)] transition-colors ${
                    selectable && selectedKeys.includes(item[keyField]) ? 'bg-[var(--color-primary-subtle)]' : ''
                  }`}
                >
                  {selectable && (
                    <td className="px-3 py-3">
                      <input
                        type="checkbox"
                        checked={selectedKeys.includes(item[keyField])}
                        onChange={() => toggleRow(item)}
                        className="rounded border-[var(--color-border)] text-[var(--color-primary)] focus:ring-[var(--color-border-focus)] cursor-pointer"
                      />
                    </td>
                  )}
                  {columns.map((col) => (
                    <td key={col.key} className="px-4 py-3 text-sm text-[var(--color-text-primary)]">
                      {col.render ? col.render(item) : item[col.key]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={pageSize}
          onPageChange={onPageChange}
        />
      )}
    </div>
  )
}

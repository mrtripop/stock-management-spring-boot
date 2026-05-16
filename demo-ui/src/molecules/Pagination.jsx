import { Button } from '../atoms/Button'

export function Pagination({ currentPage, totalPages, totalItems, pageSize, onPageChange, className = '' }) {
  if (totalPages <= 1) return null

  const start = (currentPage - 1) * pageSize + 1
  const end = Math.min(currentPage * pageSize, totalItems)

  return (
    <div className={`flex justify-between items-center py-3 px-4 border-t border-[var(--color-border-light)] ${className}`}>
      <div className="text-xs text-[var(--color-text-secondary)]">
        Showing {start}–{end} of {totalItems}
      </div>
      <div className="flex gap-1">
        <Button variant="secondary" size="sm" disabled={currentPage <= 1} onClick={() => onPageChange(currentPage - 1)}>
          ‹
        </Button>
        {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
          const page = i + 1
          return (
            <Button key={page} variant={page === currentPage ? 'primary' : 'secondary'} size="sm" onClick={() => onPageChange(page)}>
              {page}
            </Button>
          )
        })}
        <Button variant="secondary" size="sm" disabled={currentPage >= totalPages} onClick={() => onPageChange(currentPage + 1)}>
          ›
        </Button>
      </div>
    </div>
  )
}
import { Button } from '../atoms/Button'
import { Select } from '../atoms/Select'

function getPageRange(current, total, maxVisible = 7) {
  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  const half = Math.floor((maxVisible - 2) / 2)
  let start = current - half
  let end = current + half

  if (start <= 1) {
    start = 2
    end = maxVisible - 1
  } else if (end >= total) {
    end = total - 1
    start = total - maxVisible + 2
  }

  const pages = []
  pages.push(1)

  if (start > 2) {
    pages.push('...')
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  if (end < total - 1) {
    pages.push('...')
  }

  if (total > 1) {
    pages.push(total)
  }

  return pages
}

const PAGE_SIZE_OPTIONS = [10, 25, 50, 100]

export function Pagination({
  currentPage,
  totalPages,
  totalElements,
  pageSize = 10,
  onPageChange,
  onPageSizeChange,
  className = '',
}) {
  if (totalPages <= 1 && !onPageSizeChange) return null

  const startItem = totalElements > 0 ? (currentPage - 1) * pageSize + 1 : 0
  const endItem = Math.min(currentPage * pageSize, totalElements)
  const pages = getPageRange(currentPage, totalPages, 7)

  return (
    <div className={`flex flex-wrap justify-between items-center gap-[var(--space-3)] py-[var(--space-3)] px-[var(--space-4)] border-t border-[var(--color-border)] ${className}`}>
      <div className="flex items-center gap-[var(--space-3)]">
        <span className="text-[length:var(--text-xs)] text-[var(--color-text-secondary)]">
          Showing {startItem}–{endItem} of {totalElements}
        </span>
        {onPageSizeChange && (
          <div className="flex items-center gap-[var(--space-1)]">
            <span className="text-[length:var(--text-xs)] text-[var(--color-text-muted)]">per page</span>
            <Select
              value={pageSize}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
              className="w-16 h-7 text-[length:var(--text-xs)] py-0 px-1"
            >
              {PAGE_SIZE_OPTIONS.map((size) => (
                <option key={size} value={size}>{size}</option>
              ))}
            </Select>
          </div>
        )}
      </div>
      {totalPages > 1 && (
        <div className="flex items-center gap-0.5">
          <Button
            variant="ghost"
            size="sm"
            disabled={currentPage <= 1}
            onClick={() => onPageChange(currentPage - 1)}
            aria-label="Previous page"
          >
            &lsaquo;
          </Button>
          {pages.map((page, index) =>
            page === '...' ? (
              <span
                key={`ellipsis-${index}`}
                className="px-1 text-[length:var(--text-xs)] text-[var(--color-text-muted)] select-none"
              >
                &hellip;
              </span>
            ) : (
              <Button
                key={page}
                variant={page === currentPage ? 'primary' : 'ghost'}
                size="sm"
                onClick={() => onPageChange(page)}
                aria-current={page === currentPage ? 'page' : undefined}
              >
                {page}
              </Button>
            )
          )}
          <Button
            variant="ghost"
            size="sm"
            disabled={currentPage >= totalPages}
            onClick={() => onPageChange(currentPage + 1)}
            aria-label="Next page"
          >
            &rsaquo;
          </Button>
        </div>
      )}
    </div>
  )
}

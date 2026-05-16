import { Spinner } from '../atoms/Spinner'
import { Badge } from '../atoms/Badge'
import { Pagination } from '../molecules/Pagination'

export function DataTable({
  columns,
  data = [],
  loading = false,
  currentPage = 1,
  totalPages = 1,
  totalElements = 0,
  pageSize = 10,
  onPageChange,
  emptyMessage = 'No data found',
  renderRow,
  className = '',
}) {
  return (
    <div className={`bg-white rounded-[var(--radius-lg)] shadow-[var(--shadow-sm)] overflow-hidden border border-[var(--color-border-light)] ${className}`}>
      <table className="w-full border-collapse">
        <thead>
          <tr className="bg-slate-50 border-b border-[var(--color-border-light)]">
            {columns.map((col) => (
              <th
                key={col.key}
                className="px-4 py-2.5 text-left text-[10px] font-semibold text-[var(--color-text-muted)] uppercase tracking-wide"
                style={{ width: col.width }}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={columns.length} className="py-12 text-center">
                <Spinner size="lg" className="mx-auto" />
                <p className="text-xs text-[var(--color-text-muted)] mt-2">Loading...</p>
              </td>
            </tr>
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="py-12 text-center text-sm text-[var(--color-text-muted)]">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((item) => renderRow(item))
          )}
        </tbody>
      </table>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        onPageChange={onPageChange}
      />
    </div>
  )
}

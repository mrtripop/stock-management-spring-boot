import { Icon } from '../atoms/Icon'

export function TableRowActions({ onEdit, onDelete, editTitle = 'Edit', deleteTitle = 'Delete' }) {
  return (
    <span className="inline-flex gap-1">
      <button
        onClick={onEdit}
        title={editTitle}
        className="inline-flex items-center justify-center w-7 h-7 rounded-[var(--radius-sm)] border border-[var(--color-border-light)] text-[var(--color-primary)] hover:border-[var(--color-primary)] hover:bg-teal-50 transition-colors cursor-pointer"
      >
        <Icon name="pencil" className="w-3.5 h-3.5" />
      </button>
      {onDelete && (
        <button
          onClick={onDelete}
          title={deleteTitle}
          className="inline-flex items-center justify-center w-7 h-7 rounded-[var(--radius-sm)] border border-[var(--color-border-light)] text-[var(--color-danger)] hover:border-[var(--color-danger)] hover:bg-red-50 transition-colors cursor-pointer"
        >
          <Icon name="trash" className="w-3.5 h-3.5" />
        </button>
      )}
    </span>
  )
}
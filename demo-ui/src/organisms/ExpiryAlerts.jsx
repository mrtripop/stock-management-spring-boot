import { useNavigate } from 'react-router-dom'

export function ExpiryAlerts({ items = [], className = '' }) {
  const navigate = useNavigate()

  const getDaysUntil = (dateStr) => {
    if (!dateStr) return null
    const diff = new Date(dateStr) - new Date()
    return Math.ceil(diff / (1000 * 60 * 60 * 24))
  }

  return (
    <div className={`bg-white rounded-[var(--radius-lg)] p-4 shadow-[var(--shadow-sm)] ${className}`}>
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-xs font-semibold text-[var(--color-text-primary)]">⚠️ Expiry Alerts</h3>
        <button onClick={() => navigate('/inventory')} className="text-[10px] text-[var(--color-primary)] hover:underline cursor-pointer">
          View all
        </button>
      </div>
      <div className="flex flex-col gap-1.5">
        {items.length === 0 ? (
          <p className="text-xs text-[var(--color-text-muted)] py-4 text-center">No expiring batches</p>
        ) : items.map((item) => {
          const days = getDaysUntil(item.expiryDate)
          const urgent = days !== null && days <= 7
          return (
            <div
              key={item.id || item.batchNumber}
              className={`flex justify-between items-center px-3 py-2 rounded-[var(--radius-sm)] border-l-3 ${
                urgent ? 'bg-red-50 border-l-[var(--color-danger)]' : 'bg-amber-50 border-l-[var(--color-warning)]'
              }`}
            >
              <div>
                <div className="text-[10px] font-semibold text-[var(--color-text-primary)]">{item.productName || item.barcode}</div>
                <div className="text-[8px] text-[var(--color-text-muted)]">
                  Batch: {item.batchNumber || '-'} · {item.quantity} units
                </div>
              </div>
              <div className="text-right">
                <div className={`text-[9px] font-semibold ${urgent ? 'text-[var(--color-danger)]' : 'text-[var(--color-warning)]'}`}>
                  {days !== null ? `${days} days` : '-'}
                </div>
                <div className="text-[7px] text-[var(--color-text-muted)]">
                  {item.expiryDate ? new Date(item.expiryDate).toLocaleDateString() : '-'}
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

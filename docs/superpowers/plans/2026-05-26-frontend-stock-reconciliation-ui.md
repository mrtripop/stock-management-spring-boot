# Stock Reconciliation UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a manual trigger for stock reconciliation in the Inventory Tasks tab, restricted to admins, with a confirmation dialog and a persistent progress banner.

**Architecture:** 
- Use `useMutation` for triggering the reconciliation and `useQuery` with `refetchInterval` for polling the status.
- Update `Inventory.jsx` to manage the visibility of the "In Progress" banner based on the polling status.
- Use `useHasRole('ADMIN')` to control button accessibility.

**Tech Stack:** React, TanStack Query, Tailwind CSS (via design tokens).

---

### Task 1: API Hooks for Reconciliation

**Files:**
- Modify: `demo-ui/src/lib/hooks/useInventory.js`

- [ ] **Step 1: Implement `useTriggerReconcile` mutation**

```javascript
export function useTriggerReconcile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.post('/inventory/admin/reconcile', {}),
    onSuccess: () => {
      // Invalidate tasks to refresh the view
      queryClient.invalidateQueries({ queryKey: TASKS_KEY });
    },
  });
}
```

- [ ] **Step 2: Implement `useReconcileStatus` query with polling**

```javascript
export function useReconcileStatus() {
  return useQuery({
    queryKey: ['inventory', 'reconcile-status'],
    queryFn: async () => {
      const res = await api.get('/inventory/admin/reconcile/status');
      return res.data;
    },
    // Poll every 10 seconds if we are in PROCESSING state
    refetchInterval: (query) => {
      const status = query.state.data?.data?.status;
      return status === 'PROCESSING' ? 10000 : false;
    },
  });
}
```

- [ ] **Step 3: Commit**

```bash
git add demo-ui/src/lib/hooks/useInventory.js
git commit -m "feat(inventory): add reconciliation trigger and status hooks"
```

---

### Task 2: Trigger Button & Confirmation Flow

**Files:**
- Modify: `demo-ui/src/pages/Inventory.jsx`

- [ ] **Step 1: Import required hooks and components**
Import `useHasRole` from `../lib/auth` and `useTriggerReconcile` from `../lib/hooks/useInventory`.

- [ ] **Step 2: Implement Admin check and button logic**
Add the "Reconcile Stock" button to the `PageHeader` actions when `tab === 2`.

```javascript
const isAdmin = useHasRole('ADMIN');
const triggerReconcile = useTriggerReconcile();
const [confirmOpen, setConfirmOpen] = useState(false);

// Update PageHeader actions:
actions={
  tab === 2 ? (
    <div className="flex gap-2">
      <Button onClick={() => triggerScan.mutate()}>Run Scan</Button>
      <Button 
        onClick={() => setConfirmOpen(true)} 
        disabled={!isAdmin}
        title={!isAdmin ? "Administrator privileges required to trigger stock reconciliation." : ""}
      >
        Reconcile Stock
      </Button>
    </div>
  ) : tab === 1 ? (
    <Button onClick={() => setDrawerOpen(true)}>Stock In</Button>
  ) : null
}
```

- [ ] **Step 3: Add `ConfirmationDialog` to the page**

```javascript
<ConfirmationDialog 
  open={confirmOpen} 
  onClose={() => setConfirmOpen(false)} 
  onConfirm={async () => {
    await triggerReconcile.mutateAsync();
    setConfirmOpen(false);
  }}
  title="Confirm Reconciliation"
  message="Triggering a full stock reconciliation will analyze all batches and correct any quantity drifts. This may take a moment. Do you wish to proceed?"
/>
```

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/pages/Inventory.jsx
git commit -m "feat(inventory): implement reconcile stock trigger with admin check and confirmation"
```

---

### Task 3: Monitoring Banner & Polling Logic

**Files:**
- Modify: `demo-ui/src/pages/Inventory.jsx`

- [ ] **Step 1: Integrate `useReconcileStatus` hook**

```javascript
const { data: statusData, isLoading: statusLoading } = useReconcileStatus();
const reconcileStatus = statusData?.data?.status;
```

- [ ] **Step 2: Implement the "In Progress" Banner**
Add the banner at the top of the page content, visible only when `reconcileStatus === 'PROCESSING'`.

```javascript
{reconcileStatus === 'PROCESSING' && (
  <div style={{
    backgroundColor: 'var(--color-warning-subtle)', 
    borderLeft: '4px solid var(--color-warning)', 
    padding: '12px 16px', 
    borderRadius: '4px', 
    marginBottom: '20px', 
    display: 'flex', 
    alignItems: 'center', 
    gap: '12px', 
    color: 'var(--color-warning-text)', 
    fontSize: '0.875rem', 
    fontWeight: 500 
  }}>
    <div className="spinner-small" />
    Stock reconciliation in progress... Correcting quantity drifts across all batches.
  </div>
)}
```
*(Note: Use existing Spinner component if available or a CSS animation for the spinner)*.

- [ ] **Step 3: Handle the "Reconcile Stock" button state during processing**
Disable the button if `reconcileStatus === 'PROCESSING'`.

- [ ] **Step 4: Commit**

```bash
git add demo-ui/src/pages/Inventory.jsx
git commit -m "feat(inventory): add reconciliation progress banner and status polling"
```

---

### Task 4: Verification & Final Polish

- [ ] **Step 1: Test as Admin**
  - Trigger reconciliation $\rightarrow$ Confirm $\rightarrow$ Verify banner appears $\rightarrow$ Verify button is disabled.
- [ ] **Step 2: Test as Non-Admin**
  - Verify button is disabled and shows tooltip.
- [ ] **Step 3: Verify Polling**
  - Verify banner disappears once backend status changes to `COMPLETED` or `FAILED`.
- [ ] **Step 4: Final Commit**
  - Clean up any unused imports or console logs.

```bash
git commit -m "test: verify stock reconciliation flow"
```

import { Select } from './Select'

export default {
  title: 'Atoms/Select',
  component: Select,
  argTypes: {
    label: {
      control: 'text',
      description: 'Label text above the select',
    },
    error: {
      control: 'text',
      description: 'Error message displayed below the select',
    },
    helperText: {
      control: 'text',
      description: 'Helper text displayed below the select',
    },
    placeholder: {
      control: 'text',
      description: 'Placeholder option (disabled, empty value)',
    },
    options: {
      description: 'Array of { value, label } objects',
    },
    disabled: {
      control: 'boolean',
      description: 'Disables the select',
      table: { defaultValue: { summary: 'false' } },
    },
  },
  args: {
    placeholder: 'Select an option...',
    options: [
      { value: 'option1', label: 'Option 1' },
      { value: 'option2', label: 'Option 2' },
      { value: 'option3', label: 'Option 3' },
    ],
  },
}

export const Playground = {
  args: {
    label: 'Choose one',
    id: 'playground',
  },
}

export const Default = {
  args: {
    placeholder: 'Select an option...',
    options: [
      { value: '1', label: 'Option 1' },
      { value: '2', label: 'Option 2' },
      { value: '3', label: 'Option 3' },
    ],
  },
}

export const WithLabel = {
  args: {
    label: 'Category',
    placeholder: 'Choose a category...',
    options: [
      { value: 'otc', label: 'Over the Counter' },
      { value: 'prescription', label: 'Prescription' },
      { value: 'supplement', label: 'Supplement' },
    ],
    id: 'category',
  },
}

export const WithHelperText = {
  args: {
    label: 'Sort by',
    helperText: 'Default sort is by name',
    options: [
      { value: 'name', label: 'Name' },
      { value: 'price', label: 'Price' },
      { value: 'date', label: 'Date Added' },
    ],
    id: 'sort',
  },
}

export const WithError = {
  args: {
    label: 'Role',
    error: 'Please select a role',
    placeholder: 'Select role...',
    options: [
      { value: 'admin', label: 'Admin' },
      { value: 'pharmacist', label: 'Pharmacist' },
      { value: 'employee', label: 'Employee' },
    ],
    id: 'role-error',
  },
}

export const Disabled = {
  args: {
    label: 'Disabled',
    disabled: true,
    options: [
      { value: '1', label: 'Option 1' },
    ],
    id: 'disabled',
  },
}

export const StoreType = {
  name: 'Pharmacy Enum — StoreType',
  render: () => (
    <Select
      label="Store Type"
      placeholder="Select store type..."
      options={[
        { value: 'PHYSICAL', label: 'Physical' },
        { value: 'HUB', label: 'Hub' },
        { value: 'LOGICAL', label: 'Logical' },
      ]}
      id="store-type"
    />
  ),
}

export const UserRole = {
  name: 'Pharmacy Enum — UserRole',
  render: () => (
    <Select
      label="User Role"
      placeholder="Select role..."
      options={[
        { value: 'ADMIN', label: 'Admin' },
        { value: 'MANAGER', label: 'Manager' },
        { value: 'PHARMACIST', label: 'Pharmacist' },
        { value: 'EMPLOYEE', label: 'Employee' },
      ]}
      id="user-role"
    />
  ),
}

export const BatchStatus = {
  name: 'Pharmacy Enum — BatchStatus',
  render: () => (
    <Select
      label="Batch Status"
      placeholder="Filter by status..."
      options={[
        { value: 'AVAILABLE', label: 'Available' },
        { value: 'RECALLED', label: 'Recalled' },
        { value: 'QUARANTINED', label: 'Quarantined' },
      ]}
      id="batch-status"
    />
  ),
}

export const InvoiceStatus = {
  name: 'Pharmacy Enum — InvoiceStatus',
  render: () => (
    <Select
      label="Invoice Status"
      placeholder="Filter by status..."
      options={[
        { value: 'PENDING', label: 'Pending' },
        { value: 'COMPLETED', label: 'Completed' },
        { value: 'VOIDED', label: 'Voided' },
      ]}
      id="invoice-status"
    />
  ),
}

export const StateMatrix = {
  render: () => (
    <div className="flex flex-col gap-4 max-w-sm">
      <Select
        label="Default"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        id="default"
      />
      <Select
        label="With helper"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        helperText="Pick one"
        id="helper"
      />
      <Select
        label="With error"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        error="Required field"
        id="error"
      />
      <Select
        label="Disabled"
        placeholder="Select..."
        options={[{ value: '1', label: 'Option 1' }]}
        disabled
        id="disabled"
      />
    </div>
  ),
}

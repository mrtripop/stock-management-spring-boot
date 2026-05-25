import { FormField } from './FormField'
import { Input } from '../atoms/Input'
import { Select } from '../atoms/Select'

export default {
  title: 'Molecules/FormField',
  component: FormField,
  argTypes: {
    label: { control: 'text' },
    required: { control: 'boolean' },
    error: { control: 'text' },
    helperText: { control: 'text' },
  },
}

export const Default = {
  args: {
    label: 'Username',
    children: <Input placeholder="Enter username" />,
  },
}

export const Required = {
  args: {
    label: 'Email',
    required: true,
    children: <Input type="email" placeholder="email@example.com" />,
  },
}

export const WithError = {
  args: {
    label: 'Password',
    required: true,
    error: 'Password must be at least 8 characters',
    children: <Input type="password" placeholder="Enter password" error />,
  },
}

export const WithHelperText = {
  args: {
    label: 'Batch Number',
    helperText: 'Format: BN-YYYY-XXXX',
    children: <Input placeholder="BN-2026-0001" />,
  },
}

export const WithSelect = {
  args: {
    label: 'Category',
    required: true,
    children: (
      <Select>
        <option value="">Select category</option>
        <option value="1">Tablets</option>
        <option value="2">Capsules</option>
        <option value="3">Syrup</option>
      </Select>
    ),
  },
}

export const ErrorOverridesHelper = {
  args: {
    label: 'Quantity',
    helperText: 'Must be a positive integer',
    error: 'Quantity cannot be negative',
    children: <Input type="number" error />,
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-6 max-w-sm">
      <FormField label="Default" helperText="Standard input field">
        <Input placeholder="Default state" />
      </FormField>
      <FormField label="Required" required>
        <Input placeholder="Required field" />
      </FormField>
      <FormField label="With Error" required error="This field is required">
        <Input placeholder="Error state" error />
      </FormField>
      <FormField label="Select" required helperText="Choose one option">
        <Select>
          <option value="">Pick one</option>
          <option value="a">Option A</option>
          <option value="b">Option B</option>
        </Select>
      </FormField>
    </div>
  ),
}

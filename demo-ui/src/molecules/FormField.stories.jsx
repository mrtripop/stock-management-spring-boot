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
    hint: { control: 'text' },
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
    children: <Input type="password" placeholder="Enter password" />,
  },
}

export const WithHint = {
  args: {
    label: 'Batch Number',
    hint: 'Format: BN-YYYY-XXXX',
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

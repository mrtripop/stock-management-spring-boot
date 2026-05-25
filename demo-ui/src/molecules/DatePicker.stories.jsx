import { useState } from 'react'
import { DatePicker } from './DatePicker'

export default {
  title: 'Molecules/DatePicker',
  component: DatePicker,
  argTypes: {
    label: { control: 'text' },
    value: { control: 'date' },
    min: { control: 'date' },
    max: { control: 'date' },
    error: { control: 'text' },
    disabled: { control: 'boolean' },
    helperText: { control: 'text' },
    required: { control: 'boolean' },
  },
}

export const Default = {
  args: {
    label: 'Expiry Date',
    onChange: (e) => console.log(e.target.value),
  },
}

export const WithValue = {
  args: {
    label: 'Manufacture Date',
    value: '2026-01-15',
    onChange: (e) => console.log(e.target.value),
  },
}

export const WithMinMax = {
  args: {
    label: 'Delivery Date',
    min: '2026-05-17',
    max: '2026-12-31',
    helperText: 'Must be within the current year',
    onChange: (e) => console.log(e.target.value),
  },
}

export const WithError = {
  args: {
    label: 'Expiry Date',
    required: true,
    error: 'Expiry date is required',
    onChange: (e) => console.log(e.target.value),
  },
}

export const Disabled = {
  args: {
    label: 'Created Date',
    value: '2026-03-10',
    disabled: true,
    helperText: 'Auto-set on creation',
    onChange: () => {},
  },
}

export const Interactive = {
  render: () => {
    const [value, setValue] = useState('')
    return (
      <DatePicker
        label="Select a date"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        min="2026-01-01"
        max="2026-12-31"
        helperText={value ? `Selected: ${value}` : 'Pick any date in 2026'}
      />
    )
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-6 max-w-xs">
      <DatePicker label="Default" onChange={() => {}} />
      <DatePicker label="With value" value="2026-06-15" onChange={() => {}} />
      <DatePicker label="Required" required onChange={() => {}} />
      <DatePicker label="With error" required error="Date is required" onChange={() => {}} />
      <DatePicker label="Disabled" value="2026-01-01" disabled onChange={() => {}} />
      <DatePicker label="With helper" helperText="Select a date between today and Dec 31" onChange={() => {}} />
    </div>
  ),
}

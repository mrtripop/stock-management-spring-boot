import { Select } from './Select'

export default {
  title: 'Atoms/Select',
  component: Select,
}

export const Default = {
  render: () => (
    <Select>
      <option value="">Select an option</option>
      <option value="1">Option 1</option>
      <option value="2">Option 2</option>
      <option value="3">Option 3</option>
    </Select>
  ),
}

export const WithError = {
  render: () => (
    <Select error="Please select a value">
      <option value="">Select an option</option>
      <option value="1">Option 1</option>
    </Select>
  ),
}

import { Input } from './Input'

export default {
  title: 'Atoms/Input',
  component: Input,
  argTypes: {
    error: { control: 'text' },
    placeholder: { control: 'text' },
  },
}

export const Default = { args: { placeholder: 'Enter text...' } }

export const WithError = { args: { placeholder: 'Invalid input', error: 'This field is required' } }

export const Disabled = { args: { placeholder: 'Disabled input', disabled: true } }

export const InputTypes = {
  render: () => (
    <div className="flex flex-col gap-3 max-w-sm">
      <Input placeholder="Text input" />
      <Input type="password" placeholder="Password" />
      <Input type="email" placeholder="email@example.com" />
      <Input type="number" placeholder="123" />
    </div>
  ),
}

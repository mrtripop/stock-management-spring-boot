import { Input } from './Input'
import { MagnifyingGlassIcon, ExclamationCircleIcon } from '@heroicons/react/24/outline'

export default {
  title: 'Atoms/Input',
  component: Input,
  argTypes: {
    label: {
      control: 'text',
      description: 'Label text above the input',
    },
    error: {
      control: 'text',
      description: 'Error message displayed below the input',
    },
    helperText: {
      control: 'text',
      description: 'Helper text displayed below the input (hidden when error is set)',
    },
    placeholder: {
      control: 'text',
      description: 'Placeholder text',
    },
    disabled: {
      control: 'boolean',
      description: 'Disables the input',
      table: { defaultValue: { summary: 'false' } },
    },
    leftIcon: {
      description: 'Heroicons component rendered inside the left side',
    },
    rightIcon: {
      description: 'Heroicons component rendered inside the right side',
    },
  },
  args: {
    placeholder: 'Enter text...',
  },
}

export const Playground = {
  args: {
    placeholder: 'Type something...',
    label: 'Label',
  },
}

export const Default = {
  args: { placeholder: 'Enter text...' },
}

export const WithLabel = {
  args: {
    label: 'Username',
    placeholder: 'Enter your username',
    id: 'username',
  },
}

export const WithHelperText = {
  args: {
    label: 'Email',
    placeholder: 'you@example.com',
    helperText: 'We will never share your email.',
    id: 'email',
  },
}

export const WithError = {
  args: {
    label: 'Password',
    placeholder: 'Enter password',
    error: 'Password must be at least 8 characters',
    id: 'password',
  },
}

export const Disabled = {
  args: {
    label: 'Disabled Field',
    placeholder: 'Cannot edit',
    disabled: true,
    id: 'disabled',
  },
}

export const WithLeftIcon = {
  render: () => (
    <Input
      label="Search"
      placeholder="Search products..."
      leftIcon={MagnifyingGlassIcon}
      id="search"
    />
  ),
}

export const WithRightIcon = {
  render: () => (
    <Input
      label="Website"
      placeholder="https://example.com"
      rightIcon={ExclamationCircleIcon}
      id="website"
    />
  ),
}

export const WithBothIcons = {
  render: () => (
    <Input
      label="Search products"
      placeholder="Type to search..."
      leftIcon={MagnifyingGlassIcon}
      rightIcon={ExclamationCircleIcon}
      id="search-both"
    />
  ),
}

export const InputTypes = {
  render: () => (
    <div className="flex flex-col gap-4 max-w-sm">
      <Input label="Text" placeholder="Text input" type="text" id="text" />
      <Input label="Password" placeholder="Password" type="password" id="password" />
      <Input label="Email" placeholder="email@example.com" type="email" id="email" />
      <Input label="Number" placeholder="123" type="number" id="number" />
      <Input label="Date" type="date" id="date" />
    </div>
  ),
}

export const StateMatrix = {
  render: () => (
    <div className="flex flex-col gap-4 max-w-sm">
      <Input label="Default" placeholder="Default state" id="default" />
      <Input label="With helper" placeholder="With helper" helperText="Helper text here" id="helper" />
      <Input label="With error" placeholder="Error state" error="This field is required" id="error" />
      <Input label="Disabled" placeholder="Disabled" disabled id="disabled" />
      <Input label="Error + icon" placeholder="Error with icon" error="Invalid search" leftIcon={MagnifyingGlassIcon} id="error-icon" />
    </div>
  ),
}

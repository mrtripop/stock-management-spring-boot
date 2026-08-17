import { Spinner } from './Spinner'

export default {
  title: 'Atoms/Spinner',
  component: Spinner,
  argTypes: {
    size: {
      control: 'select',
      options: ['sm', 'md', 'lg'],
      description: 'Spinner diameter: sm=16px, md=24px, lg=32px',
      table: { defaultValue: { summary: 'md' } },
    },
    color: {
      control: 'color',
      description: 'Border top color. Defaults to var(--color-primary)',
      table: { defaultValue: { summary: 'var(--color-primary)' } },
    },
  },
  args: {
    size: 'md',
  },
}

export const Playground = {
  args: {
    size: 'md',
  },
}

export const Sizes = {
  render: () => (
    <div className="flex items-center gap-6">
      <div className="flex flex-col items-center gap-2">
        <Spinner size="sm" />
        <span className="text-xs text-[var(--color-text-muted)]">sm</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" />
        <span className="text-xs text-[var(--color-text-muted)]">md</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="lg" />
        <span className="text-xs text-[var(--color-text-muted)]">lg</span>
      </div>
    </div>
  ),
}

export const CustomColors = {
  render: () => (
    <div className="flex items-center gap-6">
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" />
        <span className="text-xs text-[var(--color-text-muted)]">primary</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-danger)" />
        <span className="text-xs text-[var(--color-text-muted)]">danger</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-success)" />
        <span className="text-xs text-[var(--color-text-muted)]">success</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-warning)" />
        <span className="text-xs text-[var(--color-text-muted)]">warning</span>
      </div>
      <div className="flex flex-col items-center gap-2">
        <Spinner size="md" color="var(--color-text-secondary)" />
        <span className="text-xs text-[var(--color-text-muted)]">muted</span>
      </div>
    </div>
  ),
}

export const InsideButton = {
  name: 'Usage — Inside Button',
  render: () => (
    <button
      disabled
      className="inline-flex items-center gap-2 px-4 h-9 text-sm font-medium rounded-[var(--radius-md)] bg-[var(--color-primary)] text-[var(--color-text-inverse)] opacity-75 cursor-not-allowed"
    >
      <Spinner size="sm" color="var(--color-text-inverse)" />
      Saving...
    </button>
  ),
}

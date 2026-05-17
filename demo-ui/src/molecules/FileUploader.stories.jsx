import { FileUploader } from './FileUploader'

export default {
  title: 'Molecules/FileUploader',
  component: FileUploader,
  argTypes: {
    accept: { control: 'text' },
    maxSize: { control: 'number' },
    loading: { control: 'boolean' },
    error: { control: 'text' },
    label: { control: 'text' },
    onFile: { action: 'file' },
  },
}

export const Default = {
  args: {
    onFile: (file) => console.log('File:', file.name),
  },
}

export const CsvOnly = {
  args: {
    accept: '.csv',
    label: 'Upload CSV',
    onFile: (file) => console.log('CSV:', file.name),
  },
}

export const WithSizeLimit = {
  args: {
    accept: '.csv,.xlsx',
    maxSize: 5 * 1024 * 1024,
    onFile: (file) => console.log('File:', file.name),
  },
}

export const Loading = {
  args: {
    loading: true,
    onFile: () => {},
  },
}

export const WithError = {
  args: {
    error: 'Upload failed. Please try again.',
    onFile: () => {},
  },
}

export const MultipleFormats = {
  args: {
    accept: '.csv,.json,.xml',
    onFile: (file) => console.log('File:', file.name),
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-8 max-w-md">
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Default</p>
        <FileUploader onFile={(f) => console.log(f)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">CSV only</p>
        <FileUploader accept=".csv" onFile={(f) => console.log(f)} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Loading</p>
        <FileUploader loading onFile={() => {}} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">Error state</p>
        <FileUploader error="Invalid file format. Please upload a CSV file." onFile={() => {}} />
      </div>
    </div>
  ),
}

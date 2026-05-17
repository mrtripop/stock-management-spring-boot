import { Pagination } from './Pagination'

export default {
  title: 'Molecules/Pagination',
  component: Pagination,
  argTypes: {
    currentPage: { control: 'number' },
    totalPages: { control: 'number' },
    totalElements: { control: 'number' },
    pageSize: { control: 'number' },
    onPageChange: { action: 'pageChange' },
    onPageSizeChange: { action: 'pageSizeChange' },
  },
}

export const Default = {
  args: {
    currentPage: 1,
    totalPages: 5,
    totalElements: 48,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const MiddlePage = {
  args: {
    currentPage: 3,
    totalPages: 5,
    totalElements: 48,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const ManyPages = {
  args: {
    currentPage: 15,
    totalPages: 50,
    totalElements: 492,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const FirstPage = {
  args: {
    currentPage: 1,
    totalPages: 20,
    totalElements: 198,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const LastPage = {
  args: {
    currentPage: 20,
    totalPages: 20,
    totalElements: 198,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const WithPageSizeSelector = {
  args: {
    currentPage: 2,
    totalPages: 10,
    totalElements: 96,
    pageSize: 10,
    onPageChange: () => {},
    onPageSizeChange: () => {},
  },
}

export const SinglePage = {
  args: {
    currentPage: 1,
    totalPages: 1,
    totalElements: 7,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const VariantMatrix = {
  render: () => (
    <div className="flex flex-col gap-8">
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">5 pages, on page 1</p>
        <Pagination currentPage={1} totalPages={5} totalElements={48} pageSize={10} onPageChange={() => {}} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">50 pages, on page 15</p>
        <Pagination currentPage={15} totalPages={50} totalElements={492} pageSize={10} onPageChange={() => {}} />
      </div>
      <div>
        <p className="text-[length:var(--text-xs)] text-[var(--color-text-muted)] mb-2">With page size selector</p>
        <Pagination currentPage={2} totalPages={10} totalElements={96} pageSize={10} onPageChange={() => {}} onPageSizeChange={() => {}} />
      </div>
    </div>
  ),
}

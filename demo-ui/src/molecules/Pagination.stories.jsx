import { Pagination } from './Pagination'

export default {
  title: 'Molecules/Pagination',
  component: Pagination,
  argTypes: {
    onPageChange: { action: 'pageChange' },
  },
}

export const Default = {
  args: {
    currentPage: 1,
    totalPages: 5,
    totalItems: 48,
    pageSize: 10,
    onPageChange: () => {},
  },
}

export const MiddlePage = {
  args: {
    currentPage: 3,
    totalPages: 5,
    totalItems: 48,
    pageSize: 10,
    onPageChange: () => {},
  },
}

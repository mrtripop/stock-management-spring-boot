import { SearchBar } from './SearchBar'
import { Select } from '../atoms/Select'

export default {
  title: 'Molecules/SearchBar',
  component: SearchBar,
  argTypes: {
    placeholder: { control: 'text' },
    onSearch: { action: 'search' },
  },
}

export const Default = { args: { placeholder: 'Search products...' } }

export const WithFilter = {
  render: () => (
    <SearchBar placeholder="Search products..." onSearch={(v) => console.log(v)}>
      <Select className="w-40">
        <option value="">All Categories</option>
        <option value="1">Tablets</option>
        <option value="2">Capsules</option>
      </Select>
    </SearchBar>
  ),
}

import * as Icons from '@heroicons/react/24/outline'

const iconMap = {
  'home': Icons.HomeIcon,
  'cube': Icons.CubeIcon,
  'archive': Icons.ArchiveBoxIcon,
  'beaker': Icons.BeakerIcon,
  'cart': Icons.ShoppingCartIcon,
  'credit-card': Icons.CreditCardIcon,
  'map-pin': Icons.MapPinIcon,
  'users': Icons.UserGroupIcon,
  'logout': Icons.ArrowRightOnRectangleIcon,
  'search': Icons.MagnifyingGlassIcon,
  'bell': Icons.BellIcon,
  'plus': Icons.PlusIcon,
  'pencil': Icons.PencilSquareIcon,
  'trash': Icons.TrashIcon,
  'funnel': Icons.FunnelIcon,
  'arrow-down': Icons.ArrowDownIcon,
  'arrow-up': Icons.ArrowUpIcon,
  'exclamation': Icons.ExclamationTriangleIcon,
  'check': Icons.CheckIcon,
  'x-mark': Icons.XMarkIcon,
  'chevron-left': Icons.ChevronLeftIcon,
  'chevron-right': Icons.ChevronRightIcon,
  'chevron-down': Icons.ChevronDownIcon,
  'arrow-down-tray': Icons.ArrowDownTrayIcon,
  'arrow-up-tray': Icons.ArrowUpTrayIcon,
  'magnifying-glass': Icons.MagnifyingGlassIcon,
  'receipt': Icons.DocumentTextIcon,
  'info': Icons.InformationCircleIcon,
  'sun': Icons.SunIcon,
  'moon': Icons.MoonIcon,
  'clock': Icons.ClockIcon,
}

const sizeMap = {
  sm: 'w-4 h-4',
  md: 'w-5 h-5',
  lg: 'w-6 h-6',
}

export function Icon({ name, size = 'md', className = '' }) {
  const Component = iconMap[name]
  if (!Component) return null
  return <Component className={`${sizeMap[size]} ${className}`} />
}

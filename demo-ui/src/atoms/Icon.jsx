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
  'arrow-down-tray': Icons.ArrowDownTrayIcon,
  'arrow-up-tray': Icons.ArrowUpTrayIcon,
  'magnifying-glass': Icons.MagnifyingGlassIcon,
}

export function Icon({ name, className = '' }) {
  const Component = iconMap[name]
  if (!Component) return null
  return <Component className={`w-5 h-5 ${className}`} />
}
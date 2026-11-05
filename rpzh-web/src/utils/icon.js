import * as ElementPlusIcons from '@element-plus/icons-vue'

/**
 * 后端 tb_menu.icon 存的是若依体系下的字体图标名（system / user / peoples ...），
 * Element Plus 用的是组件名（Setting / User / UserFilled ...），这里做一层别名映射。
 *
 * 找不到对应图标时返回空字符串，调用方不渲染图标，避免 Vue 抛「无法解析组件」的警告。
 */
const ALIAS = {
  system: 'Setting',
  user: 'User',
  peoples: 'UserFilled',
  people: 'UserFilled',
  tree: 'OfficeBuilding',
  treeTable: 'OfficeBuilding',
  dept: 'OfficeBuilding',
  company: 'OfficeBuilding',
  lock: 'Lock',
  password: 'Key',
  key: 'Key',
  log: 'Document',
  document: 'Document',
  form: 'Document',
  edit: 'Edit',
  monitor: 'Monitor',
  server: 'Cpu',
  tool: 'Tools',
  build: 'Tools',
  job: 'AlarmClock',
  time: 'Timer',
  chart: 'DataLine',
  chart2: 'TrendCharts',
  dict: 'Collection',
  nested: 'Share',
  guide: 'Guide',
  message: 'Message',
  email: 'Message',
  phone: 'Iphone',
  star: 'Star',
  validCode: 'CircleCheck',
  excel: 'Grid',
  upload: 'Upload',
  download: 'Download',
  search: 'Search',
  add: 'Plus',
  remove: 'Delete',
  refresh: 'Refresh',
  home: 'HomeFilled',
  dashboard: 'Odometer'
}

const ICON_NAMES = new Set(Object.keys(ElementPlusIcons))

export function resolveIcon(name) {
  if (!name || name === '#') return ''

  const alias = ALIAS[name]
  if (alias && ICON_NAMES.has(alias)) return alias

  // 直接写 Element Plus 组件名也能用（如 Setting / User）
  if (ICON_NAMES.has(name)) return name

  return ''
}

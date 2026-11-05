/**
 * 所有页面组件，用于把后端菜单里的 component 字符串
 * （如 "system/user/index"）映射成真正的异步组件。
 */
const viewModules = import.meta.glob('../views/**/*.vue')

// 用动态 import 而不是静态 import，避免 placeholder 同时被 glob 动态导入
// 与静态导入而触发 Vite 的 "dynamically imported but also statically imported" 警告
const Placeholder = () => import('@/views/error/placeholder.vue')

function resolveView(component) {
  if (!component) return Placeholder

  const clean = String(component)
    .replace(/^\/+/, '')
    .replace(/\.vue$/, '')

  const candidates = [`../views/${clean}.vue`, `../views/${clean}/index.vue`]
  for (const key of candidates) {
    if (viewModules[key]) return viewModules[key]
  }

  // 后端配了组件但前端还没实现时，落到占位页而不是白屏
  return Placeholder
}

function joinPath(parentPath, childPath) {
  if (!childPath) return parentPath || '/'
  if (/^https?:\/\//i.test(childPath)) return childPath

  const segments = `${parentPath === '/' ? '' : parentPath}/${childPath}`
    .split('/')
    .filter(Boolean)

  return `/${segments.join('/')}`
}

function byOrder(a, b) {
  return Number(a.orderNum || 0) - Number(b.orderNum || 0)
}

/**
 * 后端 Menu 树 → 路由 + 侧边栏菜单
 *
 * 关键点：目录（M）只作为侧边栏分组，不生成占位路由；
 * 真正注册的是叶子页面（C），路径为绝对路径，统一挂在 Layout 的 Root 路由下。
 * 这样不会有嵌套 router-view 的问题，URL 也和 menu.path 完全一致。
 */
export function buildRoutes(menuTree = [], parentPath = '/') {
  const routes = []
  const menus = []

  ;[...menuTree].sort(byOrder).forEach((menu) => {
    // F 是按钮权限，不参与路由
    if (menu.menuType === 'F') return

    const children = Array.isArray(menu.children)
      ? menu.children.filter((child) => child.menuType !== 'F')
      : []

    const fullPath = joinPath(parentPath, menu.path)
    const hidden = String(menu.visible ?? '0') === '1'
    const title = menu.menuName || '未命名菜单'
    const icon = menu.icon || ''

    if (children.length > 0) {
      const built = buildRoutes(children, fullPath)

      // 目录本身没有页面，只做分组
      menus.push({
        path: fullPath,
        title,
        icon,
        hidden,
        children: built.menus
      })

      routes.push(...built.routes)
      return
    }

    // 没有子节点：当作页面处理
    routes.push({
      path: fullPath,
      name: `menu-${menu.menuId}`,
      component: resolveView(menu.component),
      meta: {
        title,
        icon,
        perms: menu.perms || '',
        menuId: menu.menuId,
        componentPath: menu.component || '',
        hidden
      }
    })

    menus.push({ path: fullPath, title, icon, hidden, children: [] })
  })

  return { routes, menus }
}

/** 菜单树里第一个可见叶子，用于登录后的默认落地页 */
export function firstLeafPath(menus = []) {
  for (const menu of menus) {
    if (menu.hidden) continue
    if (menu.children && menu.children.length) {
      const leaf = firstLeafPath(menu.children)
      if (leaf) return leaf
    } else if (menu.path) {
      return menu.path
    }
  }
  return ''
}

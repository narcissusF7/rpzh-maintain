import { defineStore } from 'pinia'
import router from '@/router'
import { getRouters } from '@/api/menu'
import { buildRoutes, firstLeafPath } from '@/router/dynamic'
import { localMenus } from '@/router/localMenus'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    menus: [],
    loaded: false,
    // true 表示后端菜单为空，当前用的是 localMenus 本地兜底
    fallback: false,
    error: '',
    homePath: '/dashboard',
    addedNames: []
  }),
  getters: {
    /** 侧边栏最终渲染用：过滤掉隐藏项 */
    visibleMenus: (state) => filterHidden(state.menus)
  },
  actions: {
    async generateRoutes() {
      let tree = []
      let error = ''
      let fallback = false

      try {
        const data = await getRouters()
        tree = Array.isArray(data) ? data : []
      } catch (e) {
        error = e?.message || '菜单接口异常'
      }

      if (!tree.length) {
        fallback = true
        tree = localMenus
      }

      const { routes, menus } = buildRoutes(tree)

      this.addedNames = []
      routes.forEach((route) => {
        router.addRoute('Root', route)
        if (route.name) this.addedNames.push(route.name)
      })

      this.routes = routes
      this.menus = menus
      this.loaded = true
      this.fallback = fallback
      this.error = error

      const leaf = firstLeafPath(menus)
      if (leaf && !fallback) this.homePath = leaf

      return routes
    },

    reset() {
      this.addedNames.forEach((name) => {
        if (router.hasRoute(name)) router.removeRoute(name)
      })
      this.addedNames = []
      this.routes = []
      this.menus = []
      this.loaded = false
      this.fallback = false
      this.error = ''
      this.homePath = '/dashboard'
    }
  }
})

function filterHidden(menus = []) {
  const result = []

  menus.forEach((menu) => {
    if (menu.hidden) return

    const children = filterHidden(menu.children || [])
    // 目录下面的子菜单全被隐藏了，这个目录也没必要显示；
    // 本来就没有子节点的，是真正的页面菜单，必须保留。
    const isDirectory = (menu.children || []).length > 0
    if (isDirectory && children.length === 0) return

    result.push({ ...menu, children })
  })

  return result
}

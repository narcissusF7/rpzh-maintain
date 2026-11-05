import router from '@/router'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'

const APP_TITLE = '养护管理系统'

router.beforeEach(async (to) => {
  document.title = to.meta?.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE

  const userStore = useUserStore()

  // 未登录：只有 public 页面放行
  if (!userStore.token) {
    if (to.meta?.public) return true
    return {
      path: '/login',
      query: to.fullPath && to.fullPath !== '/' ? { redirect: to.fullPath } : {}
    }
  }

  // 已登录还去登录页，直接回首页
  if (to.path === '/login') {
    return { path: '/' }
  }

  const permissionStore = usePermissionStore()

  // 首次进入：拉菜单、注册动态路由，然后重新解析当前地址
  if (!permissionStore.loaded) {
    await permissionStore.generateRoutes()
    // 注意：必须拆成 path + query + hash，直接塞 fullPath 会把 ?taskNum=xx 丢掉，
    // 导致「预警详情 → 查看关联任务」等带参跳转的预筛选失效。
    return { path: to.path, query: to.query, hash: to.hash, replace: true }
  }

  return true
})

/**
 * 兜底释放滚动锁：
 * 弹层在渲染异常、或未关闭就跳转时，body 上的 `el-popup-parent--hidden` 可能残留，
 * 表现为「整页点不动 / 无法滚动」。这里只做「释放」，绝不删除任何遮罩节点 ——
 * 因为新页面的 el-dialog 是先挂 overlay、后渲染内容的，删节点会把正常弹窗打断。
 */
function releaseStaleScrollLock() {
  const hasVisibleOverlay = Array.from(document.querySelectorAll('.el-overlay'))
    .some((el) => getComputedStyle(el).display !== 'none')
  if (hasVisibleOverlay) return
  document.body.classList.remove('el-popup-parent--hidden', 'el-message-box-parent--hidden')
  document.body.style.removeProperty('overflow')
  document.body.style.removeProperty('padding-right')
}

router.afterEach(() => {
  // 关闭 loading / 上报埋点；并释放上一页可能残留的滚动锁
  setTimeout(releaseStaleScrollLock, 120)
})

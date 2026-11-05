import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth, DEMO_TOKEN } from '@/utils/auth'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 20000
})

let redirecting = false
let lastMessage = ''
let lastMessageAt = 0

/** 同一句报错 3 秒内只弹一次，避免页面并发请求时刷屏 */
function toast(message) {
  const now = Date.now()
  if (message === lastMessage && now - lastMessageAt < 3000) return
  lastMessage = message
  lastMessageAt = now
  ElMessage.error(message)
}

/**
 * function 模块部分接口在空数据 / 权限不足时仍会返回业务失败，
 * 页面会自己在表格上方展示降级说明，不需要再弹全局错误框刷屏。
 * 写操作可显式传 silent: false 恢复弹窗。
 */
function shouldSilent(config) {
  if (config?.silent === false) return false
  if (config?.silent === true) return true
  return /^\/(maintain|dataReport|issue|node)\b/.test(config?.url || '')
}

function toLogin(message) {
  if (redirecting) return
  redirecting = true
  clearAuth()
  ElMessage.error(message || '登录状态已失效，请重新登录')
  // 用完整跳转，避免在拦截器里引用 router 造成循环依赖
  const redirect = encodeURIComponent(window.location.pathname + window.location.search)
  window.location.href = `/login?redirect=${redirect}`
}

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    // 演示令牌不是合法 JWT，带上反而会被资源服务器判为无效令牌，直接不带
    if (token && token !== DEMO_TOKEN) {
      // 后端返回的是 "bearer xxx"，这里做一次兼容：没有前缀就补上
      config.headers.Authorization = /^bearer\s/i.test(token) ? token : `bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    // 验证码等二进制流直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const data = response.data

    // 统一响应体 Result{flag, code, message, data}
    if (data && typeof data === 'object' && Object.prototype.hasOwnProperty.call(data, 'flag')) {
      if (data.flag) {
        return data.data
      }
      const message = data.message || '操作失败'
      if (!shouldSilent(response.config)) toast(message)
      return Promise.reject(new Error(message))
    }

    // 少数接口直接返回字符串（如 /username/getUserRole）
    return data
  },
  (error) => {
    const status = error.response?.status
    const serverMessage = error.response?.data?.message

    if (status === 401 || status === 403) {
      toLogin(status === 403 ? '没有访问权限或登录已过期' : '登录状态已失效，请重新登录')
      return Promise.reject(error)
    }

    if (!shouldSilent(error.config)) {
      if (error.code === 'ECONNABORTED') {
        toast('请求超时，请检查后端服务是否已启动')
      } else if (!error.response) {
        toast('网络异常，无法连接后端（检查网关 7001 是否启动）')
      } else {
        toast(serverMessage || `请求失败（${status}）`)
      }
    }

    return Promise.reject(error)
  }
)

export default service

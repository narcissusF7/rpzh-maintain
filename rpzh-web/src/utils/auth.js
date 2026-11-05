const TOKEN_KEY = 'rpzh_token'
const USER_KEY = 'rpzh_user'

/**
 * 演示令牌：不是合法 JWT，请求时不会带到 Authorization 头里。
 * 后端 ResourceServerConfig 把 antMatchers 配成了 permitAll，等于全部路径放行，
 * 所以不带令牌也能读接口；写接口因为拿不到登录用户（TokenDecode）可能报错。
 */
export const DEMO_TOKEN = 'demo-local-token'

/**
 * 后端 /user/login 返回的 jwt 已经带了 "bearer " 前缀，
 * 这里原样存储，请求时直接塞进 Authorization 头。
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getCachedUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function setCachedUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user || {}))
}

export function removeCachedUser() {
  localStorage.removeItem(USER_KEY)
}

export function clearAuth() {
  removeToken()
  removeCachedUser()
}

function decodeBase64Url(str) {
  const b64 = str.replace(/-/g, '+').replace(/_/g, '/')
  const padded = b64 + '='.repeat((4 - (b64.length % 4)) % 4)
  const binary = atob(padded)
  try {
    const bytes = Uint8Array.from(binary, (c) => c.charCodeAt(0))
    return new TextDecoder('utf-8').decode(bytes)
  } catch (e) {
    return binary
  }
}

/** 从登录 JWT 里解出权限串（登录时固化，前端只用于界面显隐，不替代后端鉴权） */
export function getAuthorities() {
  const raw = getToken().replace(/^bearer\s+/i, '').trim()
  const parts = raw.split('.')
  if (parts.length < 2) return []
  try {
    const payload = JSON.parse(decodeBase64Url(parts[1]))
    return Array.isArray(payload.authorities) ? payload.authorities : []
  } catch (e) {
    return []
  }
}

/** 管理员判定：演示账号 / admin / 拥有系统管理类权限 */
export function isAdmin() {
  const user = getCachedUser() || {}
  if (user.demo || user.username === 'admin') return true
  const auth = getAuthorities()
  return auth.includes('system:menu:list') || auth.includes('system:user:add') || auth.includes('system:role:list')
}

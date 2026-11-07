import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'
import { getProfile } from '@/api/user'
import { getToken, setToken, clearAuth, getCachedUser, setCachedUser, DEMO_TOKEN } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getCachedUser() || {},
    demoMode: getToken() === DEMO_TOKEN
  }),
  getters: {
    displayName: (state) => state.userInfo.nickname || state.userInfo.username || '未登录',
    isDemo: (state) => state.demoMode
  },
  actions: {
    /**
     * 真实登录：/user/login 返回 { username, jwt: "bearer xxx" }
     */
    async login(form) {
      const data = await loginApi(form)
      const jwt = data?.jwt
      if (!jwt) {
        throw new Error('登录响应中没有 jwt，请检查账号状态')
      }
      this.token = jwt
      this.demoMode = false
      setToken(jwt)

      const user = { username: data.username || form.username }
      this.userInfo = user
      setCachedUser(user)

      // 拉一次个人资料补全昵称，失败不影响登录
      this.loadProfile()
      return data
    },

    /**
     * 演示登录：只写本地假令牌，不发请求。
     * 用于本机 tb_user 为空 / OAuth 客户端未配置的场景。
     */
    demoLogin(username = 'admin') {
      this.token = DEMO_TOKEN
      this.demoMode = true
      setToken(DEMO_TOKEN)
      const user = { username, nickname: '演示账号', demo: true }
      this.userInfo = user
      setCachedUser(user)
    },

    async loadProfile() {
      if (this.demoMode || !this.token) return null
      try {
        const profile = await getProfile()
        if (profile) {
          this.userInfo = { ...this.userInfo, ...profile }
          setCachedUser(this.userInfo)
        }
        return profile
      } catch (e) {
        // 个人资料接口失败（例如后端未放开）不阻断登录
        return null
      }
    },

    logout() {
      this.token = ''
      this.userInfo = {}
      this.demoMode = false
      clearAuth()
    },

    /**
     * 中台单点登录回跳：后端已签发本地 JWT，这里直接写入并进入系统。
     * token 形如 "bearer xxx"。
     */
    ssoLogin(token) {
      if (!token) {
        throw new Error('SSO 回跳缺少 token')
      }
      this.token = token
      this.demoMode = false
      setToken(token)
      const user = { username: '' }
      this.userInfo = user
      setCachedUser(user)
      this.loadProfile()
      return token
    }
  }
})

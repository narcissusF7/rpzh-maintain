import request from './request'
import { toForm } from '@/utils/form'

/**
 * 登录：OauthController#login
 * 参数走表单，返回 Result.data = { username, jwt: "bearer xxx" }
 * 注意：本地未配置 OAuth 客户端时该接口不可用，登录页会提示。
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data: toForm({
      username: data.username,
      password: data.password,
      uuidCode: data.uuidCode,
      verificationCode: data.verificationCode
    })
  })
}

/**
 * 图形验证码：返回 jpeg 二进制。
 * 后端需要前端传一个 uuidCode 作为 redis key。
 */
export function getCaptcha(uuidCode) {
  return request({
    url: '/user/verification',
    method: 'get',
    params: { uuidCode },
    responseType: 'blob'
  })
}

/**
 * 单点登录入口（不同系统跳转用），无验证码。
 */
export function maintainLogin(data) {
  return request({
    url: '/user/maintainLogin',
    method: 'post',
    data: toForm(data)
  })
}

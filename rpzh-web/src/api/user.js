import request from './request'
import { toForm } from '@/utils/form'

/** 分页查询用户：UserController#findAllUserList（POST 表单，page 从 1 开始） */
export function listUser(query) {
  return request({
    url: '/username/findAllUserList',
    method: 'post',
    data: toForm({
      page: query.page,
      size: query.size,
      nickName: query.nickName,
      phonenumber: query.phonenumber,
      status: query.status,
      deptId: query.deptId,
      startTime: query.startTime,
      endTime: query.endTime
    })
  })
}

/** 新增用户：必传 userName / nickName / password / roleIds */
export function addUser(data) {
  return request({
    url: '/username/addUser',
    method: 'post',
    data: toForm(data)
  })
}

/** 修改用户：roleIds 不能为空（后端有校验） */
export function updateUser(data) {
  return request({
    url: '/username/editUser',
    method: 'post',
    data: toForm(data)
  })
}

/** 批量删除用户：后端读 userIds 数组，实际是逻辑删除 del_flag */
export function deleteUser(userIds) {
  return request({
    url: '/username/deleteUser',
    method: 'post',
    data: toForm({ userIds })
  })
}

/** 启用 / 停用用户：userId + status */
export function changeUserStatus(userId, status) {
  return request({
    url: '/username/changeUserStatus',
    method: 'post',
    data: toForm({ userId, status })
  })
}

/** 管理员重置任意用户密码 */
export function resetUserPassword(username, newpassWord) {
  return request({
    url: '/username/adminUpdatePassWord',
    method: 'get',
    params: { username, newpassWord }
  })
}

/** 修改自己的密码 */
export function updatePassword(username, newpassWord, notarizePassword) {
  return request({
    url: '/username/editPassWord',
    method: 'get',
    params: { username, newpassWord, notarizePassword }
  })
}

/** 修改用户时回显已选角色 id 列表 */
export function getUserRoleIds(userId) {
  return request({
    url: '/username/findUserByUpdate',
    method: 'get',
    params: { userId }
  })
}

/** 个人信息 */
export function getProfile() {
  return request({
    url: '/username/findOneUserMessage',
    method: 'get'
  })
}

/** 个人中心修改资料 */
export function updateProfile(data) {
  return request({
    url: '/username/changeUserMessage',
    method: 'post',
    data: toForm(data)
  })
}

import request from './request'
import { toForm } from '@/utils/form'

/** 分页查询角色：RoleController#findAllRole */
export function listRole(query) {
  return request({
    url: '/role/findAllRole',
    method: 'post',
    data: toForm({
      page: query.page,
      size: query.size,
      roleName: query.roleName,
      roleKey: query.roleKey,
      status: query.status
    })
  })
}

/** 新增角色：roleName / roleKey / roleSort / menuIds 均必填 */
export function addRole(data) {
  return request({
    url: '/role/addRole',
    method: 'post',
    data: toForm(data)
  })
}

/** 修改角色：roleId 为 1 时后端只允许改菜单权限 */
export function updateRole(data) {
  return request({
    url: '/role/editRole',
    method: 'post',
    data: toForm(data)
  })
}

/** 批量删除角色：GET + 重复 roleId 参数 */
export function deleteRoles(roleIds) {
  return request({
    url: '/role/delectRole',
    method: 'get',
    params: toForm({ roleId: roleIds })
  })
}

/** 启用 / 停用角色（超级管理员不允许操作，后端会抛异常） */
export function changeRoleStatus(roleId, status) {
  return request({
    url: '/role/changeStatus',
    method: 'post',
    data: toForm({ roleId, status })
  })
}

/** 修改数据权限范围：1 全部 2 自定义 3 本部门 4 本部门及以下 */
export function updateDataScope(roleId, dataScope) {
  return request({
    url: '/role/dataScope',
    method: 'post',
    data: toForm({ roleId, dataScope })
  })
}

/** 角色已授权的菜单 id 列表 */
export function getRoleMenuIds(roleId) {
  return request({
    url: '/menu/getOneRouters',
    method: 'get',
    params: { roleId }
  })
}

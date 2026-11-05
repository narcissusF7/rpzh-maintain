import request from './request'
import { toForm } from '@/utils/form'

/** 菜单树（含按钮）：MenuController#list */
export function listMenu() {
  return request({
    url: '/menu/list',
    method: 'get'
  })
}

/** 新增菜单：path / parentId / menuName 必填 */
export function addMenu(data) {
  return request({
    url: '/menu/addMenu',
    method: 'post',
    data: toForm(data)
  })
}

/**
 * 修改菜单：后端强制校验 parentName 非空，
 * 所以提交时必须把父菜单名称一起带上（父级为根时传「主类目」）。
 */
export function updateMenu(data) {
  return request({
    url: '/menu/editMenu',
    method: 'post',
    data: toForm(data)
  })
}

/** 删除菜单：存在子菜单或已分配角色时会被拒绝 */
export function deleteMenu(menuId) {
  return request({
    url: '/menu/deleteMenu',
    method: 'get',
    params: { menuId }
  })
}

/** 修改菜单状态 */
export function changeMenuStatus(menuId, status) {
  return request({
    url: '/menu/changeMenuStatus',
    method: 'post',
    data: toForm({ menuId, status })
  })
}

/** 登录后的动态路由菜单（后端按角色过滤） */
export function getRouters() {
  return request({
    url: '/menu/getRouters',
    method: 'get'
  })
}

/** 新增菜单时下拉用的完整菜单树 */
export function findAllMenu() {
  return request({
    url: '/menu/findAllMenu',
    method: 'post'
  })
}

/** 按名称搜索菜单（扁平结果） */
export function searchMenu(params) {
  return request({
    url: '/menu/searchMenuList',
    method: 'get',
    params: toForm(params)
  })
}

import request from './request'
import { toForm } from '@/utils/form'

/** 部门树：DeptController#findAllDept */
export function listDept(query = {}) {
  return request({
    url: '/dept/findAllDept',
    method: 'post',
    data: toForm({
      deptName: query.deptName,
      status: query.status
    })
  })
}

/** 新增部门：parentId / deptName / orderNum 必填 */
export function addDept(data) {
  return request({
    url: '/dept/addDept',
    method: 'post',
    data: toForm(data)
  })
}

/** 修改部门 */
export function updateDept(data) {
  return request({
    url: '/dept/editDept',
    method: 'post',
    data: toForm(data)
  })
}

/** 删除部门：存在下级部门或部门下有用户时会被拒绝 */
export function deleteDept(deptId) {
  return request({
    url: '/dept/deleteDept',
    method: 'get',
    params: { deptId }
  })
}

/** 修改部门状态：接口名后端拼写为 changeDeptStutas，保持一致 */
export function changeDeptStatus(deptId, status) {
  return request({
    url: '/dept/changeDeptStutas',
    method: 'post',
    data: toForm({ deptId, status })
  })
}

/** 搜索部门 */
export function searchDept(params) {
  return request({
    url: '/dept/searchDept',
    method: 'post',
    data: toForm(params)
  })
}

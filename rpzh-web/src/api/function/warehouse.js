import request from '@/api/request'
import { toForm } from '@/utils/form'

/**
 * 仓库 / 楼 / 层 / 区 / 垛
 * 后端 WarehouseController，路径 /maintain/warehouse
 * 注意：接口几乎都是表单参数（@ApiIgnore Dto），统一用 toForm。
 */
const BASE = '/maintain/warehouse'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })

export function warehouseTree(params = {}) {
  return post('/warehouseTree', params)
}

export function warehouseInfo(params = {}) {
  return post('/warehouseInfo', params)
}

export function getStackPage(params = {}) {
  return post('/getStackPage', params)
}

/** 所有库区（用于垛位下拉按库区逐个加载，避免写死 201~205） */
export function getAllWareArea() {
  return post('/getAllWareArea')
}

export function addWarehouse(params) { return post('/addWarehouse', params) }
export function updateWarehouse(params) { return post('/updateWarehouse', params) }
export function addBuilding(params) { return post('/addBuilding', params) }
export function updateBuilding(params) { return post('/updateBuilding', params) }
export function addFloor(params) { return post('/addFloor', params) }
export function updateFloor(params) { return post('/updateFloor', params) }
export function addWarearea(params) { return post('/addWarearea', params) }
export function updateWarearea(params) { return post('/updateWarearea', params) }
export function addStack(params) { return post('/addStack', params) }
export function updateStack(params) { return post('/updateStack', params) }
export function delWarehouseNode(params) { return post('/del', params) }

export function locating() {
  return request({ url: BASE + '/locating', method: 'get' })
}

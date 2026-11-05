import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 节点（传感器）管理：NodeController，/maintain/node */
const BASE = '/maintain/node'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })
const get = (url, params = {}) =>
  request({ url: BASE + url, method: 'get', params })

export function findNode(params = {}) { return post('/findNode', params) }
export function findVagueNode(params = {}) { return post('/findVagueNode', params) }
export function findNoEnterStack(params = {}) { return post('/findNoEnterStack', params) }
export function findNodeType(params = {}) { return post('/findNodeType', params) }
export function findNodeByStackId(stackId) { return get('/findNodeByStackId', { stackId }) }
export function findNodeReplaceLog(params = {}) { return get('/findNodeReplaceLog', params) }
export function findOperationHistory(params = {}) { return get('/findOperationHistory', params) }

export function addNode(params) { return post('/addNode', params) }
export function updateNode(params) { return post('/updateNode', params) }
/** 后端是 @DeleteMapping("/deleteNode") + String[] nodeIds，按重复 key 走 query */
export function deleteNode(nodeIds) {
  return request({
    url: BASE + '/deleteNode',
    method: 'delete',
    params: toForm({ nodeIds })
  })
}
// 入垛：后端 enterStack(String data) 需要 data 是「JSON 数组字符串」表单字段，
// 不能用数组直接传（toForm 会把数组变成 [object Object]）。
export function enterStack(list) { return post('/enterStack', { data: JSON.stringify(list) }) }
export function unbindStack(params) { return post('/unbindStack', params) }
export function replaceNodeId(params) { return post('/replaceNode', params) }
export function flipStack(params) { return post('/flipStack', params) }

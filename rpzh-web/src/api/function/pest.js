import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 虫害监测：PestMonitoringController，/maintain/pestMonitoring */
const BASE = '/maintain/pestMonitoring'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })
const get = (url, params = {}) =>
  request({ url: BASE + url, method: 'get', params })

export function findPestBoard(params = {}) { return post('/findPestBoard', params) }
export function findPestDataHistory(params = {}) { return post('/findPestDataHistory', params) }
export function findPestDataHistoryByOne(params = {}) { return get('/findPestDataHistoryByOne', params) }
export function findPestRule() { return get('/findPestRule') }
export function findPestBoardByRfid(rfid) { return get('/findPestBoardByRfid', { rfid }) }
export function findPestWarningValue(pestBoardId) { return get('/findPestWarningValue', { pestBoardId }) }

export function addPestBoard(params) { return post('/addPestBoard', params) }
export function deletePestBoard(ids) { return post('/deletePestBoard', { ids }) }
export function updatePestRule(params) { return post('/updatePestRule', params) }

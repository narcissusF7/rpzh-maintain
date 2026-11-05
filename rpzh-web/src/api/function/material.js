import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 物料管理：MaterialController，/maintain/material */
const BASE = '/maintain/material'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })

export function findMaterialInfo(stackId) { return post('/findMaterialInfo', { stackId }) }
export function findAlarmValueSetting(params = {}) { return post('/findAlarmValueSetting', params) }
export function findAllMaterialCodes() { return post('/findAllMaterialCodes') }

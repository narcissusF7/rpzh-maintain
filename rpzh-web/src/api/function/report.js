import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 数据报表：DataReportController，/dataReport */
const BASE = '/dataReport'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })

export function findSummaryReport(params = {}) { return post('/findSummaryReport', params) }
export function findSummaryReportDetails(params = {}) { return post('/findSummaryReportDetails', params) }
export function findAllStackData(params = {}) { return post('/stackComparison', params) }
export function findDeviceParticularsInfo(params = {}) { return post('/findDeviceParticularsInfo', params) }
export function findStackComparisonData(params = {}) { return post('/findStackComparisonData', params) }
export function findStackAndWarehouseInfo(params = {}) { return post('/findStackAndWarehouseInfo', params) }
export function findStackAndCoreInfo(params = {}) { return post('/findStackAndCoreInfo', params) }
export function findStackAndCoreInfoList(params = {}) { return post('/findStackAndCoreInfoList', params) }
export function findMaintainEarlyTimes(params = {}) { return post('/findMaintainEarlyTimes', params) }

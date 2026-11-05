import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 监控中心：MonitoringCenterController，/maintain/monitoringCenter */
const BASE = '/maintain/monitoringCenter'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })
const get = (url, params = {}) =>
  request({ url: BASE + url, method: 'get', params })

export function findMonitoringData(params = {}) { return post('/findMonitoringData', params) }
export function findTroubleNode() { return get('/findTroubleNode') }
export function findWarningThreshold() { return get('/findWarningThreshold') }
/** 当前生效的设备异常判定阈值（yml + tb_general_setting 覆盖后的值） */
export function findNodeAbnormalConfig() { return get('/findNodeAbnormalConfig') }
export function findAlarmValue(params = {}) { return post('/findAlarmValue', params) }
export function findWeatherData(params = {}) { return get('/findWeatherData', params) }
export function findWeatherDataByDay(params = {}) { return get('/findWeatherDataByDay', params) }
export function findNowWeather() { return get('/findNowWeather') }
export function findNodeHistory(params = {}) { return get('/findNodeHistory', params) }
export function findNodeHistoryGraph(params = {}) { return get('/findNodeHistoryGraph', params) }
export function findTaskFromMonitored(params = {}) { return post('/findTaskFromMonitored', params) }

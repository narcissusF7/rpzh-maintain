import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 预警中心：EarlyWarningController + HistoryWarningController */
const CURRENT = '/maintain/alarmWarning'
const HISTORY = '/maintain/historyWarning'

const post = (base) => (url, params = {}) =>
  request({ url: base + url, method: 'post', data: toForm(params) })
const cur = post(CURRENT)
const his = post(HISTORY)

export function findEarlyWarningInfo(params = {}) { return cur('/findEarlyWarningInfo', params) }
/** 当前预警逐条列表（含 taskId / nodeId / siteRoute，供「预警→对应任务」跳转） */
export function findCurrentWarningList(params = {}) { return cur('/findCurrentWarningList', params) }
export function findEarlyWarningByWarningType(params = {}) { return cur('/findEarlyWarningByWarningType', params) }
export function findEarlyWarningBySiteId(params = {}) { return cur('/findEarlyWarningBySiteId', params) }
export function findNodeData(params = {}) { return cur('/findNodeData', params) }
export function getRepairData(params = {}) { return cur('/getRepairData', params) }
export function misreport(id) { return cur('/misreport', { id }) }
export function warningSend(params = {}) { return cur('/warningSend', params) }

export function findHistoryWarning(params = {}) { return his('/findHistoryWarning', params) }
export function findWarningCount(params = {}) { return his('/findWarningCount', params) }

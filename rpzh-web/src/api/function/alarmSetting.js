import request from '@/api/request'
import { toForm } from '@/utils/form'

/**
 * 阈值设置：AlarmSettingTemplateController + WareAreaAlarmSettingController
 * 阈值模板 / 预警颜色 / 库区报警设置
 */
const TEMPLATE = '/maintain/alarmSettingTemplate'
const AREA = '/maintain/wareAreaAlarmSetting'

const post = (base) => (url, params = {}) =>
  request({ url: base + url, method: 'post', data: toForm(params) })
const tpl = post(TEMPLATE)
const area = post(AREA)

export function findAlarmSettingTemplateTable(params = {}) { return tpl('/findAlarmSettingTemplateTable', params) }
export function findColorAndAlertUpgradeDay() { return tpl('/findColorAndAlertUpgradeDay') }
export function updateOrInsertColorAndAlertUpgradeDay(params) { return tpl('/updateOrInsertColorAndAlertUpgradeDay', params) }
export function findAlarmValueSetting(params = {}) { return tpl('/findAlarmValueSetting', params) }
export function insertAlarmSettingTemplate(params) { return tpl('/insertAlarmSettingTemplate', params) }
export function updateAlarmSettingTemplate(params) { return tpl('/updateAlarmSettingTemplate', params) }
export function deleteAlarmValueSetting(ids) { return tpl('/deleteAlarmSettingTemplate', { ids }) }
export function findLogData(params = {}) { return tpl('/findLogData', params) }

export function findWareAreaAlarmSettingInfo(params = {}) { return area('/findWareAreaAlarmSettingInfo', params) }
export function findWareAreaAlarmSettingById(params = {}) { return area('/findWareAreaAlarmSettingById', params) }
export function insertWareAreaAlarmSetting(params) { return area('/insertWareAreaAlarmSetting', params) }
export function updateWareAreaAlarmSetting(params) { return area('/updateWareAreaAlarmSetting', params) }
export function deleteWareAreaAlarmSetting(ids) { return area('/deleteWareAreaAlarmSetting', { ids }) }

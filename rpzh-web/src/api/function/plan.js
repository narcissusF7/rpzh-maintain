import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 养护计划：MaintainPlanController，/maintain/maintainPlan */
const BASE = '/maintain/maintainPlan'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })

export function findMaintainPlanInfo(params = {}) { return post('/findMaintainPlanInfo', params) }
/** 已有未完成养护计划（is_end='0'）的垛位 id 列表，用于新建时筛掉已占用垛位 */
export function findOccupiedStackIds() { return post('/findOccupiedStackIds') }
export function beforeUpdateMaintainPlan(id) { return post('/beforeUpdateMaintainPlan', { id }) }
export function findMaintainPlanById(id) { return post('/findMaintainPlanById', { id }) }
export function findMaintainPlanDetails(params = {}) { return post('/findMaintainPlanDetails', params) }
export function findEndMaintainPlanDetails(id) { return post('/findEndMaintainPlanDetails', { id }) }
export function findExpireCollectTable(params = {}) { return post('/findExpireCollectTable', params) }
export function findMaintainMonthEndReport(params = {}) { return post('/findMaintainMonthEndReport', params) }
export function findMaintainHistoryCollect(params = {}) { return post('/findMaintainHistoryCollect', params) }
export function findMaintainCountReport(params = {}) { return post('/findMaintainCountReport', params) }
export function getPhases(maintenceStyle) { return post('/getPhases', { maintenceStyle }) }
export function findAlarmSettingTemplate(params = {}) { return post('/findAlarmSettingTemplate', params) }
export function findAlarmSettingTemplateList() { return post('/findAlarmSettingTemplateList') }
export function findCountByName(earlyWarningRuleName) { return post('/findCountByName', { earlyWarningRuleName }) }

export function insertMaintainPlan(params) { return post('/insert', params) }
export function updateMaintainPlan(params) { return post('/updateMaintainPlan', params) }
export function endMaintainPlan(params) { return post('/endMaintainPlan', params) }
export function deleteMaintainPlanInfo(ids) { return post('/deleteMaintainPlanInfo', { ids }) }
/** 移垛：把养护计划及其下设备整体迁到目标垛位（nodeDtoList 为 JSON 字符串数组） */
export function moveStack(params) { return post('/moveStack', params) }
export function changeMaintainPlanStage(params) { return post('/changeMaintainPlanStage', params) }
/** 养护全链路统计：阶段转换 / 预警次数 / 虫害数量 / 养护结果 */
export function findMaintainPlanStatistics(id) { return post('/findMaintainPlanStatistics', { id }) }

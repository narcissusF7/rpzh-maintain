import request from '@/api/request'
import { toForm } from '@/utils/form'

/** 任务管理：TaskManagementController，/maintain/taskmanager */
const BASE = '/maintain/taskmanager'

const post = (url, params = {}) =>
  request({ url: BASE + url, method: 'post', data: toForm(params) })

export function findTask(params = {}) { return post('/findTask', params) }
export function findTaskDetail(id) { return post('/findTaskDetail', { id }) }
export function findTaskDetailBySiteId(params = {}) { return post('/findTaskDetailBySiteId', params) }
export function findTaskByUser(params = {}) { return post('/findTaskByUser', params) }
export function findTeskByMyCreate(params = {}) { return post('/findTeskByMyCreate', params) }
export function findAllNodeByStack(siteRoute) { return post('/findAllNodeByStack', { siteRoute }) }
export function findPicUrlByTaskId(params = {}) { return post('/findPicUrlByTaskId', params) }

/** 按任务 id 查询已上传的现场照片（tb_task_pic） */
export function findTaskPics(id) { return findPicUrlByTaskId({ id }) }

/** 上传现场照片到 FastDFS（需任务 id）；后端落库 tb_task_pic，返回 { group, fileName } */
export function uploadTaskPic(file, id) {
  const fd = new FormData()
  fd.append('file', file)
  fd.append('id', id)
  return request({ url: '/maintain/file/upload', method: 'post', data: fd })
}

/** 删除现场照片：FastDFS + tb_task_pic */
export function deleteTaskPic(groupName, fileName) {
  return post('/maintain/file/delete', { groupName, fileName })
}

export function taskScheduleCount(params = {}) { return post('/taskScheduleCount', params) }
export function taskCountTrend(params = {}) { return post('/taskCountTrend', params) }
export function taskCountByType(params = {}) { return post('/taskCountByType', params) }
export function taskTypePercent(params = {}) { return post('/taskTypePercent', params) }
export function taskAvlFinishTime(params = {}) { return post('/taskAvlFinishTime', params) }

export function addTask(params) { return post('/addTask', params) }
export function delTask(ids) { return post('/delTask', { ids }) }
export function taskDispose(params) { return post('/taskDispose', params) }
export function taskEvaluate(params) { return post('/taskEvaluate', params) }

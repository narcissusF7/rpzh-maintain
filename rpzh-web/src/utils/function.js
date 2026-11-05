/**
 * function（养护业务）模块字典与接口降级工具。
 *
 * rpzh-function 的 MySQL mapper 已完成 Oracle → MySQL 适配
 * （LIMIT / STR_TO_DATE / DATE_FORMAT / GROUP_CONCAT / MAX(id)+1）。
 * 剩余失败多来自 ClickHouse 历史表无数据、空配置表越界或权限不足；
 * 页面统一用 loadList() 调用，失败时返回可展示的降级原因而不是抛异常刷屏。
 */

export const NODE_TYPE = {
  '01': '温湿度节点',
  '02': '氧气节点',
  '03': '芯温节点',
  '04': '水分节点',
  '05': 'CO₂ 节点'
}

/**
 * 设备节点状态，对应 tb_node.status。
 * 0 解垛 / 1 已入垛 / 2 未使用 / 3 故障 / 4 运维中 / 5 异常
 * 「异常」= 长时间未收到已入垛设备上报的数据，或设备多次发出异常数据。
 * 新增项会自动出现在节点管理的状态筛选下拉里（后端 /findNode 已按 status 精确过滤）。
 */
export const NODE_STATUS = {
  '1': { label: '已入垛', type: 'success' },
  '0': { label: '解垛', type: 'info' },
  '2': { label: '未使用', type: 'info' },
  '3': { label: '故障', type: 'danger' },
  '4': { label: '运维中', type: 'warning' },
  '5': { label: '异常', type: 'danger' }
}

export const WARNING_GRADE = {
  '1': { label: '一级预警', type: 'warning' },
  '2': { label: '二级预警', type: 'warning' },
  '3': { label: '三级预警', type: 'danger' }
}

/**
 * 预警类型字典 —— 编码与后端 EarlyWarningController/HistoryWarningController 的
 * @ApiImplicitParam 说明保持一致（01~03 设备故障，04~05 虫数，06~11 环境，12~19 包芯）。
 * 之前只映射了 01~04 且语义不对，导致「垛内湿度(10)」等直接显示成数字。
 */
export const WARNING_TYPE = {
  '01': '设备电量',
  '02': '设备信号',
  '03': '设备丢包',
  '04': '垛内虫数',
  '05': '仓间虫数',
  '06': '垛内氧气',
  '07': '仓间氧气',
  '08': '垛内温度',
  '09': '仓间温度',
  '10': '垛内湿度',
  '11': '仓间湿度',
  '12': '包芯温度',
  '13': '包芯高于环境',
  '14': '仓间粉螟',
  '19': '包芯低于环境'
}

/** 预警确认/处置状态（对应 tb_early_warning.is_affirm） */
export const WARNING_AFFIRM = {
  '0': { label: '已处置', type: 'success' },
  '1': { label: '正在处理', type: 'warning' },
  '2': { label: '未处理', type: 'info' },
  '3': { label: '误报', type: 'danger' }
}

export const TASK_STATUS = {
  '0': { label: '待处理', type: 'warning' },
  '3': { label: '处理中', type: 'primary' },
  '1': { label: '已完成', type: 'success' }
}

export const TASK_TYPE = {
  '01': '温度异常处置',
  '02': '设备更换',
  '03': '翻垛作业',
  '04': '虫害巡检',
  '05': '养护复核'
}

export const BOARD_TYPE = {
  '01': '常规诱捕板',
  '02': '加强诱捕板'
}

export const ALERT_LEVEL = {
  '0': { label: '正常', type: 'success' },
  '1': { label: '注意', type: 'warning' },
  '2': { label: '预警', type: 'danger' }
}

export const PLAN_STATUS = {
  '0': { label: '进行中', type: 'primary' },
  '1': { label: '已结束', type: 'info' }
}

/** 字典取文案；未命中时回退成原值，避免显示空白 */
export function dictLabel(dict, value) {
  if (!dict) return value ?? '-'
  const hit = dict[String(value)]
  if (hit === undefined || hit === null) return value ?? '-'
  return typeof hit === 'object' ? hit.label : hit
}

/** 字典取 el-tag 类型 */
export function dictType(dict, value) {
  const hit = dict?.[String(value)]
  if (hit && typeof hit === 'object' && hit.type) return hit.type
  return 'info'
}

/** 把 { label, value }[] 转成 el-select options */
export function toOptions(dict) {
  return Object.entries(dict || {}).map(([value, item]) => ({
    value,
    label: typeof item === 'object' ? item.label : item
  }))
}

/** 后端返回可能是数组 / PageInfo / 单对象，统一成 { rows, total } */
export function toRows(data) {
  if (!data) return { rows: [], total: 0 }
  if (Array.isArray(data)) return { rows: data, total: data.length }
  if (typeof data === 'object') {
    if (Array.isArray(data.list)) {
      return { rows: data.list, total: Number(data.total) || data.list.length }
    }
    return { rows: [data], total: 1 }
  }
  return { rows: [], total: 0 }
}

/** 把超长的 MyBatis/SQL 异常压缩成一句人能看懂的降级原因 */
export function shortReason(message = '') {
  const text = String(message).replace(/\s+/g, ' ').trim()
  if (/rownum/i.test(text)) return '后端 mapper 仍出现 Oracle rownum 语法（应已改为 LIMIT）'
  if (/Unknown database 'MC'/i.test(text)) return '依赖外部 MC.VW_MAINTAIN_INVENTORY 兼容视图'
  if (/SEIINGTEMPLATE|DUAL|NEXTVAL|CURRVAL/i.test(text)) return '后端 mapper 仍出现 Oracle 序列写法（应已改为 MAX(id)+1）'
  if (/Unknown column 't2\.content'/i.test(text)) return '养护阶段表缺少 content 字段'
  if (/Unknown column/i.test(text)) return '后端 SQL 引用了不存在的字段'
  if (/Unknown table/i.test(text) && /clickhouse/i.test(text)) return 'ClickHouse 缺少对应的历史数据表'
  if (/Unknown table/i.test(text)) return '后端 SQL 引用了不存在的表'
  if (/不允许访问|Access denied|权限/i.test(text)) return '当前账号缺少该接口的权限'
  if (/不能为空/i.test(text)) return text.slice(0, 40)
  if (!text) return '接口返回失败但未给出原因'
  return text.slice(0, 70)
}

/**
 * 调用列表接口并降级：永不抛错。
 * @returns {{ok: boolean, message: string, rows: Array, total: number}}
 */
export async function loadList(api, params = {}) {
  try {
    const data = await api(params)
    return { ok: true, message: '', ...toRows(data) }
  } catch (e) {
    return {
      ok: false,
      message: e?.message || '接口暂不可用',
      rows: [],
      total: 0
    }
  }
}

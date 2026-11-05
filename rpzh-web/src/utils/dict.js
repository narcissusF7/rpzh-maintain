/**
 * 字段字典
 *
 * ⚠️ 后端各模块的 status 语义并不统一，这里按「实际库表默认值 + SQL 硬编码条件」对齐：
 *   - 用户：1 = 正常，0 = 停用（UserDao.findById 里写死了 WHERE status = 1）
 *   - 菜单：1 = 正常，0 = 停用（MenuDao.findAllMenuStatuTrue 里写死了 WHERE status = 1）
 *   - 角色/部门：0 = 正常，1 = 停用（tb_role / tb_dept 的 status 默认值都是 0，查询无硬编码）
 * 如果后续后端统一了语义，只改这个文件即可。
 */

/** 用户状态：1 正常 / 0 停用 */
export const USER_STATUS = [
  { label: '正常', value: '1', tag: 'success' },
  { label: '停用', value: '0', tag: 'danger' }
]

/** 角色、部门状态：0 正常 / 1 停用 */
export const NORMAL_DISABLED = [
  { label: '正常', value: '0', tag: 'success' },
  { label: '停用', value: '1', tag: 'danger' }
]

export const MENU_STATUS = [
  { label: '正常', value: '1', tag: 'success' },
  { label: '停用', value: '0', tag: 'info' }
]

export const SEX_OPTIONS = [
  { label: '男', value: '1' },
  { label: '女', value: '0' },
  { label: '未知', value: '2' }
]

export const MENU_TYPES = [
  { label: '目录', value: 'M' },
  { label: '菜单', value: 'C' },
  { label: '按钮', value: 'F' }
]

export const VISIBLE_OPTIONS = [
  { label: '显示', value: '0' },
  { label: '隐藏', value: '1' }
]

/** RoleVo.dataScope：1 全部 2 自定义 3 本部门 4 本部门及以下 */
export const DATA_SCOPE_OPTIONS = [
  { label: '全部数据权限', value: '1' },
  { label: '自定义数据权限', value: '2' },
  { label: '本部门数据权限', value: '3' },
  { label: '本部门及以下数据权限', value: '4' }
]

export function dictLabel(dict, value, fallback = '-') {
  const item = dict.find((entry) => String(entry.value) === String(value))
  return item ? item.label : fallback
}

export function dictTag(dict, value, fallback = 'info') {
  const item = dict.find((entry) => String(entry.value) === String(value))
  return item ? item.tag : fallback
}

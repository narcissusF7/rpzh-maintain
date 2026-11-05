/**
 * 后端大量接口用 @RequestParam / 实体对象接收表单参数，
 * 数组必须序列化成重复 key（roleIds=1&roleIds=2），
 * 不能是 roleIds[]=1&roleIds[]=2，否则 Spring 绑不上。
 */
export function toForm(obj = {}) {
  const params = new URLSearchParams()

  Object.entries(obj).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return

    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item === undefined || item === null || item === '') return
        params.append(key, item)
      })
      return
    }

    if (value instanceof Date) {
      params.append(key, formatDateTime(value))
      return
    }

    params.append(key, value)
  })

  return params
}

export function formatDateTime(date) {
  const d = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
    d.getMinutes()
  )}:${pad(d.getSeconds())}`
}

/** 后端 createdTime / createtime 等字段格式不统一，统一裁成 yyyy-MM-dd HH:mm:ss */
export function formatTime(value) {
  if (!value) return '-'
  if (typeof value === 'string') return value.replace('T', ' ').slice(0, 19)
  return formatDateTime(value)
}

/** 去掉空串 / null，用于提交前的表单清洗 */
export function cleanParams(obj = {}) {
  const result = {}
  Object.entries(obj).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    result[key] = value
  })
  return result
}

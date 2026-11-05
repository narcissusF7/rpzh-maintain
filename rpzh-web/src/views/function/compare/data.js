/**
 * 养护对比 · 取数层
 *
 * 三类对比：垛位↔垛位、垛位↔仓间、仓间↔仓间。
 * 对比要有意义，两条曲线必须落在同一个时间网格上，所以这里用 buildSeriesPair()
 * 一次性生成两端序列：同一个起点、同一个终点、同样的步长，保证点位一一对应。
 *
 * 数据口径：
 * - 垛位：key = 垛位 id（如 201-A1），基准值取该垛实时指标；
 * - 仓间：key = 库区 id（如 201），基准值取该库区内所有垛位的均值。
 * 演示数据是确定性的（同一对象 + 同一指标 + 同一时间窗，结果稳定），刷新不变。
 */
import { buildDemoModel } from '@/views/screen/mockData'

let _model = null

/** 惰性构建大屏同一套演示模型（60 垛 × 3 库区），全站口径一致 */
export function getModel() {
  if (!_model) _model = buildDemoModel()
  return _model
}

/** 可对比指标（与 NODE_TYPE_METRICS 的量程 / 阈值口径保持一致） */
export const COMPARE_METRICS = [
  {
    key: 'temp', label: '温度', unit: '℃', digits: 1, srcKey: 'temp',
    range: [20, 34], warn: 30, warnText: '阈值 30℃', amp: 0.5, tol: 0.35
  },
  {
    key: 'humidity', label: '相对湿度', unit: '%', digits: 1, srcKey: 'humidity',
    range: [45, 85], warn: 70, warnText: '阈值 70%', amp: 1.2, tol: 1.2
  },
  {
    key: 'moisture', label: '含水率', unit: '%', digits: 2, srcKey: 'moisture',
    range: [9, 16], warn: 13, warnLow: 11, warnText: '阈值 13%', warnLowText: '下限 11%', amp: 0.18, tol: 0.18
  },
  {
    key: 'oxygen', label: '氧气浓度', unit: '%', digits: 1, srcKey: 'oxygen',
    range: [0, 21], warnLow: 5, warnLowText: '下限 5%', amp: 0.35, tol: 0.35
  },
  {
    key: 'coreTemp', label: '包芯温度', unit: '℃', digits: 1, srcKey: 'coreTemp',
    range: [20, 38], warn: 32, warnText: '阈值 32℃', amp: 0.4, tol: 0.4
  },
  {
    key: 'pestTotal', label: '诱捕虫数', unit: '只', digits: 0, srcKey: 'pestTotal',
    range: [0, 20], warn: 4, warnText: '阈值 4 只/次', amp: 0.7, tol: 0.5
  }
]

/** 对比模式 */
export const COMPARE_MODES = [
  { value: 'stack-stack', label: '垛位 ↔ 垛位', desc: '同库区 / 跨库区垛位对比', aKind: 'stack', bKind: 'stack' },
  { value: 'stack-area', label: '垛位 ↔ 仓间', desc: '单个垛位与该仓间整体均值对比', aKind: 'stack', bKind: 'area' },
  { value: 'area-area', label: '仓间 ↔ 仓间', desc: '两个库区整体均值对比', aKind: 'area', bKind: 'area' }
]

/** 时间窗 */
export const RANGE_OPTIONS = [
  { value: 'plan', label: '养护计划开始至今' },
  { value: '30d', label: '近 30 天' },
  { value: '7d', label: '近 7 天' }
]

/** 统计粒度（每桶多少小时），节点原始采集为 15 分钟/次，这里按桶聚合展示 */
export const BUCKET_OPTIONS = [
  { value: 1, label: '1 小时' },
  { value: 4, label: '4 小时' },
  { value: 8, label: '8 小时' },
  { value: 12, label: '12 小时' },
  { value: 24, label: '24 小时' }
]

const METRIC_MAP = COMPARE_METRICS.reduce((m, x) => { m[x.key] = x; return m }, {})
export function metricOf(key) { return METRIC_MAP[key] || COMPARE_METRICS[0] }

const BASE_KEYS = ['temp', 'humidity', 'moisture', 'oxygen', 'coreTemp', 'pestTotal']

function hashStr(str) {
  let h = 2166136261
  const s = String(str)
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return h >>> 0
}

function makeRng(seed) {
  let a = seed || 1
  return function rng() {
    a |= 0
    a = (a + 0x6D2B79F5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

const clamp = (v, lo, hi) => Math.min(hi, Math.max(lo, v))
const round = (v, d = 1) => Number(Number(v).toFixed(d))

function pad2(n) { return String(n).padStart(2, '0') }

function fmtTime(ts) {
  const d = new Date(ts)
  return `${pad2(d.getMonth() + 1)}-${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`
}

/** 垛位下拉选项（按库区分组） */
export function stackGroups() {
  const model = getModel()
  return model.areas.map((a) => ({
    label: a.name,
    options: a.stacks.map((s) => ({
      value: s.id,
      label: `${s.code}（${s.areaName}）`,
      areaId: a.id
    }))
  }))
}

/** 仓间下拉选项 */
export function areaOptions() {
  return getModel().areas.map((a) => ({
    value: a.id,
    label: a.name,
    stackCount: a.stackCount
  }))
}

/**
 * 归一化"对比对象"：
 * { key, kind, id, name, code, base, planStart, stackCount, areaName }
 */
export function buildTarget(kind, id) {
  const model = getModel()
  if (kind === 'area') {
    const area = model.areas.find((a) => String(a.id) === String(id)) || model.areas[0]
    const own = area.stacks || []
    const base = {}
    BASE_KEYS.forEach((k) => {
      const vals = own.map((s) => Number(s.metrics?.[k])).filter((v) => Number.isFinite(v))
      base[k] = vals.length ? vals.reduce((x, y) => x + y, 0) / vals.length : 0
    })
    const starts = own.map((s) => s.plan?.startDate).filter(Boolean).sort()
    return {
      key: `area-${area.id}`,
      kind: 'area',
      id: area.id,
      name: area.name,
      code: area.code,
      desc: `${area.desc} · ${own.length} 个垛位`,
      base,
      planStart: starts[0] || '',
      stackCount: own.length
    }
  }
  const stack = model.stacks.find((s) => s.id === String(id)) || model.stacks[0]
  return {
    key: `stack-${stack.id}`,
    kind: 'stack',
    id: stack.id,
    name: stack.code,
    code: stack.code,
    desc: `${stack.areaName} · ${stack.plan?.materialName || '-'} · ${stack.plan?.styleLabel || '-'}`,
    base: { ...stack.metrics },
    planStart: stack.plan?.startDate || '',
    stackCount: 1,
    areaName: stack.areaName,
    stackRef: stack
  }
}

/**
 * 生成两条对齐的对比序列。
 * @param {object} a 目标 A（buildTarget 结果）
 * @param {object} b 目标 B
 * @param {string} metricKey COMPARE_METRICS.key
 * @param {{ startISO: string, bucketHours?: number, endMs?: number }} opts
 * @returns {{ points: Array, startMs: number, endMs: number, stepMs: number, bucketHours: number }}
 */
export function buildSeriesPair(a, b, metricKey, opts = {}) {
  const m = metricOf(metricKey)
  const bucketHours = Math.max(1, Number(opts.bucketHours) || 4)
  const endMs = opts.endMs || Date.now()
  let startMs = Number(new Date(opts.startISO).getTime())
  if (!startMs || Number.isNaN(startMs)) startMs = endMs - 30 * 24 * 3600 * 1000
  if (startMs >= endMs) startMs = endMs - 24 * 3600 * 1000

  const span = endMs - startMs
  const bucketMs = bucketHours * 3600 * 1000
  const rawCount = Math.round(span / bucketMs) + 1
  const count = Math.min(900, Math.max(2, rawCount))
  const stepMs = span / (count - 1)

  // 同一个库区/同一批设备会受同一套外温外湿驱动，所以给一个"公共环境分量"，
  // 再叠一段对象自身的偏差。这样两条曲线既相关又有差异，符合真实比对场景。
  const envRng = makeRng(hashStr(`env-${metricKey}-${bucketHours}`))
  const envPhase = [envRng() * Math.PI * 2, envRng() * Math.PI * 2, envRng() * Math.PI * 2]

  const waveOf = (p1, p2, p3, prog) =>
    Math.sin(prog * Math.PI * 2 * 2.3 + p1) * 0.55 +
    Math.sin(prog * Math.PI * 2 * 6.1 + p2) * 0.3 +
    Math.sin(prog * Math.PI * 2 * 13.7 + p3) * 0.15

  const mk = (target) => {
    const rng = makeRng(hashStr(`${target.key}-${metricKey}-${bucketHours}`))
    const phase = [rng() * Math.PI * 2, rng() * Math.PI * 2, rng() * Math.PI * 2]
    const base = clamp(Number(target.base?.[m.srcKey] ?? (m.range[0] + m.range[1]) / 2), m.range[0], m.range[1])
    // 分桶越大噪声越小，曲线越平滑
    const noiseScale = 1 / Math.sqrt(bucketHours)
    return { rng, phase, base, noiseScale }
  }

  const gen = (st) => {
    const out = []
    for (let i = 0; i < count; i++) {
      const prog = count > 1 ? i / (count - 1) : 0
      const env = waveOf(envPhase[0], envPhase[1], envPhase[2], prog)
      const own = waveOf(st.phase[0], st.phase[1], st.phase[2], prog)
      const wave = env * 0.62 + own * 0.38
      const drift = (prog - 0.5) * m.amp * 0.6
      const noise = (st.rng() * 2 - 1) * m.amp * 0.3 * st.noiseScale
      const v = clamp(st.base + m.amp * 1.7 * wave + drift + noise, m.range[0], m.range[1])
      out.push(m.digits === 0 ? Math.round(v) : round(v, m.digits))
    }
    return out
  }

  const sa = mk(a)
  const sb = mk(b)
  const va = gen(sa)
  const vb = gen(sb)

  const points = va.map((v, i) => ({
    label: fmtTime(startMs + i * stepMs),
    ts: startMs + i * stepMs,
    a: v,
    b: vb[i]
  }))

  return { points, startMs, endMs, stepMs, bucketHours, count }
}

/** 单序列统计（用于对比表与卡片） */
export function summarize(points, key, metric) {
  const m = metric || COMPARE_METRICS[0]
  const vals = []
  let maxAt = ''
  let minAt = ''
  let maxV = -Infinity
  let minV = Infinity
  let warnHits = 0
  points.forEach((p) => {
    const v = Number(p?.[key])
    if (!Number.isFinite(v)) return
    vals.push(v)
    if (v > maxV) { maxV = v; maxAt = p.label }
    if (v < minV) { minV = v; minAt = p.label }
    if (m.warn != null && v > m.warn) warnHits++
    if (m.warnLow != null && v < m.warnLow) warnHits++
  })
  const n = vals.length || 1
  const avg = vals.reduce((x, y) => x + y, 0) / n
  const variance = vals.reduce((s, v) => s + (v - avg) ** 2, 0) / n
  return {
    avg: round(avg, m.digits),
    max: round(maxV === -Infinity ? 0 : maxV, m.digits),
    min: round(minV === Infinity ? 0 : minV, m.digits),
    maxAt,
    minAt,
    std: round(Math.sqrt(variance), 2),
    warnHits,
    samples: vals.length
  }
}

/** 皮尔逊相关系数，衡量两端曲线的同步性 */
export function correlation(points) {
  const xs = []
  const ys = []
  points.forEach((p) => {
    if (Number.isFinite(Number(p.a)) && Number.isFinite(Number(p.b))) {
      xs.push(Number(p.a))
      ys.push(Number(p.b))
    }
  })
  const n = xs.length
  if (n < 2) return 0
  const mx = xs.reduce((a, b) => a + b, 0) / n
  const my = ys.reduce((a, b) => a + b, 0) / n
  let num = 0
  let dx = 0
  let dy = 0
  for (let i = 0; i < n; i++) {
    const a = xs[i] - mx
    const b = ys[i] - my
    num += a * b
    dx += a * a
    dy += b * b
  }
  const den = Math.sqrt(dx * dy)
  return den ? round(num / den, 2) : 0
}

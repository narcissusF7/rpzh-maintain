/**
 * 养护大屏数据源
 *
 * 1) buildDemoModel()：确定性生成一批「正常测试数据」，长期保留、不删除。
 *    3 个库区 × 20 垛 = 60 个垛位，每垛都有「当前养护计划 + 该计划下的节点 + 实时指标」。
 *    数据结构刻意对齐真实 VO 字段（stackId / materialCode / maintenceStyle / phases /
 *    nodeId / nodeType / status / caseNo / siteX~Z / warningType / warningGrade），
 *    便于随时切换成真实接口数据。
 * 2) enrichWithReal(model)：把网关里真实的养护计划 / 节点注入到前若干个垛位，
 *    失败或字段缺失时自动回退，不影响大屏渲染。
 *
 * 说明：虚拟数据只存在于前端，不写业务库，因此不会改变节点管理 / 养护计划 /
 * 监控中心等既有页面的数据，也不会影响回归脚本的计数断言。
 */

export const SCREEN_DATA_VERSION = '1.0.0'

export const AREA_DEFS = [
  { id: 201, name: '1号醇化库', code: 'FZ-01-A', desc: '常规养护区' },
  { id: 202, name: '2号醇化库', code: 'FZ-01-B', desc: '降氧养护区' },
  { id: 203, name: '3号醇化库', code: 'XM-01-A', desc: '梗丝养护区' }
]

export const STYLE_DICT = {
  '01': '常规养护',
  '02': '降氧养护',
  '03': '梗丝养护'
}

// 与后端 getPhases() 返回的中文阶段保持一致（顺序即业务预设推进顺序）
export const PHASE_SEQUENCE = ['养护阶段', '充氮杀虫阶段', '磷化铝杀虫阶段', '充氮保质阶段']

export const NODE_TYPE_DICT = {
  '01': '氧气',
  '02': '温湿度',
  '03': '包芯温度',
  '04': '虫情诱捕',
  '05': '含水率'
}

export const STATUS_DICT = {
  '1': '已入垛',
  '0': '解垛',
  '2': '未使用',
  '3': '故障',
  '4': '运维中',
  '5': '异常'
}

export const HEALTH_DICT = {
  normal: { label: '正常', color: '#3ddc97', level: 0 },
  watch: { label: '黄色', color: '#e8c55a', level: 1 },
  warn: { label: '橙色', color: '#f39c4a', level: 2 },
  danger: { label: '红色', color: '#f2665c', level: 3 }
}

const PRINCIPALS = ['林建国', '张伟', '陈晓明', '李文博', '吴海燕', '郑志强']
const MATERIALS = ['片烟', '烟梗', '梗丝', '复烤叶片', '醇化烟叶']
const WARNING_TYPES = [
  { code: '01', label: '设备电量', kind: '设备异常' },
  { code: '02', label: '信号强度', kind: '设备异常' },
  { code: '03', label: '数据丢包', kind: '设备异常' },
  { code: '04', label: '垛内甲虫', kind: '虫害' },
  { code: '05', label: '仓间甲虫', kind: '虫害' },
  { code: '06', label: '垛内氧气', kind: '环境' },
  { code: '08', label: '垛内温度', kind: '环境' },
  { code: '10', label: '垛内湿度', kind: '环境' },
  { code: '12', label: '包芯温度', kind: '环境' }
]

/** 确定性伪随机（固定 seed => 每次刷新数据一致，便于反复测试比对） */
function makeRng(seed) {
  let s = seed >>> 0
  return () => {
    s += 0x6d2b79f5
    let t = s
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

const round = (v, n = 1) => Number(v.toFixed(n))
const pick = (rng, arr) => arr[Math.floor(rng() * arr.length)]
const between = (rng, a, b) => a + rng() * (b - a)

function pad(n, len = 3) {
  return String(n).padStart(len, '0')
}

function clamp(v, min, max) {
  return Math.max(min, Math.min(max, v))
}

/** 字符串散列，用于让不同节点生成各异但确定的序列 */
function hashStr(str) {
  let h = 2166136261
  for (let i = 0; i < str.length; i++) {
    h ^= str.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return h >>> 0
}

const pad2 = (n) => String(n).padStart(2, '0')
function fmtClock(ts) {
  const d = new Date(ts)
  return `${pad2(d.getMonth() + 1)}/${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`
}

/**
 * 「什么设备显示什么数据」的唯一口径表。
 * 每个节点类型声明自己采集哪些指标、单位、量程、阈值与波动幅度；
 * 大屏的设备抽屉、垛位均值区、下方趋势图都从这里取配置，保证前后一致。
 *
 * metrics[].srcKey：从 stack.metrics 里取基准值的字段名。
 * warn / warnLow：上限 / 下限阈值（用于画虚线与提示）。
 */
export const NODE_TYPE_METRICS = {
  '01': {
    label: '氧气',
    desc: '氧气浓度传感器：安装于垛内与仓间，采集氧气浓度，用于判定降氧、充氮养护是否达标。',
    metrics: [
      {
        key: 'oxygen', srcKey: 'oxygen', label: '氧气浓度', unit: '%', color: '#7fd4f0',
        digits: 1, min: 0, max: 21, floor: 0, ceil: 21,
        warnLow: 5, warnLowText: '下限 5% 达标',
        jitter: 0.09, minRange: 1.4, avgAmp: 0.35
      }
    ]
  },
  '02': {
    label: '温湿度',
    desc: '温湿度传感器：采集垛内空气温度与相对湿度，是环境预警的主力数据源。',
    metrics: [
      {
        key: 'temp', srcKey: 'temp', label: '温度', unit: '℃', color: '#f0a25a',
        digits: 1, min: 20, max: 34, warn: 30, warnText: '阈值 30℃',
        jitter: 0.3, minRange: 1.6, avgAmp: 0.5
      },
      {
        key: 'humidity', srcKey: 'humidity', label: '相对湿度', unit: '%', color: '#4fb8f0',
        digits: 1, min: 45, max: 85, warn: 70, warnText: '阈值 70%',
        jitter: 0.8, minRange: 3, avgAmp: 1.2
      }
    ]
  },
  '03': {
    label: '包芯温度',
    desc: '包芯温度探针：插入烟垛芯部，采集垛芯温度，用于识别内部发热、霉变风险。',
    metrics: [
      {
        key: 'coreTemp', srcKey: 'coreTemp', label: '包芯温度', unit: '℃', color: '#f2885a',
        digits: 1, min: 20, max: 38, warn: 32, warnText: '阈值 32℃',
        jitter: 0.22, minRange: 1.2, avgAmp: 0.4
      }
    ]
  },
  '04': {
    label: '虫情诱捕',
    desc: '虫情诱捕板：按采集周期统计诱捕到的甲虫数量，用于虫害预警与处置闭环。',
    metrics: [
      {
        key: 'pest', srcKey: 'pestTotal', label: '诱捕虫数', unit: '只', color: '#e8c55a',
        digits: 0, min: 0, max: 20, floor: 0, warn: 4, warnText: '阈值 4 只/次',
        jitter: 0.55, minRange: 2, avgAmp: 0.7, bar: true
      }
    ]
  },
  '05': {
    label: '含水率',
    desc: '含水率传感器：采集烟叶含水率，用于跟踪养护过程中的水分散失与回潮。',
    metrics: [
      {
        key: 'moisture', srcKey: 'moisture', label: '含水率', unit: '%', color: '#3ddc97',
        digits: 2, min: 9, max: 16, warn: 13, warnLow: 11,
        warnText: '阈值 13%', warnLowText: '下限 11%',
        jitter: 0.07, minRange: 0.5, avgAmp: 0.18
      }
    ]
  }
}

/** 取某节点类型的展示配置（未知类型回退到温湿度） */
export function metricsOfType(nodeType) {
  return NODE_TYPE_METRICS[nodeType] || NODE_TYPE_METRICS['02']
}

/** 统计模型里每类设备的台数（同类型设备 = 跨库区、按节点类型归并） */
export function countNodesByType(model) {
  const map = {}
  Object.keys(NODE_TYPE_METRICS).forEach((t) => { map[t] = 0 })
  ;(model?.stacks || []).forEach((s) => (s.nodes || []).forEach((n) => {
    const t = String(n.nodeType)
    map[t] = (map[t] || 0) + 1
  }))
  return map
}

/**
 * 生成单个节点的历史遥测序列 —— 只生成「该设备类型会采集的指标」。
 *  01 氧气      → oxygen
 *  02 温湿度    → temp / humidity
 *  03 包芯温度  → coreTemp
 *  04 虫情诱捕  → pest（整数计数）
 *  05 含水率    → moisture
 * mode:
 *  - '24h'  ：最近 24 小时，固定 96 个点（24h ÷ 15min）。
 *  - 'plan' ：从养护计划开始（planStartISO）到现在；跨度长时按最多 240 点采样展示，
 *             语义上仍是「15 分钟一次」的原始采集粒度。
 * 确定性：以节点编号 + 模式为种子，刷新结果一致，便于测试比对。
 */
export function buildNodeHistory(node, planStartISO, mode = '24h') {
  const type = String(node?.nodeType || '02')
  const cfg = metricsOfType(type)
  const seed = (hashStr(node?.nodeId || 'NODE') ^ (mode === 'plan' ? 0x9e3779b9 : 0x85ebca6b)) >>> 0
  const rng = makeRng(seed)
  const now = Date.now()
  const stepMs = 15 * 60 * 1000
  let startMs
  let count
  let step
  if (mode === '24h') {
    count = 96
    step = stepMs
    startMs = now - (count - 1) * step
  } else {
    let ps = Number(new Date(planStartISO).getTime())
    if (!ps || Number.isNaN(ps)) ps = now - 60 * 24 * 3600 * 1000
    const span = Math.max(stepMs, now - ps)
    const raw = Math.floor(span / stepMs) + 1
    const MAX = 240
    if (raw <= MAX) {
      count = raw
      step = stepMs
    } else {
      count = MAX
      step = span / (MAX - 1)
    }
    startMs = ps
  }

  const state = {}
  cfg.metrics.forEach((md) => {
    const raw = node?.metrics?.[md.srcKey]
    state[md.key] = clamp(Number(raw != null ? raw : md.min), md.min, md.max)
  })

  const points = []
  for (let i = 0; i < count; i++) {
    const p = { label: fmtClock(startMs + i * step), ts: startMs + i * step }
    cfg.metrics.forEach((md) => {
      const v = clamp(state[md.key] + between(rng, -md.jitter, md.jitter), md.min, md.max)
      state[md.key] = v
      p[md.key] = md.bar ? Math.round(v) : round(v, md.digits)
    })
    points.push(p)
  }
  return points
}

/** 同类型设备在「养护计划开始至今」区间内的分桶均值序列 */
export function buildTypeAvgSeries(model, stack, nodeType, bucketHours = 4) {
  const type = String(nodeType || '02')
  const cfg = metricsOfType(type)
  const now = Date.now()
  let ps = Number(new Date(stack?.plan?.startDate).getTime())
  if (!ps || Number.isNaN(ps)) ps = now - 30 * 24 * 3600 * 1000
  const span = Math.max(60 * 60 * 1000, now - ps)

  // 同类型设备的基准值 = 模型内所有含该类节点的垛位指标均值
  const owners = (model?.stacks || []).filter((s) => (s.nodes || []).some((n) => String(n.nodeType) === type))
  const pool = owners.length ? owners : (model?.stacks || [])
  const base = {}
  cfg.metrics.forEach((md) => {
    const vals = pool.map((s) => Number(s.metrics?.[md.srcKey])).filter((v) => Number.isFinite(v))
    base[md.key] = vals.length ? vals.reduce((a, b) => a + b, 0) / vals.length : md.min
  })

  const bucketMs = Math.max(1, bucketHours) * 3600 * 1000
  const rawCount = Math.max(2, Math.round(span / bucketMs))
  const MAX = 900
  const count = Math.min(MAX, rawCount)
  const step = span / (count - 1 || 1)
  const rng = makeRng(hashStr(`${stack?.stackId || 'S'}-${type}-${bucketHours}`))
  const scale = 1 / Math.sqrt(Math.max(1, bucketHours))
  // 每个指标一组随机相位，用多个周期的正弦叠加出平滑的真实感曲线（分桶越大越平滑）
  const phases = {}
  cfg.metrics.forEach((md) => {
    phases[md.key] = [rng() * Math.PI * 2, rng() * Math.PI * 2, rng() * Math.PI * 2]
  })
  const points = []
  for (let i = 0; i < count; i++) {
    const ts = ps + i * step
    const prog = count > 1 ? i / (count - 1) : 0
    const p = { label: fmtClock(ts), ts }
    cfg.metrics.forEach((md) => {
      const [p1, p2, p3] = phases[md.key]
      const amp = md.avgAmp * 1.7
      const wave =
        Math.sin(prog * Math.PI * 2 * 2.3 + p1) * 0.55 +
        Math.sin(prog * Math.PI * 2 * 6.1 + p2) * 0.3 +
        Math.sin(prog * Math.PI * 2 * 13.7 + p3) * 0.15
      const noise = between(rng, -1, 1) * md.jitter * scale * 0.9
      const v = clamp(base[md.key] + amp * wave + noise, md.min, md.max)
      p[md.key] = md.bar ? Math.max(0, Math.round(v)) : round(v, md.digits)
    })
    p.samples = Math.max(1, Math.round(step / (15 * 60 * 1000))) * Math.max(1, owners.length)
    points.push(p)
  }

  // 全期统计（按主指标）
  const main = cfg.metrics[0]
  const series = points.map((p) => Number(p[main.key])).filter((v) => Number.isFinite(v))
  const avg = series.length ? series.reduce((a, b) => a + b, 0) / series.length : 0
  const maxV = series.length ? Math.max(...series) : 0
  const minV = series.length ? Math.min(...series) : 0
  const atMax = points.find((p) => Number(p[main.key]) === maxV)
  const atMin = points.find((p) => Number(p[main.key]) === minV)

  return {
    type,
    label: cfg.label,
    count: owners.reduce((n, s) => n + (s.nodes || []).filter((x) => String(x.nodeType) === type).length, 0),
    owners: owners.length,
    bucketHours,
    pointCount: points.length,
    sampleCount: points[0]?.samples || 0,
    points,
    avg: round(avg, main.digits),
    max: round(maxV, main.digits),
    min: round(minV, main.digits),
    maxAt: atMax?.label || '',
    minAt: atMin?.label || '',
    startDate: stack?.plan?.startDate || ''
  }
}

/** 生成 24 小时趋势（对齐截图的时间轴 01:20 ~ 01:00） */
function buildTrend(rng, base) {
  const points = []
  let t = base.temp
  let h = base.humidity
  let m = base.moisture
  for (let i = 0; i < 24; i++) {
    t += between(rng, -0.45, 0.45)
    h += between(rng, -1.1, 1.1)
    m += between(rng, -0.12, 0.12)
    t = Math.max(23, Math.min(31, t))
    h = Math.max(58, Math.min(73, h))
    m = Math.max(10.6, Math.min(13.6, m))
    points.push({
      label: `${pad(Math.floor(i / 2) + 1, 2)}:${i % 2 === 0 ? '20' : '50'}`,
      temp: round(t, 1),
      humidity: round(h, 1),
      moisture: round(m, 2)
    })
  }
  return points
}

/** 按阈值判定垛位健康等级（阈值口径与 tb_alarm_value_setting / 仓间阈值一致） */
function judgeHealth(metrics, offline) {
  if (metrics.temp >= 30 || metrics.humidity >= 70 || metrics.moisture < 11 || metrics.moisture > 13.2 || metrics.pestTotal >= 4) {
    return 'danger'
  }
  if (metrics.temp >= 28.5 || metrics.humidity >= 68 || metrics.moisture < 11.4 || metrics.moisture > 13 || metrics.pestTotal >= 3) {
    return 'warn'
  }
  if (metrics.temp >= 28.2 || metrics.humidity >= 67.5 || offline > 0 || metrics.pestTotal >= 3) {
    return 'watch'
  }
  return 'normal'
}

function buildWarnings(rng, stack, health) {
  if (health === 'normal') return []
  const count = health === 'danger' ? 2 : 1
  const out = []
  for (let i = 0; i < count; i++) {
    const wt = pick(rng, WARNING_TYPES)
    out.push({
      id: `W-${stack.id}-${i + 1}`,
      warningType: wt.code,
      typeLabel: wt.label,
      kind: wt.kind,
      warningGrade: health === 'danger' ? 3 : health === 'warn' ? 2 : 1,
      nodeId: stack.nodes[0]?.nodeId || '-',
      stackId: stack.stackId,
      isAffirm: rng() > 0.45 ? '2' : '1',
      time: `2026-11-05 ${pad(8 + Math.floor(rng() * 11), 2)}:${pad(Math.floor(rng() * 60), 2)}`
    })
  }
  return out
}

function buildStack(area, row, col, index, rng) {
  const letter = 'ABCDE'[row]
  const code = `${letter}${col + 1}垛`
  const stackId = 301 + index
  const style = ['01', '02', '03'][(index + row) % 3]
  const phaseIdx = Math.floor(rng() * PHASE_SEQUENCE.length)
  const phases = PHASE_SEQUENCE[phaseIdx]
  const planDays = 90 + Math.floor(rng() * 180)
  const elapsedDays = Math.max(3, Math.min(planDays - 1, Math.floor(planDays * between(rng, 0.12, 0.92))))
  const startDate = new Date(Date.UTC(2026, 9, 1) + (index % 12) * 86400000 * 3)
  const start = `${startDate.getUTCFullYear()}-${pad(startDate.getUTCMonth() + 1, 2)}-${pad(startDate.getUTCDate(), 2)}`

  // 以「正常数据」为主（约 85%），保留少量越界样本用于验证分级配色与预警链路
  const hot = rng() < 0.035
  const wet = rng() < 0.03
  const pestHeavy = rng() < 0.05
  const metrics = {
    temp: round(hot ? between(rng, 28.6, 30.8) : between(rng, 24.8, 27.3)),
    humidity: round(wet ? between(rng, 68.2, 71.4) : between(rng, 61.5, 66.2)),
    moisture: round(between(rng, 11.55, 12.95), 2),
    oxygen: round(between(rng, 2.1, 8.6)),
    coreTemp: round(between(rng, 24.2, 27.6)),
    beetle: Math.floor(rng() * (pestHeavy ? 4 : 2)),
    mealworm: Math.floor(rng() * (pestHeavy ? 3 : 2))
  }
  metrics.pestTotal = metrics.beetle + metrics.mealworm

  const nodeCount = 3 + Math.floor(rng() * 4)
  const nodes = []
  let offline = 0
  for (let i = 0; i < nodeCount; i++) {
    const types = ['01', '02', '03', '04', '05']
    const nodeType = types[(i + index) % types.length]
    const bad = rng() < 0.012
    if (bad) offline++
    nodes.push({
      id: 1000 + index * 10 + i,
      nodeId: `NODE-${pad(index * 10 + i + 1, 4)}`,
      nodeType,
      typeLabel: NODE_TYPE_DICT[nodeType],
      status: bad ? '5' : '1',
      statusLabel: bad ? '异常' : '已入垛',
      caseNo: `BOX-${area.code}-${letter}${col + 1}-${pad(i + 1, 2)}`,
      siteX: String((i % 3) + 1),
      siteY: String(Math.floor(i / 3) + 1),
      siteZ: String((i % 2) + 1),
      stackId,
      siteRoute: `0,1,11,101,${area.id},${stackId}`,
      siteType: '05',
      gatherSite: `${area.name}/${code}`,
      stack: code,
      intoStackTime: `${start} 09:20:00`,
      lastReport: bad ? '超过 48 小时未上报' : `2026-11-05 ${pad(19, 2)}:${pad(Math.floor(rng() * 60), 2)}`,
      value: nodeType === '01' ? `${metrics.oxygen}%`
        : nodeType === '02' ? `${metrics.temp}℃ / ${metrics.humidity}%`
          : nodeType === '03' ? `${metrics.coreTemp}℃`
            : nodeType === '04' ? `${metrics.pestTotal}只` : `${metrics.moisture}%`
    })
  }

  const health = judgeHealth(metrics, offline)
  const stack = {
    id: `${area.id}-${letter}${col + 1}`,
    code,
    name: code,
    areaId: area.id,
    areaName: area.name,
    stackId,
    pidRoute: `0,1,11,101,${area.id},${stackId}`,
    siteType: '05',
    plan: {
      id: 100 + index,
      materialCode: `MAT-${area.code}-${pad(index + 1, 4)}`,
      materialName: MATERIALS[index % MATERIALS.length],
      maintenceStyle: style,
      styleLabel: STYLE_DICT[style],
      phases,
      phaseIndex: phaseIdx,
      stageId: 1000 + index,
      startDate: start,
      planDays,
      elapsedDays,
      progress: Math.round((elapsedDays / planDays) * 100),
      principal: PRINCIPALS[index % PRINCIPALS.length],
      isEnd: '0',
      maintainResult: 1,
      createdTime: `${start} 08:30:00`,
      source: 'demo'
    },
    nodes,
    metrics,
    health,
    alertLevel: HEALTH_DICT[health].level,
    onlineNodes: nodes.length - offline,
    totalNodes: nodes.length,
    warnings: []
  }
  stack.warnings = buildWarnings(rng, stack, health)
  stack.trend = buildTrend(rng, metrics)
  return stack
}

function buildFeed(stacks) {
  const warnStacks = stacks.filter((s) => s.health !== 'normal')
  const feed = [
    {
      id: 'F1', tag: 'AI', type: 'agent',
      title: '监控 Agent 已上线',
      body: `正在值守 ${AREA_DEFS.length} 个库区、${stacks.length} 个垛位，接入温湿度、含水率、虫情与设备数据。点右上角「注入异常」可演练自主巡检、分级预警与处置闭环。`,
      time: '20:12'
    },
    {
      id: 'F2', tag: 'AI', type: 'agent',
      title: '例行巡检完成',
      body: `${AREA_DEFS.length} 个库区、${stacks.length} 个垛位环境正常，设备在线，健康度 ${Math.round((stacks.filter((s) => s.health === 'normal').length / stacks.length) * 100)} 分。`,
      time: '20:10'
    },
    {
      id: 'F3', tag: 'AI', type: 'agent',
      title: '养护计划进度同步完成',
      body: `已同步 ${stacks.length} 个垛位的当前养护计划（阶段、养护天数、负责人），${stacks.filter((s) => s.plan.progress > 70).length} 个垛位进入养护后期。`,
      time: '20:08'
    }
  ]
  warnStacks.slice(0, 4).forEach((s, i) => {
    feed.push({
      id: `FW${i}`, tag: '预警', type: 'alarm',
      title: `${s.areaName} · ${s.code} ${HEALTH_DICT[s.health].label}预警`,
      body: `${s.warnings[0]?.typeLabel || '环境指标'} 触发；当前温度 ${s.metrics.temp}℃ / 湿度 ${s.metrics.humidity}% / 含水率 ${s.metrics.moisture}%，已生成处置任务。`,
      time: `19:${pad(50 - i * 7, 2)}`
    })
  })
  feed.push(
    { id: 'L1', tag: '日志', type: 'log', title: '节点数据上报', body: `本轮采集 ${stacks.reduce((n, s) => n + s.totalNodes, 0)} 个节点实时数据，全部写入历史表。`, time: '19:42' },
    { id: 'L2', tag: '日志', type: 'log', title: '阈值缓存刷新', body: '环境阈值按仓间 → 楼层 → 楼栋逐级向上查找，缓存已命中。', time: '19:35' },
    { id: 'L3', tag: '日志', type: 'log', title: '养护阶段统计', body: `阶段分布：${PHASE_SEQUENCE.map((p) => `${p} ${stacks.filter((s) => s.plan.phases === p).length} 垛`).join('、')}。`, time: '19:30' }
  )
  return feed
}

export function buildDemoModel() {
  const rng = makeRng(20261105)
  const stacks = []
  AREA_DEFS.forEach((area) => {
    for (let row = 0; row < 4; row++) {
      for (let col = 0; col < 5; col++) {
        const index = stacks.length
        stacks.push(buildStack(area, row, col, index, rng))
      }
    }
  })

  const areas = AREA_DEFS.map((a) => {
    const own = stacks.filter((s) => s.areaId === a.id)
    return {
      ...a,
      stackCount: own.length,
      warningCount: own.filter((s) => s.health !== 'normal').length,
      avgTemp: round(own.reduce((n, s) => n + s.metrics.temp, 0) / own.length),
      avgHumidity: round(own.reduce((n, s) => n + s.metrics.humidity, 0) / own.length),
      stacks: own
    }
  })

  return {
    version: SCREEN_DATA_VERSION,
    source: 'demo',
    generatedAt: new Date().toISOString(),
    areas,
    stacks,
    devices: [
      { id: 'D1', name: '除湿机', icon: 'drop', status: '运行中', running: true, detail: '1号醇化库 · 目标湿度 65%' },
      { id: 'D2', name: '空调', icon: 'cool', status: '已停止', running: false, detail: '2号醇化库 · 目标温度 26℃' },
      { id: 'D3', name: '风机', icon: 'fan', status: '运行中', running: true, detail: '3号醇化库 · 低速换气' }
    ],
    feed: buildFeed(stacks)
  }
}

/** 轻量表单 POST，直接走 /api（网关），不依赖 axios 拦截器 */
async function postForm(path, params) {
  const token = localStorage.getItem('rpzh_token') || ''
  const headers = { 'Content-Type': 'application/x-www-form-urlencoded' }
  if (token && token !== 'demo-local-token') headers.Authorization = token
  const body = new URLSearchParams()
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null) body.append(k, String(v))
  })
  const res = await fetch(`/api${path}`, { method: 'POST', headers, body: body.toString() })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  const json = await res.json()
  if (json?.flag === false) throw new Error(json?.message || '接口返回失败')
  return json?.data
}

const rowsOf = (d) => (Array.isArray(d) ? d : Array.isArray(d?.list) ? d.list : Array.isArray(d?.rows) ? d.rows : [])

/**
 * 把网关里的真实养护计划 / 节点注入到大屏模型前若干个垛位。
 * 真实字段缺失时保留虚拟值；任何异常都被吞掉并返回 error，保证大屏不白屏。
 */
export async function enrichWithReal(model) {
  const stats = { realPlans: 0, realNodes: 0, realAreas: 0, error: '' }
  try {
    const [planData, nodeData, treeData] = await Promise.all([
      postForm('/maintain/maintainPlan/findMaintainPlanInfo', { page: 1, size: 200 }),
      postForm('/maintain/node/findNode', { page: 1, size: 500 }),
      postForm('/maintain/warehouse/warehouseTree', { warehouseNodeId: 0, warehouseNodeType: '01' }).catch(() => null)
    ])
    const plans = rowsOf(planData)
    const nodes = rowsOf(nodeData)
    const warehouses = rowsOf(treeData)
    stats.realAreas = warehouses.length
    stats.realPlans = plans.length
    stats.realNodes = nodes.length

    // 计划：按 stackName 末段匹配垛位；匹配不上就顺序注入前 N 个
    plans.forEach((p, i) => {
      const stack = model.stacks[i % model.stacks.length]
      if (!stack) return
      const tail = String(p.stackName || '').split('/').pop()
      const matched = model.stacks.find((s) => s.code === tail) || model.stacks.find((s) => String(p.stackName || '').includes(s.name))
      const target = matched || stack
      target.plan = {
        ...target.plan,
        id: p.id ?? target.plan.id,
        materialCode: p.materialCode || target.plan.materialCode,
        maintenceStyle: p.maintenceStyle || target.plan.maintenceStyle,
        styleLabel: STYLE_DICT[p.maintenceStyle] || target.plan.styleLabel,
        phases: p.phases || target.plan.phases,
        stageId: p.stageId ?? target.plan.stageId,
        isEnd: p.isEnd ?? target.plan.isEnd,
        createdTime: p.createdTime || target.plan.createdTime,
        source: 'real'
      }
      if (tail && !matched) target.realStackName = tail
    })

    // 节点：按 stack 名称 / siteId 归到对应垛位，覆盖该垛位的节点清单
    const grouped = new Map()
    nodes.forEach((n) => {
      const key = n.stack || n.gatherSite || `S${n.siteId ?? 'x'}`
      if (!grouped.has(key)) grouped.set(key, [])
      grouped.get(key).push({
        id: n.id,
        nodeId: n.nodeId,
        nodeType: n.nodeType,
        typeLabel: NODE_TYPE_DICT[n.nodeType] || n.nodeType,
        status: String(n.status ?? '1'),
        statusLabel: STATUS_DICT[String(n.status ?? '1')] || '未知',
        caseNo: n.caseNo || '-',
        siteX: n.siteX ?? '',
        siteY: n.siteY ?? '',
        siteZ: n.siteZ ?? '',
        stackId: n.siteId ?? n.stackId,
        siteRoute: n.siteRoute || '',
        siteType: n.siteType || '05',
        gatherSite: n.gatherSite || '',
        stack: n.stack || '',
        intoStackTime: n.intoStackTime || '',
        lastReport: n.lastUpdatedTime || n.intoStackTime || '--',
        value: '--',
        source: 'real'
      })
    })
    let gi = 0
    grouped.forEach((list, key) => {
      const target = model.stacks.find((s) => key === s.name || String(key).includes(s.name)) || model.stacks[gi % model.stacks.length]
      target.nodes = list
      target.totalNodes = list.length
      target.onlineNodes = list.filter((n) => n.status === '1').length
      target.realNodeGroup = key
      gi++
    })

    model.realInjected = true
    model.realStats = stats
  } catch (e) {
    stats.error = e?.message || String(e)
    model.realInjected = false
    model.realStats = stats
  }
  return { model, stats }
}

/** 模拟一次实时刷新：让温度和湿度轻微波动，并重新判定健康等级 */
export function tickModel(model) {
  const rng = makeRng(Date.now() % 100000)
  model.stacks.forEach((s) => {
    s.metrics.temp = round(Math.max(23.5, Math.min(31.2, s.metrics.temp + between(rng, -0.18, 0.18))))
    s.metrics.humidity = round(Math.max(58, Math.min(72.5, s.metrics.humidity + between(rng, -0.4, 0.4))))
    s.metrics.moisture = round(Math.max(10.8, Math.min(13.5, s.metrics.moisture + between(rng, -0.03, 0.03))), 2)
    s.health = judgeHealth(s.metrics, s.totalNodes - s.onlineNodes)
    s.alertLevel = HEALTH_DICT[s.health].level
    const last = s.trend[s.trend.length - 1]
    if (last) {
      last.temp = s.metrics.temp
      last.humidity = s.metrics.humidity
      last.moisture = s.metrics.moisture
    }
  })
  model.generatedAt = new Date().toISOString()
  return model
}

<script setup>
/**
 * 养护对比 · 双序列叠加曲线（纯 SVG）
 *
 * 与 TrendChart 的区别：TrendChart 是「一个对象的多指标分通道」，
 * 这里是「同一指标的两个对象叠在同一坐标系」，便于直接比高低、比波动、比走势。
 * 特性：自适应量程、阈值线、两线之间的差值填充、悬停十字线与对照浮层。
 */
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  points: { type: Array, default: () => [] },
  // [{ key: 'a', name: 'A1垛', color: '#2f86d6' }]
  series: { type: Array, default: () => [] },
  // { label, unit, digits, warn, warnLow, warnText, warnLowText, tol }
  metric: { type: Object, default: () => ({}) },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  height: { type: Number, default: 320 }
})

const PAD = { left: 56, right: 22, top: 14, bottom: 26 }

const wrap = ref(null)
const width = ref(720)
let ro = null

onMounted(() => {
  if (!wrap.value) return
  width.value = wrap.value.clientWidth || 720
  if (typeof ResizeObserver !== 'undefined') {
    ro = new ResizeObserver((entries) => {
      const cw = entries[0]?.contentRect?.width
      if (cw && Math.abs(cw - width.value) > 1) width.value = cw
    })
    ro.observe(wrap.value)
  }
})
onBeforeUnmount(() => { ro?.disconnect() })

const digits = computed(() => props.metric?.digits ?? 1)
const unit = computed(() => props.metric?.unit || '')

function fmt(v, d = digits.value) {
  if (v == null || !Number.isFinite(Number(v))) return '--'
  return Number(v).toFixed(d)
}

const plotW = computed(() => Math.max(80, width.value - PAD.left - PAD.right))
const plotH = computed(() => Math.max(80, props.height - PAD.top - PAD.bottom))
const n = computed(() => props.points.length)
const stepX = computed(() => (n.value > 1 ? plotW.value / (n.value - 1) : 0))
const xOf = (i) => PAD.left + i * stepX.value

/** 自适应量程：覆盖两条曲线，留 12% 边距，并保证一个最小可视跨度（否则线会贴成直线） */
const scale = computed(() => {
  const vals = []
  props.points.forEach((p) => {
    props.series.forEach((s) => {
      const v = Number(p[s.key])
      if (Number.isFinite(v)) vals.push(v)
    })
  })
  if (!vals.length) return { lo: 0, hi: 1 }
  const dMin = Math.min(...vals)
  const dMax = Math.max(...vals)
  const mid = (dMin + dMax) / 2
  const minSpan = Math.max((props.metric?.tol || 0.4) * 3, 0.6)
  const span = Math.max(dMax - dMin, minSpan) * 1.24
  return { lo: mid - span / 2, hi: mid + span / 2 }
})

const yOf = (v) => {
  const { lo, hi } = scale.value
  const ratio = Math.max(0, Math.min(1, (Number(v) - lo) / Math.max(1e-6, hi - lo)))
  return PAD.top + (1 - ratio) * plotH.value
}

function smoothPath(pts) {
  if (!pts.length) return ''
  if (pts.length === 1) return `M${pts[0].x.toFixed(1)},${pts[0].y.toFixed(1)}`
  let d = `M${pts[0].x.toFixed(1)},${pts[0].y.toFixed(1)}`
  const t = 0.16
  for (let i = 0; i < pts.length - 1; i++) {
    const p0 = pts[i - 1] || pts[i]
    const p1 = pts[i]
    const p2 = pts[i + 1]
    const p3 = pts[i + 2] || p2
    const c1x = p1.x + (p2.x - p0.x) * t
    const c1y = p1.y + (p2.y - p0.y) * t
    const c2x = p2.x - (p3.x - p1.x) * t
    const c2y = p2.y - (p3.y - p1.y) * t
    d += ` C${c1x.toFixed(1)},${c1y.toFixed(1)} ${c2x.toFixed(1)},${c2y.toFixed(1)} ${p2.x.toFixed(1)},${p2.y.toFixed(1)}`
  }
  return d
}

/** 折线（不做平滑）：计数类指标是离散取值，平滑会产生"2.4 只"这种不存在的高点 */
function polyPath(pts) {
  return pts.map((p, i) => `${i ? 'L' : 'M'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')
}

/** 整数指标（虫数）走折线，其余走平滑曲线 */
const useSmooth = computed(() => (props.metric?.digits ?? 1) > 0)
function pathOf(pts) { return useSmooth.value ? smoothPath(pts) : polyPath(pts) }

/** 两条曲线的坐标点 + 路径 */
const lines = computed(() => props.series.map((s) => {
  const pts = props.points
    .map((p, i) => ({ v: Number(p[s.key]), x: xOf(i) }))
    .filter((q) => Number.isFinite(q.v))
    .map((q) => ({ x: q.x, y: yOf(q.v) }))
  return { ...s, pts, d: pathOf(pts), last: pts.length ? pts[pts.length - 1] : null }
}))

/** 两线之间的差值填充（A 正向 + B 反向），直观看出谁高谁低 */
const gapPath = computed(() => {
  if (lines.value.length < 2) return ''
  const [l1, l2] = lines.value
  if (l1.pts.length < 2 || l2.pts.length < 2) return ''
  const revPts = [...l2.pts].reverse()
  const rev = pathOf(revPts).replace(/^M/, 'L')
  return `${l1.d} ${rev} Z`
})

/** 横向网格 + Y 轴刻度（整数指标会出现 -0 / 重复刻度，这里统一清洗） */
const grid = computed(() => {
  const { lo, hi } = scale.value
  const rows = 4
  const out = []
  const seen = new Set()
  for (let i = 0; i <= rows; i++) {
    const v = lo + ((hi - lo) * i) / rows
    let text = fmt(v, digits.value)
    if (Number(text) === 0) text = fmt(0, digits.value)
    if (seen.has(text)) continue
    seen.add(text)
    out.push({ v, y: yOf(v), text })
  }
  return out
})

/** X 轴刻度：最多 6 个，长跨度只显示日期 */
const xTicks = computed(() => {
  const total = n.value
  if (!total) return []
  const spanDays = (props.points[total - 1]?.ts - props.points[0]?.ts) / 86400000
  const max = Math.min(6, total)
  const idxs = []
  for (let i = 0; i < max; i++) {
    idxs.push(Math.round((i * (total - 1)) / (max - 1 || 1)))
  }
  return [...new Set(idxs)].map((i) => {
    const p = props.points[i]
    const d = new Date(p.ts)
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const mi = String(d.getMinutes()).padStart(2, '0')
    return {
      x: xOf(i),
      text: spanDays > 3 ? `${mm}-${dd}` : `${hh}:${mi}`,
      anchor: i === 0 ? 'start' : i === total - 1 ? 'end' : 'middle'
    }
  })
})

/** 阈值线（上限 / 下限，含水率这类上下限都有的指标会画两条） */
const thresholds = computed(() => {
  const m = props.metric || {}
  const { lo, hi } = scale.value
  const out = []
  if (m.warn != null && m.warn >= lo && m.warn <= hi) {
    out.push({ y: yOf(m.warn), text: m.warnText || `阈值 ${m.warn}${unit.value}`, tone: '#e05a4f' })
  }
  if (m.warnLow != null && m.warnLow >= lo && m.warnLow <= hi) {
    out.push({ y: yOf(m.warnLow), text: m.warnLowText || `下限 ${m.warnLow}${unit.value}`, tone: '#3f8f6f' })
  }
  return out
})

const hoverIdx = ref(-1)
const hovered = computed(() => (hoverIdx.value >= 0 ? props.points[hoverIdx.value] : null))

function onMove(evt) {
  if (!n.value) return
  const rect = evt.currentTarget.getBoundingClientRect()
  const x = evt.clientX - rect.left
  const i = Math.round((x - PAD.left) / Math.max(1e-6, stepX.value))
  hoverIdx.value = Math.max(0, Math.min(n.value - 1, i))
}
function onLeave() { hoverIdx.value = -1 }

const tipStyle = computed(() => {
  if (hoverIdx.value < 0) return { display: 'none' }
  const x = xOf(hoverIdx.value)
  const left = Math.max(96, Math.min(width.value - 96, x))
  return { left: `${left}px` }
})

const delta = computed(() => {
  if (!props.points.length) return null
  const p = props.points[props.points.length - 1]
  const a = Number(p.a)
  const b = Number(p.b)
  if (!Number.isFinite(a) || !Number.isFinite(b)) return null
  return round(a - b)
})
function round(v) { const d = digits.value; return Number(Number(v).toFixed(d)) }
</script>

<template>
  <div ref="wrap" class="cmp-chart">
    <div class="cmp-chart__head">
      <div class="cmp-chart__text">
        <span class="cmp-chart__title">{{ title }}</span>
        <span v-if="subtitle" class="cmp-chart__sub">{{ subtitle }}</span>
      </div>
      <div class="cmp-chart__legend">
        <span v-for="s in series" :key="s.key" class="cmp-legend">
          <i :style="{ background: s.color }" />
          <span class="cmp-legend__name">{{ s.name }}</span>
          <b :style="{ color: s.color }">{{ fmt(points.length ? points[points.length - 1][s.key] : null) }}{{ unit }}</b>
        </span>
        <span v-if="delta !== null" class="cmp-chart__delta" :class="{ 'is-up': delta > 0, 'is-down': delta < 0 }">
          A - B = {{ delta > 0 ? '+' : '' }}{{ fmt(delta) }}{{ unit }}
        </span>
      </div>
    </div>

    <div class="cmp-chart__plot" :style="{ height: `${height}px` }">
      <svg :width="width" :height="height" @mousemove="onMove" @mouseleave="onLeave">
        <!-- 横向网格 -->
        <g>
          <line
            v-for="(g, i) in grid" :key="`g${i}`"
            :x1="PAD.left" :x2="PAD.left + plotW" :y1="g.y" :y2="g.y"
            stroke="#eceff3" stroke-width="1"
          />
          <text
            v-for="(g, i) in grid" :key="`gt${i}`"
            :x="PAD.left - 8" :y="g.y + 3.5" text-anchor="end"
            fill="#98a2b3" font-size="10"
          >{{ g.text }}</text>
        </g>

        <!-- 阈值线 -->
        <g v-for="(th, ti) in thresholds" :key="`th${ti}`">
          <line
            :x1="PAD.left" :x2="PAD.left + plotW" :y1="th.y" :y2="th.y"
            :stroke="th.tone" stroke-width="1" stroke-dasharray="5 4" opacity="0.75"
          />
          <text :x="PAD.left + plotW" :y="th.y - 4" text-anchor="end" :fill="th.tone" font-size="9.5">
            {{ th.text }}
          </text>
        </g>

        <!-- 两线之间的差值填充 -->
        <path v-if="gapPath" :d="gapPath" fill="#7f8fa4" opacity="0.1" />

        <!-- 两条曲线 -->
        <path
          v-for="(l, i) in lines" :key="`l${i}`"
          :d="l.d" fill="none" :stroke="l.color" stroke-width="2"
          stroke-linejoin="round" stroke-linecap="round"
        />

        <!-- 末点标记 -->
        <circle
          v-for="(l, i) in lines" :key="`d${i}`"
          v-show="l.last"
          :cx="l.last ? l.last.x : 0" :cy="l.last ? l.last.y : 0"
          r="3" :fill="l.color" stroke="#fff" stroke-width="1.5"
        />

        <!-- X 轴刻度 -->
        <text
          v-for="(t, i) in xTicks" :key="`x${i}`"
          :x="t.x" :y="PAD.top + plotH + 16" :text-anchor="t.anchor"
          fill="#98a2b3" font-size="10"
        >{{ t.text }}</text>

        <!-- 悬停十字线 -->
        <g v-if="hovered">
          <line
            :x1="xOf(hoverIdx)" :x2="xOf(hoverIdx)" :y1="PAD.top" :y2="PAD.top + plotH"
            stroke="#9fb0c2" stroke-width="1" stroke-dasharray="3 3"
          />
          <circle
            v-for="(s, i) in series" :key="`hc${i}`"
            :cx="xOf(hoverIdx)" :cy="yOf(Number(hovered[s.key]))"
            r="3.4" :fill="s.color" stroke="#fff" stroke-width="1.5"
          />
        </g>
      </svg>

      <div v-if="hovered" class="cmp-tip" :style="tipStyle">
        <div class="cmp-tip__time">{{ hovered.label }}</div>
        <div v-for="s in series" :key="s.key" class="cmp-tip__row">
          <i :style="{ background: s.color }" />
          <span>{{ s.name }}</span>
          <b :style="{ color: s.color }">{{ fmt(hovered[s.key]) }}{{ unit }}</b>
        </div>
        <div class="cmp-tip__row cmp-tip__row--delta">
          <span>差值</span>
          <b>{{ fmt(Number(hovered.a) - Number(hovered.b)) }}{{ unit }}</b>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cmp-chart { width: 100%; }
.cmp-chart__head {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 12px; margin-bottom: 4px; flex-wrap: wrap;
}
.cmp-chart__text { display: flex; flex-direction: column; gap: 2px; }
.cmp-chart__title { font-size: 13px; font-weight: 600; color: var(--el-text-color-primary); }
.cmp-chart__sub { font-size: 11px; color: var(--el-text-color-secondary); }
.cmp-chart__legend { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.cmp-legend { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; color: var(--el-text-color-regular); }
.cmp-legend i { width: 8px; height: 8px; border-radius: 2px; display: inline-block; }
.cmp-legend__name { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cmp-legend b { font-weight: 600; font-variant-numeric: tabular-nums; }
.cmp-chart__delta {
  font-size: 12px; color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light); border-radius: 10px; padding: 1px 8px;
  font-variant-numeric: tabular-nums;
}
.cmp-chart__delta.is-up { color: #c4562f; }
.cmp-chart__delta.is-down { color: #2f7d63; }
.cmp-chart__plot { position: relative; width: 100%; }
.cmp-chart__plot svg { display: block; }
.cmp-tip {
  position: absolute; top: 6px; transform: translateX(-50%);
  min-width: 150px; padding: 7px 9px;
  background: rgba(28, 37, 48, 0.94); border-radius: 6px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
  pointer-events: none; z-index: 3;
}
.cmp-tip__time { font-size: 10.5px; color: #9fb0c2; margin-bottom: 4px; }
.cmp-tip__row {
  display: flex; align-items: center; gap: 6px;
  font-size: 11.5px; color: #d5e0ea; line-height: 1.75;
}
.cmp-tip__row i { width: 7px; height: 7px; border-radius: 2px; }
.cmp-tip__row b { margin-left: auto; font-variant-numeric: tabular-nums; }
.cmp-tip__row--delta {
  margin-top: 3px; padding-top: 3px; border-top: 1px solid rgba(255, 255, 255, 0.14);
  color: #9fb0c2;
}
</style>

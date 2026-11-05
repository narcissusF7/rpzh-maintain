<script setup>
/**
 * 大屏通用趋势图（纯 SVG，无第三方依赖）
 *
 * 设计要点（针对原实现的显示问题做了修正）：
 * 1) 按容器真实宽度绘制（ResizeObserver），不再用 preserveAspectRatio="none" 横向拉伸，
 *    文字与曲线不会被压扁/拉长。
 * 2) 每个通道按自身数据自适应量程（带最小可视跨度），曲线有起伏而不是贴成一条直线；
 *    阈值虚线仅在可视范围内绘制，超出时在右侧留白区显示箭头标记，避免文字压在曲线上。
 * 3) 通道、单位、阈值全部由 bands 配置驱动 —— 不同设备类型可以显示不同的指标。
 * 4) 支持悬停联动：竖线 + 各通道当前点 + 数据浮层。
 */
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  points: { type: Array, default: () => [] },
  // [{ key, label, unit, color, digits, warn, warnLow, warnText, warnLowText, bar, minRange, floor, ceil }]
  bands: { type: Array, default: () => [] },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  bandHeight: { type: Number, default: 52 },
  emptyText: { type: String, default: '暂无数据' }
})

const PAD = { left: 44, right: 60, top: 2, bottom: 18, gap: 8 }

const wrap = ref(null)
const width = ref(640)
let ro = null

onMounted(() => {
  if (!wrap.value) return
  width.value = wrap.value.clientWidth || 640
  if (typeof ResizeObserver !== 'undefined') {
    ro = new ResizeObserver((entries) => {
      const cw = entries[0]?.contentRect?.width
      if (cw && Math.abs(cw - width.value) > 1) width.value = cw
    })
    ro.observe(wrap.value)
  }
})
onBeforeUnmount(() => { ro?.disconnect() })

const n = computed(() => props.points.length)
const plotW = computed(() => Math.max(60, width.value - PAD.left - PAD.right))
const H = computed(() => PAD.top + props.bands.length * props.bandHeight + (props.bands.length - 1) * PAD.gap + PAD.bottom)
const stepX = computed(() => (n.value > 1 ? plotW.value / (n.value - 1) : 0))
const xOf = (i) => PAD.left + i * stepX.value

const hoverIdx = ref(-1)
const hovered = computed(() => (hoverIdx.value >= 0 ? props.points[hoverIdx.value] : null))

function fmt(v, digits = 1) {
  if (v == null || !Number.isFinite(Number(v))) return '--'
  return Number(v).toFixed(digits)
}

/** Catmull-Rom → 三次贝塞尔，轻微平滑且不过冲 */
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

/** 逐通道算出量程 / 路径 / 阈值位置 */
const view = computed(() => props.bands.map((b, i) => {
  const top = PAD.top + i * (props.bandHeight + PAD.gap)
  const digits = b.digits ?? 1
  const innerTop = top + 6
  const innerH = props.bandHeight - 12

  const vals = props.points.map((p) => Number(p[b.key])).filter((v) => Number.isFinite(v))
  const hasData = vals.length > 0
  const dMin = hasData ? Math.min(...vals) : 0
  const dMax = hasData ? Math.max(...vals) : 1
  const mid = (dMin + dMax) / 2
  const span = Math.max(dMax - dMin, b.minRange ?? 1) * 1.35

  let lo = mid - span / 2
  let hi = mid + span / 2
  if (b.floor != null) lo = Math.max(b.floor, lo)
  if (b.ceil != null) hi = Math.min(b.ceil, hi)

  const y = (v) => {
    const ratio = Math.max(0, Math.min(1, (Number(v) - lo) / Math.max(1e-6, hi - lo)))
    return innerTop + (1 - ratio) * innerH
  }

  const pts = props.points
    .map((p, k) => ({ raw: Number(p[b.key]), x: xOf(k) }))
    .filter((q) => Number.isFinite(q.raw))
    .map((q) => ({ x: q.x, y: y(q.raw) }))
  const line = smoothPath(pts)
  const baseY = top + props.bandHeight - 6
  const area = line && pts.length > 1
    ? `${line} L${pts[pts.length - 1].x.toFixed(1)},${baseY} L${pts[0].x.toFixed(1)},${baseY} Z`
    : ''

  const thresholds = []
  const pushTh = (v, text, tone) => {
    if (v == null) return
    const inView = v >= lo && v <= hi
    const rawY = inView ? y(v) : (v > hi ? top + 20 : top + props.bandHeight - 7)
    // 徽标纵向夹紧在通道内，避免压住顶部的通道名/当前值
    const badgeTop = Math.max(top + 19, Math.min(top + props.bandHeight - 8, rawY)) - 6
    thresholds.push({ v, text: text || `${v}${b.unit || ''}`, tone, inView, y: inView ? y(v) : null, badgeTop })
  }
  pushTh(b.warn, b.warnText, b.color)
  pushTh(b.warnLow, b.warnLowText, '#f0b06a')

  const lastVal = n.value ? props.points[n.value - 1][b.key] : null
  const barW = Math.max(1.2, Math.min(9, stepX.value * 0.62))

  return { b, i, top, digits, lo, hi, y, line, area, pts, thresholds, lastVal, barW, innerH, hasData }
}))

const xTicks = computed(() => {
  if (!n.value) return []
  const want = width.value < 380 ? 3 : width.value < 560 ? 4 : 6
  const out = []
  for (let k = 0; k < want; k++) {
    const i = Math.round(((n.value - 1) * k) / (want - 1))
    if (!out.some((o) => o.i === i)) out.push({ i, label: props.points[i]?.label || '' })
  }
  return out
})

function onMove(e) {
  if (!n.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  const x = e.clientX - rect.left
  const i = Math.round((x - PAD.left) / (stepX.value || 1))
  hoverIdx.value = Math.max(0, Math.min(n.value - 1, i))
}
function onLeave() { hoverIdx.value = -1 }

/** 悬停点的 y（数据缺失时返回 null，避免渲染 NaN） */
function hoverY(v) {
  const raw = props.points[hoverIdx.value]?.[v.b.key]
  const num = Number(raw)
  if (raw == null || raw === '' || !Number.isFinite(num)) return null
  return v.y(num)
}

const tipStyle = computed(() => {
  if (hoverIdx.value < 0) return {}
  const x = xOf(hoverIdx.value)
  const left = Math.min(Math.max(x + 12, 8), Math.max(8, width.value - 138))
  return { left: `${left}px`, top: `${PAD.top + 4}px` }
})
</script>

<template>
  <div ref="wrap" class="trend">
    <div v-if="title || subtitle" class="trend__head">
      <div class="trend__titles">
        <span class="trend__title">{{ title }}</span>
        <span v-if="subtitle" class="trend__sub">{{ subtitle }}</span>
      </div>
      <div class="trend__legend">
        <span v-for="b in bands" :key="b.key" class="trend__legend-item">
          <i :style="{ background: b.color }" />{{ b.label
          }}<em v-if="hovered"> {{ fmt(hovered[b.key], b.digits) }}{{ b.unit }}</em>
        </span>
      </div>
    </div>

    <div v-if="!points.length" class="trend__empty">{{ emptyText }}</div>

    <svg
      v-else
      class="trend__svg"
      :viewBox="`0 0 ${width} ${H}`"
      :style="{ height: `${H}px` }"
      @mousemove="onMove"
      @mouseleave="onLeave"
      role="img"
      :aria-label="title || '趋势图'"
    >
      <defs>
        <linearGradient v-for="(v, i) in view" :key="`grad${i}`" :id="`tg-${i}`" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" :stop-color="v.b.color" stop-opacity="0.30" />
          <stop offset="100%" :stop-color="v.b.color" stop-opacity="0.01" />
        </linearGradient>
      </defs>

      <g v-for="(v, i) in view" :key="`band${i}`">
        <!-- 通道底板 -->
        <rect
          :x="PAD.left" :y="v.top" :width="plotW" :height="bandHeight"
          fill="#0e1e2c" stroke="#1b3145" rx="4"
        />

        <!-- 量程刻度（上 / 中 / 下） -->
        <template v-for="(t, ti) in [v.hi, (v.hi + v.lo) / 2, v.lo]" :key="`t${i}-${ti}`">
          <line :x1="PAD.left" :x2="PAD.left + plotW" :y1="v.y(t)" :y2="v.y(t)" stroke="#1b3043" stroke-width="0.6" />
          <text :x="PAD.left - 6" :y="v.y(t) + 3" text-anchor="end" fill="#5b768c" font-size="8.5">
            {{ fmt(t, v.digits) }}
          </text>
        </template>

        <!-- 阈值 -->
        <template v-for="(th, ti) in v.thresholds" :key="`th${i}-${ti}`">
          <line
            v-if="th.inView"
            :x1="PAD.left" :x2="PAD.left + plotW" :y1="th.y" :y2="th.y"
            :stroke="th.tone" stroke-width="0.9" stroke-dasharray="5 4" opacity="0.6"
          />
          <g :transform="`translate(${PAD.left + plotW + 4},${th.badgeTop})`">
            <title>{{ th.text }}</title>
            <rect width="52" height="13" rx="3" :fill="th.tone" opacity="0.14" />
            <text x="26" y="9.4" text-anchor="middle" :fill="th.tone" font-size="8.4">
              {{ th.inView ? `${th.v}${v.b.unit || ''}` : `${th.v > v.hi ? '↑' : '↓'} ${th.v}${v.b.unit || ''}` }}
            </text>
          </g>
        </template>

        <!-- 曲线 / 柱 -->
        <template v-if="v.hasData">
          <template v-if="v.b.bar">
            <rect
              v-for="(p, k) in v.pts" :key="`bar${i}-${k}`"
              :x="p.x - v.barW / 2" :y="p.y" :width="v.barW" :height="Math.max(1, v.top + bandHeight - 6 - p.y)"
              :fill="v.b.color" opacity="0.55" rx="1"
            />
          </template>
          <template v-else>
            <path :d="v.area" :fill="`url(#tg-${i})`" />
            <path :d="v.line" fill="none" :stroke="v.b.color" stroke-width="1.6" stroke-linejoin="round" stroke-linecap="round" />
          </template>
        </template>
        <text
          v-else :x="PAD.left + plotW / 2" :y="v.top + bandHeight / 2 + 3"
          text-anchor="middle" fill="#446176" font-size="9.5"
        >该通道暂无数据</text>

        <!-- 通道标题 + 当前值（描边保证压在曲线上也清晰） -->
        <circle :cx="PAD.left + 8" :cy="v.top + 11" r="2.6" :fill="v.b.color" />
        <text
          :x="PAD.left + 15" :y="v.top + 14.4" :fill="v.b.color" font-size="9.6" font-weight="600"
          stroke="#0b1826" stroke-width="2.6" paint-order="stroke"
        >
          {{ v.b.label }}<tspan fill="#7893a8" font-weight="400"> / {{ v.b.unit }}</tspan>
        </text>
        <text
          :x="PAD.left + plotW - 8" :y="v.top + 14.4" text-anchor="end"
          fill="#dceaf5" font-size="9.6" font-weight="600"
          stroke="#0b1826" stroke-width="2.6" paint-order="stroke"
        >当前 {{ fmt(v.lastVal, v.digits) }}{{ v.b.unit }}</text>

        <!-- 末点标记 -->
        <circle
          v-if="v.hasData && !v.b.bar && v.pts.length"
          :cx="v.pts[v.pts.length - 1].x" :cy="v.pts[v.pts.length - 1].y"
          r="2.8" :fill="v.b.color" stroke="#0b1826" stroke-width="1.4"
        />
      </g>

      <!-- 悬停竖线 + 各通道当前点 -->
      <g v-if="hoverIdx >= 0">
        <line
          :x1="xOf(hoverIdx)" :x2="xOf(hoverIdx)" :y1="PAD.top" :y2="H - PAD.bottom"
          stroke="#7fd4f0" stroke-width="0.8" stroke-dasharray="3 3" opacity="0.75"
        />
        <circle
          v-for="(v, i) in view" :key="`hv${i}`"
          v-show="hoverY(v) !== null"
          :cx="xOf(hoverIdx)"
          :cy="hoverY(v) ?? 0"
          r="3.2" :fill="v.b.color" stroke="#0b1826" stroke-width="1.4"
        />
      </g>

      <!-- 时间轴 -->
      <text
        v-for="t in xTicks" :key="`x${t.i}`"
        :x="xOf(t.i)" :y="H - 5"
        :text-anchor="t.i === 0 ? 'start' : t.i === n - 1 ? 'end' : 'middle'"
        fill="#5b768c" font-size="8.5"
      >{{ t.label }}</text>
    </svg>

    <!-- 悬停数据浮层 -->
    <div v-if="hovered" class="trend__tip" :style="tipStyle">
      <div class="trend__tip-time">{{ hovered.label }}</div>
      <div v-for="b in bands" :key="b.key" class="trend__tip-row">
        <i :style="{ background: b.color }" />
        <span>{{ b.label }}</span>
        <b>{{ fmt(hovered[b.key], b.digits) }}{{ b.unit }}</b>
      </div>
    </div>
  </div>
</template>

<style scoped>
.trend { width: 100%; position: relative; }
.trend__head {
  display: flex; align-items: flex-end; justify-content: space-between;
  gap: 10px; margin-bottom: 8px; flex-wrap: wrap;
}
.trend__titles { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.trend__title { font-size: 12.5px; font-weight: 650; color: #dcecf7; }
.trend__sub { font-size: 9.5px; color: #5f8ba3; }
.trend__legend { display: flex; align-items: center; gap: 12px; font-size: 10px; color: #8aa8bc; }
.trend__legend-item { display: inline-flex; align-items: center; gap: 4px; white-space: nowrap; }
.trend__legend i { width: 7px; height: 7px; border-radius: 2px; display: inline-block; }
.trend__legend em { font-style: normal; color: #cfe3f0; font-variant-numeric: tabular-nums; }
.trend__svg { width: 100%; display: block; overflow: visible; }
.trend__empty {
  height: 96px; display: grid; place-items: center;
  font-size: 11px; color: #5f8ba3; border: 1px dashed #1c3448; border-radius: 8px;
}
.trend__tip {
  position: absolute; z-index: 5; pointer-events: none;
  background: #0a1826f2; border: 1px solid #2a4b66; border-radius: 6px;
  padding: 6px 8px; min-width: 110px; box-shadow: 0 6px 18px #0008;
}
.trend__tip-time { font-size: 9px; color: #7fa0b5; margin-bottom: 4px; }
.trend__tip-row { display: flex; align-items: center; gap: 5px; font-size: 10px; color: #a9c6d8; line-height: 1.7; }
.trend__tip-row i { width: 6px; height: 6px; border-radius: 2px; }
.trend__tip-row b { margin-left: auto; color: #eaf6ff; font-variant-numeric: tabular-nums; }
</style>

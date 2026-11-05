<template>
  <div class="app-page compare-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>养护对比</h2>
        <p>同一时间窗对比温度、相对湿度、含水率、氧气浓度、包芯温度与诱捕虫数；支持垛位↔垛位、垛位↔仓间、仓间↔仓间三种口径</p>
      </div>
      <el-button size="small" :icon="RefreshRight" @click="reload">重新生成</el-button>
    </div>

    <!-- 对比模式 -->
    <div class="app-card compare-toolbar">
      <div class="compare-modes">
        <button
          v-for="m in COMPARE_MODES"
          :key="m.value"
          class="compare-mode"
          :class="{ 'is-on': mode === m.value }"
          @click="switchMode(m.value)"
        >
          <span class="compare-mode__label">{{ m.label }}</span>
          <span class="compare-mode__desc">{{ m.desc }}</span>
        </button>
      </div>

      <div class="compare-filters">
        <div class="compare-field">
          <span class="compare-field__label">{{ aKind === 'area' ? '仓间 A' : '垛位 A' }}</span>
          <el-select v-model="aId" filterable size="small" class="compare-field__select">
            <template v-if="aKind === 'area'">
              <el-option v-for="o in areas" :key="o.value" :label="o.label" :value="o.value" />
            </template>
            <template v-else>
              <el-option-group v-for="g in groups" :key="g.label" :label="g.label">
                <el-option v-for="o in g.options" :key="o.value" :label="o.label" :value="o.value" />
              </el-option-group>
            </template>
          </el-select>
        </div>

        <el-button
          class="compare-swap"
          :icon="Switch"
          circle
          size="small"
          :disabled="aKind !== bKind"
          :title="aKind !== bKind ? '垛位与仓间类型不同，无法交换' : '交换两端'"
          @click="swap"
        />

        <div class="compare-field">
          <span class="compare-field__label">{{ bKind === 'area' ? '仓间 B' : '垛位 B' }}</span>
          <el-select v-model="bId" filterable size="small" class="compare-field__select">
            <template v-if="bKind === 'area'">
              <el-option v-for="o in areas" :key="o.value" :label="o.label" :value="o.value" />
            </template>
            <template v-else>
              <el-option-group v-for="g in groups" :key="g.label" :label="g.label">
                <el-option v-for="o in g.options" :key="o.value" :label="o.label" :value="o.value" />
              </el-option-group>
            </template>
          </el-select>
        </div>

        <div class="compare-field">
          <span class="compare-field__label">对比指标</span>
          <el-select v-model="metricKey" size="small" class="compare-field__select is-sm">
            <el-option v-for="m in COMPARE_METRICS" :key="m.key" :label="m.label" :value="m.key" />
          </el-select>
        </div>

        <div class="compare-field">
          <span class="compare-field__label">时间范围</span>
          <el-select v-model="range" size="small" class="compare-field__select is-sm">
            <el-option v-for="r in RANGE_OPTIONS" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </div>

        <div class="compare-field">
          <span class="compare-field__label">统计粒度</span>
          <el-select v-model="bucket" size="small" class="compare-field__select is-sm">
            <el-option v-for="b in BUCKET_OPTIONS" :key="b.value" :label="b.label" :value="b.value" />
          </el-select>
        </div>
      </div>

      <div class="compare-hint">
        <el-tag v-if="sameTarget" type="warning" size="small" effect="plain">两端选了同一个对象，曲线会重合</el-tag>
        <span>{{ windowInfo.hint }} · 共 {{ pair.points.length }} 个采样桶（每桶 {{ bucket }} 小时）</span>
      </div>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="12" class="compare-cards">
      <el-col v-for="c in cards" :key="c.title" :xs="24" :sm="12" :lg="6">
        <div class="app-card compare-card">
          <div class="compare-card__title">{{ c.title }}</div>
          <div class="compare-card__row">
            <span class="compare-card__name">{{ c.aName }}</span>
            <b :style="{ color: c.colorA }">{{ c.a }}</b>
          </div>
          <div class="compare-card__row">
            <span class="compare-card__name">{{ c.bName }}</span>
            <b :style="{ color: c.colorB }">{{ c.b }}</b>
          </div>
          <div class="compare-card__foot" :class="c.tone">{{ c.note }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 对比曲线 -->
    <div class="app-card compare-chart-card">
      <CompareChart
        :points="pair.points"
        :series="series"
        :metric="metric"
        :title="`${metric.label} · ${targetA.name} vs ${targetB.name}`"
        :subtitle="`${windowInfo.hint}；粒度 ${bucket} 小时/桶，共 ${pair.points.length} 桶${thresholdNote ? ` · ${thresholdNote}` : ''}`"
        :height="340"
      />
    </div>

    <!-- 统计对照表 -->
    <div class="app-card">
      <div class="table-toolbar">
        <span class="table-toolbar__title">统计对照</span>
        <span class="table-toolbar__count">
          {{ metric.label }} · {{ statA.samples }} 个采样点 · 相关系数 {{ corr }}
        </span>
      </div>
      <div class="table-wrap">
        <el-table :data="statRows" border stripe size="small">
          <el-table-column prop="label" label="统计项" width="150" />
          <el-table-column :label="targetA.name" min-width="150">
            <template #default="{ row }"><span class="stat-a">{{ row.a }}</span></template>
          </el-table-column>
          <el-table-column :label="targetB.name" min-width="150">
            <template #default="{ row }"><span class="stat-b">{{ row.b }}</span></template>
          </el-table-column>
          <el-table-column label="差值（A - B）" width="150">
            <template #default="{ row }">
              <span :class="row.deltaTone">{{ row.delta }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="note" label="说明" min-width="220" show-overflow-tooltip />
        </el-table>
      </div>
    </div>

    <el-alert class="compare-note" type="info" :closable="false" show-icon>
      <template #title>口径说明</template>
      <template #default>
        节点原始采集为 15 分钟/次，这里按所选粒度聚合成桶后比较；仓间口径取该库区内全部垛位的均值。
        曲线为确定性演示数据（同一对象、同一指标、同一时间窗结果稳定），与大屏演示口径一致；
        接入真实历史表后，只需把 <code>data.js</code> 的 <code>buildSeriesPair</code> 换成历史接口即可。
      </template>
    </el-alert>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RefreshRight, Switch } from '@element-plus/icons-vue'
import CompareChart from './CompareChart.vue'
import {
  COMPARE_METRICS, COMPARE_MODES, RANGE_OPTIONS, BUCKET_OPTIONS,
  stackGroups, areaOptions, buildTarget, buildSeriesPair,
  summarize, correlation, metricOf
} from './data'

const COLOR_A = '#2f86d6'
const COLOR_B = '#e08a3c'

const groups = stackGroups()
const areas = areaOptions()

const mode = ref('stack-stack')
const metricKey = ref('temp')
const range = ref('plan')
const bucket = ref(4)
const seed = ref(0)

const aStackId = ref(groups[0].options[0].value)
const bStackId = ref(groups[0].options[1]?.value || groups[1].options[0].value)
const aAreaId = ref(areas[0].value)
const bAreaId = ref(areas[1]?.value || areas[0].value)

const modeDef = computed(() => COMPARE_MODES.find((m) => m.value === mode.value) || COMPARE_MODES[0])
const aKind = computed(() => modeDef.value.aKind)
const bKind = computed(() => modeDef.value.bKind)

/** 用 aId / bId 统一对外，内部按当前模式取对应的选择值 */
const aId = computed({
  get: () => (aKind.value === 'area' ? aAreaId.value : aStackId.value),
  set: (v) => { if (aKind.value === 'area') aAreaId.value = v; else aStackId.value = v }
})
const bId = computed({
  get: () => (bKind.value === 'area' ? bAreaId.value : bStackId.value),
  set: (v) => { if (bKind.value === 'area') bAreaId.value = v; else bStackId.value = v }
})

const targetA = computed(() => buildTarget(aKind.value, aId.value))
const targetB = computed(() => buildTarget(bKind.value, bId.value))
const sameTarget = computed(() => targetA.value.key === targetB.value.key)
const metric = computed(() => metricOf(metricKey.value))

/** 时间窗：计划口径取两端"都已在养护中"的较晚起点，避免一端没数据 */
const windowInfo = computed(() => {
  const now = new Date()
  if (range.value === 'plan') {
    const starts = [targetA.value.planStart, targetB.value.planStart].filter(Boolean).sort()
    const start = starts[starts.length - 1] || ''
    return { start, hint: start ? `时间窗 ${start} 至今（两端养护计划均已开始）` : '养护计划开始至今' }
  }
  const days = range.value === '30d' ? 30 : 7
  const d = new Date(now.getTime() - days * 86400000)
  const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return { start: iso, hint: `近 ${days} 天（${iso} 至今）` }
})

const pair = computed(() => {
  void seed.value
  return buildSeriesPair(targetA.value, targetB.value, metricKey.value, {
    startISO: windowInfo.value.start,
    bucketHours: bucket.value
  })
})

const series = computed(() => [
  { key: 'a', name: targetA.value.name, color: COLOR_A },
  { key: 'b', name: targetB.value.name, color: COLOR_B }
])

const statA = computed(() => summarize(pair.value.points, 'a', metric.value))
const statB = computed(() => summarize(pair.value.points, 'b', metric.value))
const corr = computed(() => correlation(pair.value.points))

/** 阈值口径提示：曲线量程自适应后阈值可能落在可视范围外，这里始终把口径写出来 */
const thresholdNote = computed(() =>
  [metric.value.warnText, metric.value.warnLowText].filter(Boolean).join(' · ')
)

const digits = computed(() => metric.value.digits)
const unit = computed(() => metric.value.unit)

function fmt(v) {
  if (v == null || !Number.isFinite(Number(v))) return '--'
  return Number(v).toFixed(digits.value)
}
function signed(v) {
  if (!Number.isFinite(Number(v))) return '--'
  const s = Number(v)
  return `${s > 0 ? '+' : ''}${s.toFixed(digits.value)}${unit.value}`
}

/** 标准差固定两位小数：整数指标（虫数）用指标位数会全被抹成 0 */
function fmtStd(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '--'
}
function signedStd(v) {
  const n = Number(v)
  if (!Number.isFinite(n)) return '--'
  return `${n > 0 ? '+' : ''}${n.toFixed(2)}`
}

const lastA = computed(() => pair.value.points[pair.value.points.length - 1]?.a)
const lastB = computed(() => pair.value.points[pair.value.points.length - 1]?.b)

const diffAvg = computed(() => statA.value.avg - statB.value.avg)
const verdict = computed(() => {
  const tol = metric.value.tol || 0.3
  const d = diffAvg.value
  if (Math.abs(d) <= tol) return { text: '两端基本持平', tone: 'is-flat' }
  const level = Math.abs(d) > tol * 3 ? '明显' : '略'
  return { text: `A 端${level}${d > 0 ? '高' : '低'}`, tone: d > 0 ? 'is-up' : 'is-down' }
})
const stableSide = computed(() => {
  const a = statA.value.std
  const b = statB.value.std
  const tol = (metric.value.tol || 0.3) * 0.6
  if (Math.abs(a - b) <= tol) return { text: '两端波动接近', tone: 'is-flat' }
  // 只说"谁更平稳"，不评判好坏，所以统一用中性绿色，不用红色制造告警感
  return a < b
    ? { text: 'A 端更平稳', tone: 'is-down' }
    : { text: 'B 端更平稳', tone: 'is-down' }
})

const cards = computed(() => [
  {
    title: `全期均值（${unit.value}）`,
    aName: targetA.value.name,
    bName: targetB.value.name,
    a: `${fmt(statA.value.avg)}`,
    b: `${fmt(statB.value.avg)}`,
    colorA: COLOR_A,
    colorB: COLOR_B,
    tone: verdict.value.tone,
    note: `${verdict.value.text} · 差 ${signed(diffAvg.value)}`
  },
  {
    title: `最新读数（${unit.value}）`,
    aName: targetA.value.name,
    bName: targetB.value.name,
    a: fmt(lastA.value),
    b: fmt(lastB.value),
    colorA: COLOR_A,
    colorB: COLOR_B,
    tone: 'is-flat',
    note: `${pair.value.points[pair.value.points.length - 1]?.label || ''} 时刻`
  },
  {
    title: '波动性（标准差）',
    aName: targetA.value.name,
    bName: targetB.value.name,
    a: fmtStd(statA.value.std),
    b: fmtStd(statB.value.std),
    colorA: COLOR_A,
    colorB: COLOR_B,
    tone: stableSide.value.tone,
    note: stableSide.value.text
  },
  {
    title: '超阈值次数',
    aName: targetA.value.name,
    bName: targetB.value.name,
    a: `${statA.value.warnHits} 次`,
    b: `${statB.value.warnHits} 次`,
    colorA: COLOR_A,
    colorB: COLOR_B,
    tone: 'is-flat',
    note: metric.value.warnText || metric.value.warnLowText || '按阈值口径统计'
  }
])

const diffWarnTone = computed(() => {
  const d = statA.value.warnHits - statB.value.warnHits
  // 任意一端越界更多都值得关注，统一用警示色（不能因为"少的那端"是 A 就显示绿色）
  return d ? 'is-warn' : 'is-flat'
})
const diffWarnNote = computed(() => {
  const d = statA.value.warnHits - statB.value.warnHits
  if (!d) return '两端越界次数相同'
  return d > 0 ? `A 端多 ${d} 次越界` : `B 端多 ${-d} 次越界`
})
const corrText = computed(() => {
  const c = corr.value
  if (c >= 0.7) return '走势高度同步（同一环境驱动）'
  if (c >= 0.3) return '走势部分同步'
  if (c > -0.3) return '走势基本独立'
  return '走势相反，需关注'
})

function signedCount(v) {
  const n = Number(v) || 0
  return `${n > 0 ? '+' : ''}${n} 次`
}

const statRows = computed(() => [
  {
    label: '均值',
    a: `${fmt(statA.value.avg)}${unit.value}`,
    b: `${fmt(statB.value.avg)}${unit.value}`,
    delta: signed(diffAvg.value),
    deltaTone: verdict.value.tone,
    note: verdict.value.text
  },
  {
    label: '最高值',
    a: `${fmt(statA.value.max)}${unit.value}`,
    b: `${fmt(statB.value.max)}${unit.value}`,
    delta: signed(statA.value.max - statB.value.max),
    deltaTone: 'is-flat',
    note: `A 出现于 ${statA.value.maxAt || '--'}；B 出现于 ${statB.value.maxAt || '--'}`
  },
  {
    label: '最低值',
    a: `${fmt(statA.value.min)}${unit.value}`,
    b: `${fmt(statB.value.min)}${unit.value}`,
    delta: signed(statA.value.min - statB.value.min),
    deltaTone: 'is-flat',
    note: `A 出现于 ${statA.value.minAt || '--'}；B 出现于 ${statB.value.minAt || '--'}`
  },
  {
    label: '标准差（波动）',
    a: fmtStd(statA.value.std),
    b: fmtStd(statB.value.std),
    delta: signedStd(statA.value.std - statB.value.std),
    deltaTone: stableSide.value.tone,
    note: stableSide.value.text
  },
  {
    label: '超阈值次数',
    a: `${statA.value.warnHits} 次`,
    b: `${statB.value.warnHits} 次`,
    delta: signedCount(statA.value.warnHits - statB.value.warnHits),
    deltaTone: diffWarnTone.value,
    note: diffWarnNote.value
  },
  {
    label: '相关系数',
    a: '—',
    b: '—',
    delta: String(corr.value),
    deltaTone: 'is-flat',
    note: corrText.value
  }
])

function switchMode(v) {
  mode.value = v
  // 垛位↔仓间：B 端默认选该垛位所属的仓间，符合"与所在仓间对比"的直觉
  if (v === 'stack-area') {
    const g = groups.find((x) => x.options.some((o) => o.value === aStackId.value))
    const areaId = g?.options?.[0]?.areaId
    if (areaId != null && areas.some((o) => o.value === areaId)) bAreaId.value = areaId
  }
  // 仓间↔仓间：避免两端默认同一个库区
  if (v === 'area-area' && aAreaId.value === bAreaId.value) {
    const other = areas.find((o) => o.value !== aAreaId.value)
    if (other) bAreaId.value = other.value
  }
}

function swap() {
  if (aKind.value !== bKind.value) return
  const a = aId.value
  const b = bId.value
  aId.value = b
  bId.value = a
}

function reload() {
  seed.value += 1
}
</script>

<style scoped>
.compare-page { display: flex; flex-direction: column; gap: 12px; }
.compare-page .app-card { margin-bottom: 0; }

.function-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; gap: 12px;
}
.function-head__text h2 { margin: 0; font-size: 16px; font-weight: 600; }
.function-head__text p { margin: 4px 0 0; font-size: 12px; color: var(--el-text-color-secondary); line-height: 1.6; }

/* 对比模式 */
.compare-toolbar { padding: 14px 16px; display: flex; flex-direction: column; gap: 12px; }
.compare-modes { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.compare-mode {
  text-align: left; cursor: pointer; font-family: inherit;
  padding: 10px 12px; border-radius: 6px;
  border: 1px solid var(--el-border-color); background: var(--el-fill-color-blank);
  display: flex; flex-direction: column; gap: 3px;
  transition: all 0.15s ease;
}
.compare-mode:hover { border-color: var(--el-color-primary-light-5); }
.compare-mode.is-on {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: inset 0 0 0 1px var(--el-color-primary-light-7);
}
.compare-mode__label { font-size: 13px; font-weight: 600; color: var(--el-text-color-primary); }
.compare-mode__desc { font-size: 11px; color: var(--el-text-color-secondary); }

/* 筛选条 */
.compare-filters { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.compare-field { display: flex; align-items: center; gap: 6px; }
.compare-field__label { font-size: 12px; color: var(--el-text-color-regular); white-space: nowrap; }
.compare-field__select { width: 210px; }
.compare-field__select.is-sm { width: 140px; }
.compare-swap { flex: none; }
.compare-hint {
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
  font-size: 11.5px; color: var(--el-text-color-secondary);
}

/* 概览卡片 */
.compare-cards { margin: 0 !important; row-gap: 12px; }
.compare-cards .el-col { margin-bottom: 0; }
.compare-card { padding: 12px 14px; display: flex; flex-direction: column; gap: 6px; height: 100%; }
.compare-card__title { font-size: 12px; color: var(--el-text-color-secondary); }
.compare-card__row { display: flex; align-items: baseline; justify-content: space-between; gap: 8px; }
.compare-card__name {
  font-size: 12px; color: var(--el-text-color-regular);
  max-width: 62%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.compare-card__row b { font-size: 16px; font-variant-numeric: tabular-nums; }
.compare-card__foot { font-size: 11px; color: var(--el-text-color-secondary); margin-top: 2px; }
.compare-card__foot.is-up { color: #c4562f; }
.compare-card__foot.is-down { color: #2f7d63; }
.compare-card__foot.is-flat { color: var(--el-text-color-secondary); }

.compare-chart-card { padding: 14px 16px 6px; }
.compare-note { margin-top: 0; }
.compare-note code {
  background: var(--el-fill-color); border-radius: 3px; padding: 0 3px;
  font-size: 11px;
}
.stat-a { color: #2f86d6; font-variant-numeric: tabular-nums; }
.stat-b { color: #e08a3c; font-variant-numeric: tabular-nums; }
.is-up { color: #c4562f; }
.is-down { color: #2f7d63; }
.is-warn { color: #b26a00; }
.is-flat { color: var(--el-text-color-regular); }

@media (max-width: 900px) {
  .compare-modes { grid-template-columns: 1fr; }
  .compare-field__select { width: 160px; }
}
</style>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import StackCard from './components/StackCard.vue'
import TrendChart from './components/TrendChart.vue'
import {
  buildDemoModel, enrichWithReal, tickModel, buildNodeHistory, buildTypeAvgSeries,
  countNodesByType, metricsOfType, NODE_TYPE_METRICS,
  AREA_DEFS, HEALTH_DICT, PHASE_SEQUENCE, SCREEN_DATA_VERSION
} from './mockData'

/** 下方「全部库区平均」趋势的三个通道 */
const AREA_BANDS = [
  { key: 'temp', label: '温度', unit: '℃', color: '#f0a25a', digits: 1, warn: 30, warnText: '阈值 30℃', minRange: 1.6 },
  { key: 'humidity', label: '相对湿度', unit: '%', color: '#4fb8f0', digits: 1, warn: 70, warnText: '阈值 70%', minRange: 3 },
  {
    key: 'moisture', label: '含水率', unit: '%', color: '#3ddc97', digits: 2,
    warn: 13, warnLow: 11, warnText: '阈值 13%', warnLowText: '下限 11%', minRange: 0.5
  }
]

const model = ref(buildDemoModel())
const activeAreaId = ref(AREA_DEFS[0].id)
const sourceMode = ref('demo')
const loading = ref(false)
const autoRefresh = ref(true)
const now = ref(new Date())
const drawerVisible = ref(false)
const selectedStack = ref(null)
const toast = ref('')
const rightTab = ref('alarm')
const nodeDrawerVisible = ref(false)
const selectedNode = ref(null)
const nodePlanStart = ref('')
const nodeRange = ref('24h')
let clockTimer = null
let tickTimer = null
let toastTimer = null

const router = useRouter()

/** 点击告警条目：跳转到预警中心（带 nodeId 预筛选） */
function goWarning(w) {
  router.push({ path: '/function/warning', query: w?.nodeId ? { nodeId: w.nodeId } : {} })
}

function notify(msg) {
  toast.value = msg
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value = '' }, 3200)
}

const activeArea = computed(() => model.value.areas.find((a) => a.id === activeAreaId.value) || null)

const visibleStacks = computed(() => {
  const list = activeAreaId.value === 'all'
    ? model.value.stacks
    : model.value.stacks.filter((s) => s.areaId === activeAreaId.value)
  return [...list].sort((a, b) => b.alertLevel - a.alertLevel || a.code.localeCompare(b.code))
})

const kpis = computed(() => {
  const list = visibleStacks.value
  const totalNodes = list.reduce((n, s) => n + s.totalNodes, 0)
  const onlineNodes = list.reduce((n, s) => n + s.onlineNodes, 0)
  const avg = (key) => list.length ? (list.reduce((n, s) => n + s.metrics[key], 0) / list.length).toFixed(key === 'moisture' ? 2 : 1) : '--'
  const warnings = list.reduce((n, s) => n + s.warnings.length, 0)
  const pests = list.reduce((n, s) => n + s.metrics.pestTotal, 0)
  return [
    { key: 'plans', label: '在养计划', value: list.length, unit: '个', sub: `${list.filter((s) => s.plan.progress >= 70).length} 个进入养护后期`, tone: '#3ddc97' },
    { key: 'nodes', label: '在线节点', value: `${onlineNodes}/${totalNodes}`, unit: '', sub: `覆盖 ${list.length} 个垛位`, tone: '#4fb8f0' },
    { key: 'temp', label: '平均温度', value: avg('temp'), unit: '℃', sub: '阈值 ≥ 30℃ 触发红色', tone: '#f0a25a' },
    { key: 'humidity', label: '平均相对湿度', value: avg('humidity'), unit: '%', sub: '阈值 ≥ 70% 触发红色', tone: '#4fb8f0' },
    { key: 'moisture', label: '平均含水率', value: avg('moisture'), unit: '%', sub: '安全区间 11 ~ 13%', tone: '#9ad9b6' },
    { key: 'pest', label: '虫情（诱捕）', value: pests, unit: '只', sub: '本库累计计数', tone: '#e8c55a' },
    { key: 'alarm', label: '活跃告警', value: warnings, unit: '处', sub: '本库异常垛位', tone: '#f2665c' }
  ]
})

/** 趋势取当前可见垛位的逐点平均（真实 24 小时、15 分钟粒度、96 点） */
const trendPoints = computed(() => {
  const list = visibleStacks.value
  if (!list.length) return []
  // 温湿度类（温度/湿度）与含水率类设备分开取序列，再按同一时间轴合并
  const envSeries = list.map((s) => buildNodeHistory({ nodeId: s.id, nodeType: '02', metrics: s.metrics }, '', '24h'))
  const moistSeries = list.map((s) => buildNodeHistory({ nodeId: `${s.id}-M`, nodeType: '05', metrics: s.metrics }, '', '24h'))
  const len = envSeries[0].length
  const out = []
  for (let i = 0; i < len; i++) {
    let t = 0
    let h = 0
    let m = 0
    let c = 0
    let cm = 0
    envSeries.forEach((arr) => {
      const p = arr[i]
      if (!p) return
      t += p.temp
      h += p.humidity
      c++
    })
    moistSeries.forEach((arr) => {
      const p = arr[i]
      if (!p) return
      m += p.moisture
      cm++
    })
    const p0 = envSeries[0][i]
    out.push({
      ts: p0.ts,
      label: p0.label,
      temp: c ? Number((t / c).toFixed(1)) : null,
      humidity: c ? Number((h / c).toFixed(1)) : null,
      moisture: cm ? Number((m / cm).toFixed(2)) : null
    })
  }
  return out
})

const phaseDistribution = computed(() => PHASE_SEQUENCE.map((p, i) => ({
  name: p,
  count: visibleStacks.value.filter((s) => s.plan.phases === p).length,
  color: ['#3ddc97', '#4fb8f0', '#e8c55a', '#b58cf0'][i]
})))

const clockText = computed(() => now.value.toLocaleString('zh-CN', { hour12: false }))

/** 右侧栏：聚合当前视图内垛位的预警（按等级倒序） */
const aggregatedWarnings = computed(() =>
  visibleStacks.value
    .flatMap((s) => s.warnings.map((w) => ({ ...w, areaName: s.areaName, stackCode: s.code })))
    .sort((a, b) => b.warningGrade - a.warningGrade)
)
/** 右侧栏：日志流 */
const logFeed = computed(() => model.value.feed.filter((f) => f.type === 'log'))

/* ---------- 设备（节点）详情：按设备类型显示对应数据 ---------- */
/** 当前设备的类型配置：采集哪些指标、单位、阈值 */
const nodeTypeCfg = computed(() => metricsOfType(selectedNode.value?.nodeType))
const nodeBands = computed(() => nodeTypeCfg.value.metrics)
const nodeHistory = computed(() => (
  selectedNode.value ? buildNodeHistory(selectedNode.value, nodePlanStart.value, nodeRange.value) : []
))
const nodeLast = computed(() => nodeHistory.value[nodeHistory.value.length - 1] || {})
const nodeSubtitle = computed(() => {
  const b = nodeBands.value.map((x) => `${x.label}(${x.unit})`).join(' / ')
  return nodeRange.value === 'plan'
    ? `${b} · ${nodePlanStart.value} → 现在 · ${nodeHistory.value.length} 个采样点（15 分钟/次）`
    : `${b} · 最近 24 小时 · ${nodeHistory.value.length} 个采样点（15 分钟/次）`
})
const nodeKpis = computed(() => {
  const list = nodeBands.value.map((b) => ({ n: b.label, v: nodeLast.value[b.key], u: b.unit, tone: b.color }))
  list.push({ n: '采样点位数', v: nodeHistory.value.length, u: '点', tone: '#7fb2cf' })
  list.push({ n: '设备状态', v: selectedNode.value?.statusLabel || '--', u: '', tone: '#7fb2cf' })
  return list
})

/* ---------- 垛位详情：同类型设备按 1/2/4/8/12 小时分桶的均值 ---------- */
const bucketHours = ref(4)
const bucketOptions = [1, 2, 4, 8, 12]
const bucketOpen = ref(false)
const avgType = ref('02')
const typeCounts = computed(() => countNodesByType(model.value))
const typeOptions = computed(() => Object.keys(NODE_TYPE_METRICS)
  .map((t) => ({ type: t, label: NODE_TYPE_METRICS[t].label, count: typeCounts.value[t] || 0 }))
  .filter((t) => t.count > 0))
const avgBands = computed(() => metricsOfType(avgType.value).metrics)
const avgData = computed(() => (
  selectedStack.value ? buildTypeAvgSeries(model.value, selectedStack.value, avgType.value, bucketHours.value) : null
))

function pickBucket(h) {
  bucketHours.value = h
  bucketOpen.value = false
}

function onDocClick() { bucketOpen.value = false }

function selectArea(id) {
  activeAreaId.value = id
  drawerVisible.value = false
}

function openStack(stack) {
  selectedStack.value = stack
  drawerVisible.value = true
  bucketOpen.value = false
  const hasEnv = (stack.nodes || []).some((n) => String(n.nodeType) === '02')
  avgType.value = hasEnv ? '02' : String(stack.nodes?.[0]?.nodeType || '02')
}

function openNode(node, planStart) {
  // 把垛位的实时指标一并带进来，设备曲线才与该垛位的读数一致
  selectedNode.value = { ...node, metrics: selectedStack.value?.metrics || {} }
  nodePlanStart.value = planStart
  nodeRange.value = '24h'
  nodeDrawerVisible.value = true
}

async function refresh() {
  loading.value = true
  try {
    const fresh = buildDemoModel()
    if (sourceMode.value === 'real') {
      const { stats } = await enrichWithReal(fresh)
      notify(stats.error
        ? (/401|403/.test(stats.error)
          ? '未登录或令牌失效：请先登录系统再切换真实接口，当前仍使用常驻演示数据'
          : `真实接口未就绪（${stats.error}），已使用常驻演示数据`)
        : `已注入真实数据：${stats.realPlans} 个计划 / ${stats.realNodes} 个节点 / ${stats.realAreas} 个仓库`)
    }
    model.value = fresh
    now.value = new Date()
  } finally {
    loading.value = false
  }
}

async function switchSource(mode) {
  if (mode === sourceMode.value) return
  sourceMode.value = mode
  await refresh()
}

/** 演练：把一座正常垛位推到红色，验证分级配色、预警与右侧告警流 */
function injectAnomaly() {
  const target = visibleStacks.value.find((s) => s.health === 'normal')
  if (!target) { notify('当前库区没有可注入的正常垛位'); return }
  target.metrics.temp = 30.6
  target.metrics.humidity = 71.2
  target.metrics.pestTotal = 4
  target.health = 'danger'
  target.alertLevel = 3
  target.warnings = [{
    id: `W-INJECT-${target.id}`, warningType: '08', typeLabel: '垛内温度', kind: '环境',
    warningGrade: 3, nodeId: target.nodes[0]?.nodeId || '-', stackId: target.stackId,
    isAffirm: '2', time: new Date().toLocaleString('zh-CN', { hour12: false })
  }]
  notify(`已向 ${target.areaName} ${target.code} 注入异常：温度 30.6℃ / 湿度 71.2%，红色预警生效`)
}

function toggleDevice(d) {
  d.running = !d.running
  d.status = d.running ? '运行中' : '已停止'
  notify(`${d.name} 已${d.status}（演示控制，未写入设备）`)
}

function toggleFullscreen() {
  const el = document.documentElement
  if (document.fullscreenElement) document.exitFullscreen()
  else el.requestFullscreen?.()
}

function toggleAuto() {
  autoRefresh.value = !autoRefresh.value
  notify(autoRefresh.value ? '已开启自动刷新（15 秒）' : '已暂停自动刷新')
}

onMounted(() => {
  // 便于测试与直接分享：?area=all|201|202|203、?stack=A1垛、?inject=1
  const q = new URLSearchParams(window.location.search)
  const area = q.get('area')
  if (area) activeAreaId.value = area === 'all' ? 'all' : (Number(area) || activeAreaId.value)
  const stackKey = q.get('stack')
  if (stackKey) {
    const hit = model.value.stacks.find((s) => s.code === stackKey || s.id === stackKey || s.code.startsWith(stackKey))
    if (hit) openStack(hit)
  }
  if (q.get('inject') === '1') injectAnomaly()

  clockTimer = setInterval(() => { now.value = new Date() }, 1000)
  tickTimer = setInterval(() => {
    if (autoRefresh.value) tickModel(model.value)
  }, 15000)
  document.addEventListener('click', onDocClick)
})

onUnmounted(() => {
  clearInterval(clockTimer)
  clearInterval(tickTimer)
  clearTimeout(toastTimer)
  document.removeEventListener('click', onDocClick)
})
</script>

<template>
  <div class="screen">
    <!-- 顶栏 -->
    <header class="scr-head">
      <div class="scr-head__left">
        <span class="scr-logo">叶</span>
        <div>
          <h1>烟叶养护 · 节点当前养护计划监控大屏</h1>
          <p>CURRENT MAINTAIN PLAN &amp; NODE TELEMETRY · {{ activeArea ? activeArea.name : '全部库区' }}</p>
        </div>
      </div>

      <div class="scr-head__right">
        <div class="scr-source">
          <button :class="{ on: sourceMode === 'demo' }" @click="switchSource('demo')">演示数据</button>
          <button :class="{ on: sourceMode === 'real' }" @click="switchSource('real')">真实接口</button>
        </div>
        <span class="scr-clock">{{ clockText }}</span>
        <button class="scr-btn" :class="{ active: autoRefresh }" title="自动刷新" @click="toggleAuto">自动</button>
        <button class="scr-btn" :disabled="loading" @click="refresh">刷新</button>
        <button class="scr-btn scr-btn--warn" @click="injectAnomaly">注入异常</button>
        <button class="scr-btn" @click="toggleFullscreen">全屏</button>
        <a class="scr-btn" href="/">返回</a>
      </div>
    </header>

    <div class="scr-body">
      <!-- 左：库区列表 -->
      <aside class="scr-left panel">
        <div class="panel__title">库区列表</div>
        <button
          class="area-item" :class="{ on: activeAreaId === 'all' }"
          @click="selectArea('all')"
        >
          <span class="area-item__dot" style="background:#4fb8f0" />
          <span class="area-item__name">全部库区</span>
          <span class="area-item__meta">{{ model.stacks.length }} 垛 · {{ model.stacks.reduce((n, s) => n + s.warnings.length, 0) }} 处告警</span>
        </button>
        <button
          v-for="a in model.areas" :key="a.id"
          class="area-item" :class="{ on: activeAreaId === a.id }"
          @click="selectArea(a.id)"
        >
          <span class="area-item__dot" :style="{ background: a.warningCount ? '#e8c55a' : '#3ddc97' }" />
          <span class="area-item__name">{{ a.name }}</span>
          <span class="area-item__meta">{{ a.warningCount }} 处异常 · {{ a.avgTemp }}℃ / {{ a.avgHumidity }}%</span>
          <span class="area-item__code">{{ a.code }} · {{ a.desc }}</span>
        </button>

        <div class="left-phase">
          <div class="left-phase__title">当前养护阶段分布</div>
          <div v-for="p in phaseDistribution" :key="p.name" class="left-phase__row">
            <div class="left-phase__label"><span>{{ p.name }}</span><b>{{ p.count }}</b></div>
            <div class="left-phase__bar">
              <i :style="{ width: `${visibleStacks.length ? (p.count / visibleStacks.length) * 100 : 0}%`, background: p.color }" />
            </div>
          </div>
        </div>

        <div class="scr-left__note">
          点击库区切换视图；色块越偏红代表环境风险越高。<br />
          垛位卡片展示该垛<strong>当前养护计划</strong>的阶段、进度与其节点实时数据。
        </div>

        <div class="scr-left__version">
          数据集 v{{ SCREEN_DATA_VERSION }} · {{ model.stacks.length }} 垛 / {{ model.stacks.reduce((n, s) => n + s.totalNodes, 0) }} 节点<br />
          数据源：{{ sourceMode === 'real' ? '网关真实接口 + 演示补齐' : '常驻演示数据（保留）' }}
        </div>
      </aside>

      <!-- 中：指标 + 垛位矩阵 + 趋势/设备 -->
      <main class="scr-main">
        <section class="kpis">
          <div v-for="k in kpis" :key="k.key" class="kpi" :style="{ '--kpi-tone': k.tone }">
            <div class="kpi__label">{{ k.label }}</div>
            <div class="kpi__value">{{ k.value }}<small>{{ k.unit }}</small></div>
            <div class="kpi__sub">{{ k.sub }}</div>
          </div>
        </section>

        <section class="panel matrix">
          <div class="panel__head">
            <div>
              <span class="panel__title">{{ activeArea ? activeArea.name : '全部库区' }} · 节点当前养护计划（共 {{ visibleStacks.length }} 垛）</span>
              <span class="panel__sub">物料 / 养护方式 / 当前阶段 / 养护进度 / 节点实时值</span>
            </div>
            <div class="legend">
              <span v-for="(v, k) in HEALTH_DICT" :key="k"><i :style="{ background: v.color }" />{{ v.label }}</span>
            </div>
          </div>
          <div class="matrix__grid">
            <StackCard
              v-for="s in visibleStacks" :key="s.id" :stack="s"
              :selected="selectedStack?.id === s.id" @select="openStack"
            />
          </div>
        </section>

        <section class="scr-bottom">
          <div class="panel">
            <TrendChart
              :points="trendPoints"
              :bands="AREA_BANDS"
              :title="`温湿度 · 含水率趋势 · ${activeArea ? activeArea.name : '全部库区'} 平均`"
              :subtitle="`${visibleStacks.length} 个垛位逐点平均 · 15 分钟粒度 · 最近 24 小时（96 点）`"
              :band-height="50"
            />
          </div>

          <div class="panel devices">
            <div class="panel__title">设备状态与控制</div>
            <button
              v-for="d in model.devices" :key="d.id"
              class="device" @click="toggleDevice(d)"
            >
              <span class="device__icon">{{ d.icon === 'drop' ? '💧' : d.icon === 'cool' ? '❄' : '🌀' }}</span>
              <span class="device__text">
                <b>{{ d.name }}</b>
                <small>{{ d.status }} · {{ d.detail }}</small>
              </span>
              <span class="switch" :class="{ on: d.running }"><i /></span>
            </button>

          </div>
        </section>
      </main>

      <!-- 右：告警 / 日志（按需求仅保留，不含 Agent） -->
      <aside class="scr-right panel">
        <div class="scr-tabs">
          <button :class="{ on: rightTab === 'alarm' }" @click="rightTab = 'alarm'">告警</button>
          <button :class="{ on: rightTab === 'log' }" @click="rightTab = 'log'">日志</button>
        </div>

        <div v-if="rightTab === 'alarm'" class="feed">
          <div v-for="w in aggregatedWarnings" :key="w.id" class="feed__item feed__item--click" title="点击进入预警中心" @click="goWarning(w)">
            <span class="feed__tag is-alarm">{{ w.warningGrade }}级</span>
            <div class="feed__body">
              <div class="feed__title">{{ w.areaName }} · {{ w.stackCode }}</div>
              <div class="feed__text">{{ w.typeLabel }} · {{ w.kind }}类 · 节点 {{ w.nodeId }}</div>
              <div class="feed__time">{{ w.time }}</div>
            </div>
          </div>
          <div v-if="!aggregatedWarnings.length" class="feed__empty">当前视图无告警</div>
        </div>

        <div v-else class="feed">
          <div v-for="f in logFeed" :key="f.id" class="feed__item">
            <span class="feed__tag is-log">{{ f.tag }}</span>
            <div class="feed__body">
              <div class="feed__title">{{ f.title }}</div>
              <div class="feed__text">{{ f.body }}</div>
              <div class="feed__time">{{ f.time }}</div>
            </div>
          </div>
          <div v-if="!logFeed.length" class="feed__empty">暂无日志</div>
        </div>

        <div class="scr-right__foot">
          共 {{ model.stacks.length }} 垛 / {{ model.stacks.reduce((n, s) => n + s.totalNodes, 0) }} 节点<br />
          数据源：{{ sourceMode === 'real' ? '网关真实接口 + 演示补齐' : '常驻演示数据（保留）' }}
        </div>
      </aside>
    </div>

    <!-- 垛位详情：当前养护计划 + 节点清单 -->
    <div v-if="drawerVisible" class="drawer-mask" @click="drawerVisible = false" />
    <aside v-if="drawerVisible && selectedStack" class="drawer">
      <div class="drawer__head">
        <div>
          <h2>{{ selectedStack.areaName }} · {{ selectedStack.code }}</h2>
          <p>垛位 ID {{ selectedStack.stackId }} · {{ selectedStack.pidRoute }}</p>
        </div>
        <button class="drawer__close" @click="drawerVisible = false">×</button>
      </div>

      <div class="drawer__section">
        <h3>当前养护计划</h3>
        <div class="kv-grid">
          <div><span>计划编号</span><b>{{ selectedStack.plan.id }}</b></div>
          <div><span>物料编码</span><b>{{ selectedStack.plan.materialCode }}</b></div>
          <div><span>物料名称</span><b>{{ selectedStack.plan.materialName }}</b></div>
          <div><span>养护方式</span><b>{{ selectedStack.plan.styleLabel }} ({{ selectedStack.plan.maintenceStyle }})</b></div>
          <div><span>当前阶段</span><b>{{ selectedStack.plan.phases }}</b></div>
          <div><span>阶段 ID</span><b>{{ selectedStack.plan.stageId }}</b></div>
          <div><span>养护起始</span><b>{{ selectedStack.plan.startDate }}</b></div>
          <div><span>负责人</span><b>{{ selectedStack.plan.principal }}</b></div>
          <div><span>已养 / 计划</span><b>{{ selectedStack.plan.elapsedDays }} / {{ selectedStack.plan.planDays }} 天</b></div>
          <div><span>完成进度</span><b>{{ selectedStack.plan.progress }}%</b></div>
          <div><span>计划状态</span><b>{{ selectedStack.plan.isEnd === '1' ? '已完成' : '进行中' }}</b></div>
          <div><span>数据来源</span><b>{{ selectedStack.plan.source === 'real' ? '网关真实接口' : '演示数据' }}</b></div>
        </div>
        <div class="drawer__progress"><i :style="{ width: `${selectedStack.plan.progress}%`, background: HEALTH_DICT[selectedStack.health].color }" /></div>
      </div>

      <div class="drawer__section">
        <h3>实时环境指标 <span class="drawer__tone" :style="{ color: HEALTH_DICT[selectedStack.health].color }">{{ HEALTH_DICT[selectedStack.health].label }}</span></h3>
        <div class="metric-grid">
          <div v-for="m in [
            { n: '温度', v: selectedStack.metrics.temp, u: '℃' },
            { n: '相对湿度', v: selectedStack.metrics.humidity, u: '%' },
            { n: '含水率', v: selectedStack.metrics.moisture, u: '%' },
            { n: '氧气浓度', v: selectedStack.metrics.oxygen, u: '%' },
            { n: '包芯温度', v: selectedStack.metrics.coreTemp, u: '℃' },
            { n: '虫情累计', v: selectedStack.metrics.pestTotal, u: '只' }
          ]" :key="m.n">
            <span>{{ m.n }}</span><b>{{ m.v }}<small>{{ m.u }}</small></b>
          </div>
        </div>
      </div>

      <div class="drawer__section">
        <h3>同类型设备均值 <span class="drawer__tone">当前养护计划开始至今</span></h3>

        <div class="avg-bar">
          <div class="avg-types">
            <button
              v-for="t in typeOptions" :key="t.type"
              class="avg-chip" :class="{ on: avgType === t.type }"
              @click="avgType = t.type"
            >{{ t.label }}<b>{{ t.count }}</b></button>
          </div>
          <div class="avg-select" @click.stop>
            <span class="avg-select__label">统计粒度</span>
            <button class="avg-select__btn" :class="{ on: bucketOpen }" @click="bucketOpen = !bucketOpen">
              {{ bucketHours }} 小时<i>▾</i>
            </button>
            <div v-if="bucketOpen" class="avg-select__panel">
              <button
                v-for="h in bucketOptions" :key="h" :class="{ on: h === bucketHours }"
                @click="pickBucket(h)"
              >{{ h }} 小时</button>
            </div>
          </div>
        </div>

        <template v-if="avgData">
          <p class="avg-hint">
            统计口径：全部库区中与当前垛位<strong>同类（{{ avgData.label }}）</strong>的设备共
            <strong>{{ avgData.count }}</strong> 台，分布在 {{ avgData.owners }} 个垛位；
            自 {{ avgData.startDate }} 至今按 {{ avgData.bucketHours }} 小时分桶，每桶聚合
            {{ avgData.sampleCount }} 条 15 分钟采样。
          </p>

          <div class="avg-stats">
            <div><span>同类型设备</span><b>{{ avgData.count }}<small>台</small></b><em>{{ avgData.owners }} 个垛位</em></div>
            <div><span>{{ avgBands[0].label }}均值</span><b>{{ avgData.avg }}<small>{{ avgBands[0].unit }}</small></b><em>全期 {{ avgData.pointCount }} 桶</em></div>
            <div><span>最高桶</span><b>{{ avgData.max }}<small>{{ avgBands[0].unit }}</small></b><em>{{ avgData.maxAt }}</em></div>
            <div><span>最低桶</span><b>{{ avgData.min }}<small>{{ avgBands[0].unit }}</small></b><em>{{ avgData.minAt }}</em></div>
          </div>

          <TrendChart
            :points="avgData.points"
            :bands="avgBands"
            :title="`${avgData.label} · 同类型设备均值`"
            :subtitle="`${avgData.startDate} → 现在 · 每个点 = ${bucketHours} 小时均值（共 ${avgData.pointCount} 个时间桶）`"
            :band-height="avgBands.length > 1 ? 58 : 96"
          />
        </template>
      </div>

      <div class="drawer__section">
        <h3>该计划下的节点（{{ selectedStack.onlineNodes }}/{{ selectedStack.totalNodes }} 在线）</h3>
        <div class="node-table-wrap">
          <table class="node-table">
            <thead>
              <tr><th>节点编号</th><th>类型</th><th>状态</th><th>设备箱号</th><th>X/Y/Z</th><th>实时值</th><th>最后上报</th></tr>
            </thead>
            <tbody>
              <tr v-for="n in selectedStack.nodes" :key="n.nodeId">
                <td><span class="node-link" @click="openNode(n, selectedStack.plan.startDate)">{{ n.nodeId }}</span></td>
                <td>{{ n.typeLabel }}</td>
                <td :style="{ color: n.status === '1' ? '#3ddc97' : '#f2665c' }">{{ n.statusLabel }}</td>
                <td>{{ n.caseNo }}</td>
                <td>{{ n.siteX }}/{{ n.siteY }}/{{ n.siteZ }}</td>
                <td>{{ n.value }}</td>
                <td>{{ n.lastReport }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="selectedStack.warnings.length" class="drawer__section">
        <h3>该垛位预警（{{ selectedStack.warnings.length }}）</h3>
        <div v-for="w in selectedStack.warnings" :key="w.id" class="drawer__warn">
          <span :style="{ background: HEALTH_DICT[selectedStack.health].color }" />
          {{ w.typeLabel }} · {{ w.warningGrade }} 级 · {{ w.kind }}类 · 节点 {{ w.nodeId }} · {{ w.time }}
        </div>
      </div>
    </aside>

    <!-- 设备详情：按设备类型显示其实际采集的指标 -->
    <div v-if="nodeDrawerVisible" class="drawer-mask" @click="nodeDrawerVisible = false" />
    <aside v-if="nodeDrawerVisible && selectedNode" class="drawer">
      <div class="drawer__head">
        <div>
          <h2>{{ selectedNode.nodeId }} · {{ nodeTypeCfg.label }}</h2>
          <p>设备箱号 {{ selectedNode.caseNo }} · 坐标 {{ selectedNode.siteX }}/{{ selectedNode.siteY }}/{{ selectedNode.siteZ }} · {{ selectedNode.statusLabel }}</p>
        </div>
        <button class="drawer__close" @click="nodeDrawerVisible = false">×</button>
      </div>

      <div class="drawer__section">
        <h3>设备与采集指标</h3>
        <div class="kv-grid">
          <div><span>设备类型</span><b>{{ nodeTypeCfg.label }}（{{ selectedNode.nodeType }}）</b></div>
          <div><span>采集指标</span><b>{{ nodeBands.map((b) => `${b.label} ${b.unit}`).join(' / ') }}</b></div>
          <div><span>上报频率</span><b>15 分钟 / 次</b></div>
          <div><span>阈值口径</span><b>{{ nodeBands.map((b) => b.warnText || b.warnLowText).filter(Boolean).join(' / ') || '—' }}</b></div>
        </div>
        <p class="node-desc">{{ nodeTypeCfg.desc }}</p>
      </div>

      <div class="drawer__section">
        <div class="node-range">
          <span class="node-range__label">数据范围</span>
          <button :class="{ on: nodeRange === '24h' }" @click="nodeRange = '24h'">近 24 小时</button>
          <button :class="{ on: nodeRange === 'plan' }" @click="nodeRange = 'plan'">养护计划开始至今</button>
        </div>
        <p class="node-range__hint">
          <template v-if="nodeRange === 'plan'">范围：{{ nodePlanStart }} → 现在（长跨度按最多 240 点采样展示）</template>
          <template v-else>范围：最近 24 小时（96 个采样点）</template>
        </p>
      </div>

      <div class="drawer__section">
        <TrendChart
          :points="nodeHistory"
          :bands="nodeBands"
          :title="`${selectedNode.nodeId} · ${nodeTypeCfg.label}数据趋势`"
          :subtitle="nodeSubtitle"
          :band-height="nodeBands.length > 1 ? 66 : 118"
        />
      </div>

      <div class="drawer__section">
        <h3>最新读数</h3>
        <div class="metric-grid metric-grid--auto">
          <div v-for="m in nodeKpis" :key="m.n">
            <span>{{ m.n }}</span>
            <b :style="{ color: m.tone }">{{ m.v ?? '--' }}<small>{{ m.u }}</small></b>
          </div>
        </div>
      </div>
    </aside>

    <div v-if="toast" class="scr-toast">{{ toast }}</div>
  </div>
</template>

<style scoped>
.screen {
  min-height: 100vh;
  background: radial-gradient(1200px 600px at 20% -10%, #123246 0%, #081521 60%, #06111b 100%);
  color: #d5e6f2;
  font-family: 'PingFang SC', 'Microsoft YaHei', system-ui, sans-serif;
  display: flex;
  flex-direction: column;
  padding-bottom: 12px;
}

/* 顶栏 */
.scr-head {
  display: flex; align-items: center; justify-content: space-between;
  gap: 16px; padding: 12px 18px 14px; flex-wrap: wrap;
}
.scr-head__left { display: flex; align-items: center; gap: 12px; }
.scr-logo {
  width: 38px; height: 38px; border-radius: 10px; flex: none;
  display: grid; place-items: center; font-size: 19px; font-weight: 700;
  color: #06111b; background: linear-gradient(140deg, #4fe0a8, #2b9ad1);
}
.scr-head h1 { margin: 0; font-size: 19px; font-weight: 650; letter-spacing: .5px; color: #eaf6ff; }
.scr-head p { margin: 3px 0 0; font-size: 10px; color: #5f8ba3; letter-spacing: 1.2px; }
.scr-head__right { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.scr-clock { font-size: 12px; color: #8fb0c6; font-variant-numeric: tabular-nums; margin-right: 4px; }
.scr-source { display: flex; border: 1px solid #24405a; border-radius: 7px; overflow: hidden; }
.scr-source button {
  border: 0; background: transparent; color: #7e9cb2; padding: 6px 12px;
  font-size: 11px; cursor: pointer; font-family: inherit;
}
.scr-source button.on { background: #1c4c46; color: #6ff0bd; }
.scr-btn {
  border: 1px solid #24405a; background: #102332; color: #a9c6d8;
  border-radius: 7px; padding: 6px 11px; font-size: 11px; cursor: pointer;
  font-family: inherit; text-decoration: none; line-height: 1.2;
}
.scr-btn:hover { border-color: #3d7fa5; color: #eaf6ff; }
.scr-btn.active { color: #6ff0bd; border-color: #2f7d63; }
.scr-btn--warn { color: #f0b06a; border-color: #6a4a2a; }
.scr-btn:disabled { opacity: .5; cursor: wait; }

/* 主体三栏 */
.scr-body {
  display: grid; grid-template-columns: 186px minmax(0, 1fr) 250px;
  gap: 12px; padding: 0 14px; flex: 1; align-items: start;
}
.panel {
  background: linear-gradient(180deg, #0f2030 0%, #0b1a27 100%);
  border: 1px solid #1c3448; border-radius: 10px;
  padding: 12px;
}
.panel__title { font-size: 12.5px; font-weight: 650; color: #dcecf7; }
.panel__sub { display: block; font-size: 10px; color: #5f8ba3; margin-top: 3px; }
.panel__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 10px; margin-bottom: 10px; }
.legend { display: flex; align-items: center; gap: 9px; font-size: 10px; color: #8aa8bc; white-space: nowrap; }
.legend i { display: inline-block; width: 7px; height: 7px; border-radius: 2px; margin-right: 4px; }

/* 左栏 */
.scr-left { display: flex; flex-direction: column; gap: 6px; }
.area-item {
  position: relative; text-align: left; width: 100%; border: 1px solid transparent;
  background: #102332; border-radius: 8px; padding: 9px 9px 9px 12px;
  color: #cfe0ec; cursor: pointer; font-family: inherit;
  display: flex; flex-direction: column; gap: 3px;
}
.area-item:hover { background: #163044; }
.area-item.on { border-color: #2f8f76; background: #12332f; }
.area-item__dot { position: absolute; left: 4px; top: 14px; width: 5px; height: 5px; border-radius: 50%; }
.area-item__name { font-size: 12.5px; font-weight: 600; color: #e4f3ff; }
.area-item__meta { font-size: 9.5px; color: #7fa0b5; }
.area-item__code { font-size: 9px; color: #567b93; }
.scr-left__note {
  margin-top: 10px; padding: 9px; border-radius: 7px; background: #0b1c2a;
  border: 1px solid #1a3346; font-size: 9.5px; line-height: 1.7; color: #6f92a8;
}
.scr-left__note strong { color: #6ff0bd; font-weight: 600; }
.scr-left__version { margin-top: auto; padding-top: 10px; font-size: 9px; color: #4d7189; }

/* 中栏 */
.scr-main { display: flex; flex-direction: column; gap: 12px; min-width: 0; }
.kpis { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 9px; }
.kpi {
  background: linear-gradient(170deg, #122839 0%, #0c1c29 100%);
  border: 1px solid #1e3b52; border-top: 2px solid var(--kpi-tone);
  border-radius: 9px; padding: 10px 11px 9px; min-width: 0;
}
.kpi__label { font-size: 10.5px; color: #8aa8bc; white-space: nowrap; }
.kpi__value { font-size: 23px; font-weight: 700; color: var(--kpi-tone); line-height: 1.25; margin: 4px 0 2px; font-variant-numeric: tabular-nums; }
.kpi__value small { font-size: 11px; margin-left: 2px; font-weight: 500; }
.kpi__sub { font-size: 9px; color: #5f8ba3; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.matrix__grid {
  display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 9px;
  max-height: min(620px, 57vh); overflow-y: auto; padding-right: 3px;
}
.matrix__grid::-webkit-scrollbar, .node-table-wrap::-webkit-scrollbar { width: 5px; }
.matrix__grid::-webkit-scrollbar-thumb, .node-table-wrap::-webkit-scrollbar-thumb { background: #28506a; border-radius: 3px; }

.scr-bottom { display: grid; grid-template-columns: minmax(0, 1.6fr) minmax(0, 1fr); gap: 12px; }
.devices { display: flex; flex-direction: column; gap: 6px; }
.device {
  display: flex; align-items: center; gap: 9px; width: 100%;
  background: #102332; border: 1px solid #1e3b52; border-radius: 8px;
  padding: 7px 9px; color: #cfe0ec; cursor: pointer; font-family: inherit; text-align: left;
}
.device:hover { border-color: #3d7fa5; }
.device__icon { font-size: 16px; width: 24px; text-align: center; }
.device__text { display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0; }
.device__text b { font-size: 12px; color: #e0eef8; }
.device__text small { font-size: 9.5px; color: #6f92a8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.switch { width: 30px; height: 16px; border-radius: 9px; background: #274050; position: relative; flex: none; transition: background .2s; }
.switch i { position: absolute; width: 12px; height: 12px; border-radius: 50%; background: #9ab4c6; top: 2px; left: 2px; transition: left .2s, background .2s; }
.switch.on { background: #1f7a5f; }
.switch.on i { left: 16px; background: #78f0bd; }
.left-phase { margin-top: 10px; padding-top: 9px; border-top: 1px solid #1c3448; }
.left-phase__title { font-size: 10.5px; color: #9ab8cc; margin-bottom: 8px; }
.left-phase__row { margin-bottom: 7px; }
.left-phase__label { display: flex; justify-content: space-between; font-size: 9.5px; color: #7fa0b5; margin-bottom: 3px; }
.left-phase__label b { color: #d5e6f2; font-weight: 600; }
.left-phase__bar { height: 4px; background: #1a3042; border-radius: 2px; overflow: hidden; }
.left-phase__bar i { display: block; height: 100%; border-radius: 2px; }

/* 详情抽屉 */
.drawer-mask { position: fixed; inset: 0; background: #020a11aa; z-index: 40; }
.drawer {
  position: fixed; top: 0; right: 0; bottom: 0; width: min(700px, 94vw);
  background: #0c1c29; border-left: 1px solid #24506b; z-index: 41;
  overflow-y: auto; padding: 16px 18px 30px;
  box-shadow: -18px 0 44px #0009;
}
.drawer__head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; border-bottom: 1px solid #1c3448; padding-bottom: 12px; }
.drawer__head h2 { margin: 0; font-size: 17px; color: #eaf6ff; }
.drawer__head p { margin: 4px 0 0; font-size: 10px; color: #5f8ba3; }
.drawer__close { border: 0; background: transparent; color: #8fb0c6; font-size: 24px; line-height: 1; cursor: pointer; }
.drawer__section { margin-top: 16px; }
.drawer__section h3 { font-size: 12.5px; color: #dcecf7; margin: 0 0 9px; font-weight: 600; }
.drawer__tone { float: right; font-size: 11px; }
.kv-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 7px 14px; }
.kv-grid > div { display: flex; flex-direction: column; gap: 2px; padding: 6px 8px; background: #102332; border-radius: 6px; min-width: 0; }
.kv-grid span { font-size: 9.5px; color: #6f92a8; }
.kv-grid b { font-size: 11.5px; color: #dceaf5; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.drawer__progress { height: 5px; background: #1a3042; border-radius: 3px; overflow: hidden; margin-top: 9px; }
.drawer__progress i { display: block; height: 100%; }
.metric-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.metric-grid > div { background: #102332; border-radius: 7px; padding: 9px; display: flex; flex-direction: column; gap: 4px; }
.metric-grid span { font-size: 9.5px; color: #6f92a8; }
.metric-grid b { font-size: 17px; color: #6ff0bd; font-variant-numeric: tabular-nums; }
.metric-grid small { font-size: 10px; margin-left: 2px; color: #8fb0c6; }
.node-table-wrap { overflow-x: auto; }
.node-table { width: 100%; border-collapse: collapse; font-size: 10px; white-space: nowrap; }
.node-table th { text-align: left; color: #6f92a8; font-weight: 500; padding: 7px 6px; border-bottom: 1px solid #1c3448; }
.node-table td { padding: 7px 6px; border-bottom: 1px solid #163044; color: #c4d9e8; }
.drawer__warn { display: flex; align-items: center; gap: 7px; font-size: 10.5px; color: #c4d9e8; padding: 7px 9px; background: #102332; border-radius: 6px; margin-bottom: 6px; }
.drawer__warn span { width: 6px; height: 6px; border-radius: 50%; flex: none; }

/* 右栏 */
.scr-right { display: flex; flex-direction: column; height: calc(100vh - 92px); min-height: 560px; padding: 0; }
.scr-tabs { display: grid; grid-template-columns: repeat(2, 1fr); border-bottom: 1px solid #1c3448; }
.scr-tabs button {
  border: 0; background: transparent; color: #7e9cb2; padding: 11px 0;
  font-size: 11.5px; cursor: pointer; font-family: inherit; position: relative;
}
.scr-tabs button.on { color: #6ff0bd; }
.scr-tabs button.on::after { content: ''; position: absolute; bottom: 0; left: 22%; right: 22%; height: 2px; background: #6ff0bd; border-radius: 2px; }
.feed { flex: 1; overflow-y: auto; padding: 10px; display: flex; flex-direction: column; gap: 9px; }
.feed__item { display: flex; gap: 8px; }
.feed__item--click { cursor: pointer; transition: background .15s, transform .15s; border-radius: 6px; padding: 2px 4px; }
.feed__item--click:hover { background: rgba(111,240,189,0.08); transform: translateX(2px); }
.feed__tag {
  width: 26px; height: 26px; border-radius: 6px; flex: none; display: grid; place-items: center;
  font-size: 9px; font-weight: 700; background: #1c3c4c; color: #7fd4f0;
}
.feed__tag.is-alarm { background: #4a2a26; color: #f0a08c; }
.feed__tag.is-log { background: #2a3547; color: #9fb0c4; }
.feed__body { flex: 1; min-width: 0; }
.feed__title { font-size: 11.5px; color: #dcecf7; font-weight: 600; }
.feed__text { font-size: 10px; color: #7393a9; line-height: 1.65; margin-top: 3px; }
.feed__time { font-size: 9px; color: #4f7189; margin-top: 4px; }
.feed__empty { text-align: center; color: #5f8ba3; font-size: 11px; padding: 30px 0; }
.scr-right__foot { border-top: 1px solid #1c3448; padding: 9px 11px; font-size: 9.5px; color: #5f8ba3; line-height: 1.6; }

/* 节点编号可点击 */
.node-link { color: #6fd4f0; cursor: pointer; text-decoration: underline dotted; }
.node-link:hover { color: #9fe8ff; }

/* 节点详情：数据范围切换 */
.node-range { display: flex; align-items: center; gap: 8px; }
.node-range__label { font-size: 11px; color: #9ab8cc; margin-right: 2px; }
.node-range button {
  border: 1px solid #24405a; background: #102332; color: #a9c6d8;
  border-radius: 6px; padding: 5px 10px; font-size: 11px; cursor: pointer; font-family: inherit;
}
.node-range button.on { color: #6ff0bd; border-color: #2f7d63; }
.node-range__hint { font-size: 9.5px; color: #5f8ba3; margin: 7px 0 0; line-height: 1.6; }
.node-desc {
  margin: 8px 0 0; padding: 8px 10px; border-radius: 6px;
  background: #0d2130; border: 1px solid #1a3346;
  font-size: 10px; line-height: 1.7; color: #7fa0b5;
}

/* 垛位：同类型设备均值 */
.avg-bar { display: flex; align-items: center; justify-content: space-between; gap: 10px; flex-wrap: wrap; margin-bottom: 9px; }
.avg-types { display: flex; flex-wrap: wrap; gap: 5px; }
.avg-chip {
  border: 1px solid #24405a; background: #102332; color: #a9c6d8;
  border-radius: 20px; padding: 4px 10px; font-size: 10.5px; cursor: pointer;
  font-family: inherit; display: inline-flex; align-items: center; gap: 5px;
}
.avg-chip b { font-size: 9px; color: #6f92a8; font-weight: 600; }
.avg-chip:hover { border-color: #3d7fa5; color: #eaf6ff; }
.avg-chip.on { color: #6ff0bd; border-color: #2f7d63; background: #12332f; }
.avg-chip.on b { color: #6ff0bd; }

.avg-select { position: relative; display: flex; align-items: center; gap: 6px; }
.avg-select__label { font-size: 10px; color: #7fa0b5; }
.avg-select__btn {
  border: 1px solid #24405a; background: #102332; color: #cfe3f0;
  border-radius: 6px; padding: 5px 9px; font-size: 11px; cursor: pointer;
  font-family: inherit; display: inline-flex; align-items: center; gap: 6px; min-width: 84px;
  justify-content: space-between;
}
.avg-select__btn.on, .avg-select__btn:hover { border-color: #2f7d63; color: #6ff0bd; }
.avg-select__btn i { font-style: normal; font-size: 9px; opacity: .8; }
.avg-select__panel {
  position: absolute; right: 0; top: 30px; z-index: 8; min-width: 96px;
  background: #0d2030; border: 1px solid #2a4b66; border-radius: 7px;
  padding: 4px; box-shadow: 0 10px 26px #000a;
}
.avg-select__panel button {
  display: block; width: 100%; text-align: left; border: 0; background: transparent;
  color: #a9c6d8; font-size: 11px; padding: 6px 9px; border-radius: 5px;
  cursor: pointer; font-family: inherit;
}
.avg-select__panel button:hover { background: #16304a; color: #eaf6ff; }
.avg-select__panel button.on { background: #12332f; color: #6ff0bd; }

.avg-hint { margin: 0 0 9px; font-size: 9.5px; line-height: 1.7; color: #6f92a8; }
.avg-hint strong { color: #bfe0f0; font-weight: 600; }
.avg-stats { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 7px; margin-bottom: 10px; }
.avg-stats > div {
  background: #102332; border-radius: 7px; padding: 7px 9px;
  display: flex; flex-direction: column; gap: 2px; min-width: 0;
}
.avg-stats span { font-size: 9px; color: #6f92a8; }
.avg-stats b { font-size: 15px; color: #dceaf5; font-variant-numeric: tabular-nums; line-height: 1.2; }
.avg-stats b small { font-size: 9.5px; margin-left: 2px; color: #8fb0c6; }
.avg-stats em { font-style: normal; font-size: 8.5px; color: #587c94; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.metric-grid--auto { grid-template-columns: repeat(auto-fit, minmax(104px, 1fr)); }

.scr-toast {
  position: fixed; left: 50%; bottom: 26px; transform: translateX(-50%);
  background: #153a36; border: 1px solid #2f8f76; color: #9df5cf;
  padding: 9px 16px; border-radius: 8px; font-size: 12px; z-index: 60;
  box-shadow: 0 8px 28px #0008;
}

@media (min-width: 1181px) {
  /* 大屏：整页按视口高度布局，矩阵自适应、底部趋势图始终完整可见 */
  .screen { height: 100vh; overflow: hidden; }
  .scr-body { align-items: stretch; min-height: 0; }
  .scr-main { min-height: 0; }
  .matrix { flex: 1; min-height: 0; display: flex; flex-direction: column; position: relative; }
  .matrix::after {
    content: ''; position: absolute; left: 12px; right: 15px; bottom: 12px; height: 24px;
    background: linear-gradient(180deg, rgba(11, 26, 39, 0), #0b1a27);
    pointer-events: none; border-radius: 0 0 8px 8px;
  }
  .matrix__grid { flex: 1; min-height: 0; max-height: none; }
  .scr-bottom { flex: none; }
  .scr-right { height: auto; min-height: 0; }
  .scr-left { min-height: 0; overflow: hidden; }
}

@media (max-width: 1500px) {
  .kpis { grid-template-columns: repeat(4, minmax(0, 1fr)); }
  .scr-body { grid-template-columns: 168px minmax(0, 1fr) 220px; }
}
@media (max-width: 1180px) {
  .scr-body { grid-template-columns: 1fr; }
  .scr-left { flex-direction: row; flex-wrap: wrap; align-items: stretch; }
  .area-item { width: auto; flex: 1 1 150px; }
  .scr-left__note, .scr-left__version, .left-phase, .scr-right { display: none; }
  .scr-bottom { grid-template-columns: 1fr; }
  .matrix__grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
</style>

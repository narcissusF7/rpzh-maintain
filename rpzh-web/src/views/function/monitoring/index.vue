<template>
  <div class="app-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>监控中心</h2>
        <p>仓库、节点、故障与天气的实时概览；数据来自 rpzh-function 微服务</p>
      </div>
      <el-button size="small" :icon="RefreshRight" :loading="loading" @click="load">刷新数据</el-button>
    </div>

    <el-row :gutter="12" class="function-stats">
      <el-col v-for="s in statCards" :key="s.label" :xs="12" :sm="8" :md="4">
        <div class="app-card stat-card">
          <div class="stat-card__label">{{ s.label }}</div>
          <div class="stat-card__value">{{ s.value }}</div>
          <div class="stat-card__sub">{{ s.sub }}</div>
        </div>
      </el-col>
    </el-row>

    <el-alert class="function-alert" type="info" show-icon :closable="false">
      <template #title>监控曲线说明</template>
      <template #default>
        节点历史曲线（findNodeHistory / findMonitoringData）走 ClickHouse 历史表。本地 ClickHouse 已建 19 张表，
        部分节点类型（如 CO₂）对应的历史表尚未创建，曲线接口会返回 Unknown table。台账类数据不受影响。
      </template>
    </el-alert>

    <el-alert class="function-alert" type="warning" show-icon :closable="false">
      <template #title>设备异常状态判定规则（当前生效阈值）</template>
      <template #default>
        <span v-if="abnormalCfg">
          已入垛设备超过 {{ abnormalCfg.timeoutHours }} 小时未上报数据，或连续发出异常数据满 {{ abnormalCfg.maxErrorCount }} 次，
          即标记为「异常」（status=5）；异常设备重新上报且故障预警处置完成后自动恢复为「已入垛」。
        </span>
        <span v-else>阈值读取中…</span>
        阈值可在 bootstrap-local.yml 的 node.abnormal.* 或数据库 tb_general_setting（setting_key=nodeAbnormal）中调整，改库即生效。
      </template>
    </el-alert>

    <div class="app-card">
      <div class="table-toolbar">
        <span class="table-toolbar__title">接口自检</span>
        <span class="table-toolbar__count">共 {{ checks.length }} 个</span>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="checks" border size="small">
          <el-table-column prop="name" label="接口" width="150" />
          <el-table-column prop="path" label="路径" min-width="280" show-overflow-tooltip />
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.ok ? 'success' : 'warning'" size="small" effect="light">
                {{ row.ok ? '正常' : '降级' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="结果" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="{ 'check-err': !row.ok }">{{ row.summary }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="app-card">
      <div class="table-toolbar">
        <span class="table-toolbar__title">节点实时台账</span>
        <span class="table-toolbar__count">显示前 10 条</span>
      </div>
      <div class="table-wrap">
        <el-table :data="nodeRows" border stripe size="small">
          <el-table-column prop="nodeId" label="节点编号" width="140" />
          <el-table-column prop="nodeType" label="类型" width="120">
            <template #default="{ row }">{{ dictLabel(NODE_TYPE, row.nodeType) }}</template>
          </el-table-column>
          <el-table-column prop="gatherSite" label="采集位置" width="110" />
          <el-table-column prop="stack" label="所在垛位" min-width="160" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="dictType(NODE_STATUS, row.status)" size="small" effect="light">
                {{ dictLabel(NODE_STATUS, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="siteRoute" label="位置路径" min-width="200" show-overflow-tooltip />
          <template #empty>
            <el-empty :image-size="64" description="暂无节点数据" />
          </template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import { warehouseTree } from '@/api/function/warehouse'
import { findNode } from '@/api/function/node'
import {
  findTroubleNode,
  findNowWeather,
  findWarningThreshold,
  findNodeAbnormalConfig
} from '@/api/function/monitoring'
import { NODE_TYPE, NODE_STATUS, dictLabel, dictType, toRows, shortReason } from '@/utils/function'

const loading = ref(false)
const checks = ref([])
const nodeRows = ref([])
const abnormalCfg = ref(null)
const stats = reactive({
  warehouse: '-',
  nodes: '-',
  online: '-',
  abnormal: '-',
  trouble: '-',
  weather: '-'
})

const statCards = computed(() => [
  { label: '仓库数', value: stats.warehouse, sub: 'warehouseTree' },
  { label: '节点总数', value: stats.nodes, sub: 'findNode' },
  { label: '已入垛节点', value: stats.online, sub: 'status = 1' },
  { label: '异常节点', value: stats.abnormal, sub: 'status = 5' },
  { label: '故障节点', value: stats.trouble, sub: 'findTroubleNode' },
  { label: '实时天气', value: stats.weather, sub: 'findNowWeather' }
])

const tasks = [
  { name: '仓库树', path: '/maintain/warehouse/warehouseTree', fn: () => warehouseTree({ warehouseNodeId: 0, warehouseNodeType: '01' }) },
  { name: '节点台账', path: '/maintain/node/findNode', fn: () => findNode({ page: 1, size: 100 }) },
  { name: '故障节点', path: '/maintain/monitoringCenter/findTroubleNode', fn: () => findTroubleNode() },
  { name: '实时天气', path: '/maintain/monitoringCenter/findNowWeather', fn: () => findNowWeather() },
  { name: '预警阈值', path: '/maintain/monitoringCenter/findWarningThreshold', fn: () => findWarningThreshold() }
]

async function load() {
  loading.value = true
  const settled = await Promise.allSettled(tasks.map((t) => t.fn()))

  checks.value = tasks.map((t, i) => {
    const r = settled[i]
    if (r.status === 'fulfilled') {
      const { rows, total } = toRows(r.value)
      return { name: t.name, path: t.path, ok: true, summary: `返回 ${total} 条记录`, rows }
    }
    return { name: t.name, path: t.path, ok: false, summary: shortReason(r.reason?.message), rows: [] }
  })

  const byName = (n) => checks.value.find((c) => c.name === n)
  const wh = byName('仓库树')
  const nd = byName('节点台账')
  const tr = byName('故障节点')
  const we = byName('实时天气')

  stats.warehouse = wh.ok ? wh.rows.length : '-'
  stats.nodes = nd.ok ? nd.rows.length : '-'
  stats.online = nd.ok ? nd.rows.filter((r) => String(r.status) === '1').length : '-'
  stats.abnormal = nd.ok ? nd.rows.filter((r) => String(r.status) === '5').length : '-'
  stats.trouble = tr.ok ? tr.rows.length : '-'
  nodeRows.value = nd.ok ? nd.rows.slice(0, 10) : []

  try {
    abnormalCfg.value = await findNodeAbnormalConfig()
  } catch (e) {
    abnormalCfg.value = null
  }

  if (we.ok) {
    const w = we.rows?.[0] || (we.rows && !Array.isArray(we.rows) ? we.rows : null)
    stats.weather = w ? `${w.weatherName || '—'} ${w.temp ?? '—'}℃` : '暂无数据'
  } else {
    stats.weather = '暂无数据'
  }

  loading.value = false
}

onMounted(load)
</script>

<style scoped>
.function-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  margin-bottom: 12px;
}
.function-head__text h2 {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
}
.function-head__text p {
  margin: 0;
  font-size: 12px;
  opacity: 0.65;
}
.function-stats {
  margin-bottom: 12px;
}
.stat-card {
  padding: 12px 16px;
}
.stat-card__label {
  font-size: 12px;
  opacity: 0.65;
}
.stat-card__value {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 600;
}
.stat-card__sub {
  margin-top: 2px;
  font-size: 11px;
  opacity: 0.45;
}
.function-alert {
  margin-bottom: 12px;
}
.table-toolbar__title {
  font-weight: 600;
}
.table-toolbar__count {
  margin-left: 12px;
  font-size: 12px;
  opacity: 0.6;
}
.check-err {
  opacity: 0.75;
}
</style>

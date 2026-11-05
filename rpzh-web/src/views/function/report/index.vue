<template>
  <div class="app-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>数据报表</h2>
        <p>垛位对比、芯温趋势、设备明细与养护汇总；下方为各报表接口的实时自检结果</p>
      </div>
      <el-button size="small" :icon="RefreshRight" :loading="loading" @click="load">重新检测</el-button>
    </div>

    <el-row :gutter="12" class="function-stats">
      <el-col v-for="c in reportCards" :key="c.title" :xs="12" :sm="6">
        <div class="app-card report-card">
          <div class="report-card__title">{{ c.title }}</div>
          <div class="report-card__desc">{{ c.desc }}</div>
          <div class="report-card__api">{{ c.api }}</div>
        </div>
      </el-col>
    </el-row>

    <el-alert class="function-alert" type="info" show-icon :closable="false">
      <template #title>报表依赖说明</template>
      <template #default>
        报表查询走 ClickHouse 历史表做聚合，本地已建 19 张表。MySQL 侧 mapper 已完成 Oracle → MySQL 适配；
        部分报表在历史数据为空时后端会返回空值异常（如 SiteRouteUtil），下面逐个接口实测，失败项会给出原因。
      </template>
    </el-alert>

    <div class="app-card">
      <div class="table-toolbar">
        <span class="table-toolbar__title">报表接口自检</span>
        <span class="table-toolbar__count">
          {{ okCount }} / {{ checks.length }} 可用
        </span>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="checks" border stripe size="small">
          <el-table-column prop="name" label="报表接口" width="180" />
          <el-table-column prop="path" label="请求路径" min-width="260" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.ok ? 'success' : 'warning'" size="small" effect="light">
                {{ row.ok ? '正常' : '降级' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="结果" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="{ 'check-err': !row.ok }">{{ row.summary }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import {
  findSummaryReport,
  findSummaryReportDetails,
  findAllStackData,
  findDeviceParticularsInfo,
  findStackComparisonData,
  findStackAndWarehouseInfo,
  findStackAndCoreInfo,
  findMaintainEarlyTimes
} from '@/api/function/report'
import { toRows, shortReason } from '@/utils/function'

const loading = ref(false)
const checks = ref([])
const okCount = computed(() => checks.value.filter((c) => c.ok).length)

const reportCards = [
  { title: '垛位对比', desc: '多垛位环境数据横向对比', api: '/dataReport/stackComparison' },
  { title: '芯温趋势', desc: '垛芯温度与环温变化', api: '/dataReport/findStackAndCoreInfo' },
  { title: '设备明细', desc: '采集设备运行明细', api: '/dataReport/findDeviceParticularsInfo' },
  { title: '养护汇总', desc: '养护次数与到期汇总', api: '/dataReport/findSummaryReport' }
]

const params = { page: 1, size: 10 }
const tasks = [
  { name: '汇总报表', path: '/dataReport/findSummaryReport', fn: () => findSummaryReport(params) },
  { name: '汇总报表明细', path: '/dataReport/findSummaryReportDetails', fn: () => findSummaryReportDetails(params) },
  { name: '垛位对比', path: '/dataReport/stackComparison', fn: () => findAllStackData(params) },
  { name: '设备明细', path: '/dataReport/findDeviceParticularsInfo', fn: () => findDeviceParticularsInfo(params) },
  { name: '垛位对比数据', path: '/dataReport/findStackComparisonData', fn: () => findStackComparisonData(params) },
  { name: '垛位与仓库信息', path: '/dataReport/findStackAndWarehouseInfo', fn: () => findStackAndWarehouseInfo(params) },
  { name: '垛位与芯温信息', path: '/dataReport/findStackAndCoreInfo', fn: () => findStackAndCoreInfo(params) },
  { name: '养护提前次数', path: '/dataReport/findMaintainEarlyTimes', fn: () => findMaintainEarlyTimes(params) }
]

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

async function load() {
  loading.value = true
  const rows = []
  // 顺序调用而非并发：网关配置了 10 req/s 的限流，留出余量
  for (const t of tasks) {
    try {
      const data = await t.fn()
      const { total } = toRows(data)
      rows.push({ ...t, ok: true, summary: `返回 ${total} 条记录` })
    } catch (e) {
      const msg = e?.message || ''
      const summary = msg === '操作失败'
        ? '接口返回 flag=false，后端未给出原因'
        : shortReason(msg)
      rows.push({ ...t, ok: false, summary })
    }
    checks.value = [...rows]
    await sleep(170)
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
.report-card {
  padding: 14px 16px;
  min-height: 96px;
}
.report-card__title {
  font-weight: 600;
  margin-bottom: 6px;
}
.report-card__desc {
  font-size: 12px;
  opacity: 0.7;
}
.report-card__api {
  margin-top: 8px;
  font-size: 11px;
  opacity: 0.5;
  word-break: break-all;
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

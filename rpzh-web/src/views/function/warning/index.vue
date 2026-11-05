<template>
  <div class="app-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>预警中心</h2>
        <p>当前预警与历史预警；预警等级、类型、位置与处置状态</p>
      </div>
    </div>

    <el-tabs v-model="active" type="border-card" class="function-tabs">
      <el-tab-pane label="当前预警" name="current">
        <FunctionListPage
          ref="currentListRef"
          embedded
          title="当前预警"
          :api="findCurrentWarningList"
          :initial-query="initialQuery"
          :columns="currentColumns"
          :search-fields="commonSearch"
          :hint="currentHint"
          :detail="false"
          :action-width="200"
        >
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="warning" @click="markMisreport(row)">标记误报</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>
      <el-tab-pane label="历史预警" name="history">
        <FunctionListPage
          embedded
          title="历史预警"
          :api="findHistoryWarning"
          :columns="historyColumns"
          :search-fields="commonSearch"
          :stats="historyStats"
          :detail="false"
          :action-width="120"
        >
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="detailVisible"
      title="预警详情"
      width="520px"
      destroy-on-close
      :close-on-click-modal="false"
      append-to-body
    >
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="节点编号">{{ detailRow.nodeId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预警类型">{{ dictLabel(WARNING_TYPE, detailRow.warningType) }}</el-descriptions-item>
        <el-descriptions-item label="预警等级">{{ dictLabel(WARNING_GRADE, detailRow.warningGrade) }}</el-descriptions-item>
        <el-descriptions-item label="预警值">{{ detailRow.warningVal ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="预警位置">{{ detailRow.siteName || detailRow.areaName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ formatPrincipal(detailRow.principal || detailRow.principalName) }}</el-descriptions-item>
        <el-descriptions-item label="预警时间">{{ formatTime(detailRow.warningTime) }}</el-descriptions-item>
        <el-descriptions-item label="关联任务">
          <template v-if="taskIdOf(detailRow)">#{{ taskIdOf(detailRow) }}</template>
          <template v-else-if="siteRouteOf(detailRow)">按位置匹配（{{ siteRouteOf(detailRow) }}）</template>
          <template v-else>无</template>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :loading="jumping"
          :disabled="!canJump(detailRow)"
          @click="goTask(detailRow)"
        >查看关联任务</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import FunctionListPage from '../components/FunctionListPage.vue'
import { findCurrentWarningList, findHistoryWarning, misreport } from '@/api/function/warning'
import { findTaskDetail } from '@/api/function/task'
import { listUser } from '@/api/user'
import { WARNING_GRADE, WARNING_TYPE, WARNING_AFFIRM, toOptions, dictLabel } from '@/utils/function'
import { formatTime } from '@/utils/form'

const active = ref('current')
const currentListRef = ref(null)
const router = useRouter()
const route = useRoute()
const cleanName = (v) => String(v ?? '-').replace(/^null\((.*)\)$/, '$1')

/**
 * tb_early_warning.principal 存的是账号（多负责人用逗号分隔），列表要显示姓名。
 * 这里按用户列表做一次账号→姓名映射，映射不到就原样显示（历史脏数据也不至于空白）。
 */
const userMap = ref({})
async function loadUsers() {
  try {
    const d = await listUser({ page: 1, size: 500 })
    const rows = Array.isArray(d) ? d : d?.list || d?.rows || []
    const m = {}
    rows.forEach((u) => {
      if (u?.username) m[u.username] = u.nickname || u.nickName || u.username
    })
    userMap.value = m
  } catch (e) {
    userMap.value = {}
  }
}
function formatPrincipal(v) {
  const s = String(v ?? '').trim()
  if (!s) return '-'
  const names = s
    .split(',')
    .map((x) => x.trim())
    .filter(Boolean)
    .map((x) => userMap.value[x] || cleanName(x))
  return names.length ? names.join('、') : '-'
}

/* ---------------- 预警详情 + 跳转关联任务 ---------------- */
const detailVisible = ref(false)
const detailRow = ref({})
const jumping = ref(false)
function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}
function taskIdOf(row) {
  return row?.taskId ?? row?.task_id ?? ''
}
function siteRouteOf(row) {
  return row?.siteRoute ?? row?.site_route ?? ''
}
/** 有关联任务 ID 或位置路由时，才允许跳转 */
function canJump(row) {
  return !!taskIdOf(row) || !!siteRouteOf(row)
}
async function goTask(row) {
  if (jumping.value) return
  // 先关弹窗再跳转：避免遮罩层留在 body 上导致目标页无法点击
  detailVisible.value = false
  const id = taskIdOf(row)
  jumping.value = true
  try {
    // 优先：按 task_id 精确解析出任务编号再跳（任务页支持 taskNum 预筛选）
    if (id) {
      try {
        const t = await findTaskDetail(id)
        const obj = t?.data ?? t?.rows?.[0] ?? t
        const num = obj?.taskNum ?? obj?.task_num
        if (num) {
          router.push({ path: '/function/task', query: { taskNum: num } })
          return
        }
      } catch (e) {
        // 解析失败则回退到按位置跳转
      }
    }
    // 兜底：后端未返回 task_id 时，按预警位置在任务管理里筛出对应任务
    const siteRoute = siteRouteOf(row)
    if (siteRoute) {
      router.push({ path: '/function/task', query: { siteRoute } })
      return
    }
    ElMessage.warning('该预警暂无可关联的任务')
  } finally {
    jumping.value = false
  }
}

/** 大屏点告警跳过来时带 nodeId，首次加载即按该节点筛选 */
const initialQuery = route.query?.nodeId ? { nodeId: route.query.nodeId } : {}

// 已经在本页时再次从别处带 nodeId 跳进来（同一路由不同 query），需要重新筛选
watch(() => route.query.nodeId, (v, old) => {
  if (v && v !== old) currentListRef.value?.setQuery({ nodeId: v })
})

onMounted(loadUsers)

const commonSearch = [
  { prop: 'warningType', label: '预警类型', type: 'select', options: toOptions(WARNING_TYPE), width: 150 },
  { prop: 'warningGrade', label: '预警等级', type: 'select', options: toOptions(WARNING_GRADE), width: 140 },
  { prop: 'nodeId', label: '节点编号', width: 160 }
]

const currentColumns = [
  { prop: 'nodeId', label: '节点编号', minWidth: 140 },
  { prop: 'warningType', label: '预警类型', width: 130, dict: WARNING_TYPE },
  { prop: 'warningGrade', label: '预警等级', width: 120, dict: WARNING_GRADE },
  { prop: 'warningVal', label: '预警值', width: 110 },
  { prop: 'siteName', label: '预警位置', minWidth: 220 },
  { prop: 'principalName', label: '负责人', width: 120, formatter: formatPrincipal },
  { prop: 'warningTime', label: '预警时间', width: 170, type: 'time' }
]

const historyColumns = [
  { prop: 'nodeId', label: '节点编号', minWidth: 140 },
  { prop: 'warningType', label: '预警类型', width: 130, dict: WARNING_TYPE },
  { prop: 'warningGrade', label: '预警等级', width: 120, dict: WARNING_GRADE },
  { prop: 'warningVal', label: '预警值', width: 110 },
  { prop: 'siteName', label: '预警位置', minWidth: 220 },
  { prop: 'principalName', label: '负责人', width: 130, formatter: formatPrincipal },
  { prop: 'affirm', label: '确认状态', width: 110, dict: WARNING_AFFIRM },
  { prop: 'dealTime', label: '处置时间', width: 170, type: 'time' }
]

const historyStats = (rows, total) => [
  { label: '历史预警总数', value: total },
  { label: '三级预警', value: rows.filter((r) => String(r.warningGrade) === '3').length },
  { label: '二级预警', value: rows.filter((r) => String(r.warningGrade) === '2').length },
  { label: '当前页', value: rows.length }
]

const currentHint =
  '当前预警与 findColor 的 Oracle rownum = 1 已改为 LIMIT 1，现可直接读取 tb_early_warning 的实时预警。' +
  '标记误报会删除该预警及其关联任务；批量维修处置（updateRepairId）为 App 端 JSON 接口，' +
  '其 mapper 仍是 Oracle begin/end 批量块，暂未在 PC 端开放。' +
  '预警统计（findWarningCount）在无数据时会由后端返回空结果，属业务空值处理，不影响本页列表。'

async function markMisreport(row) {
  const id = row.id ?? row.earlyWarningId
  if (!id) {
    ElMessage.warning('该预警缺少 id，无法标记误报')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认将节点 ${row.nodeId || '-'} 的这条预警标记为误报吗？该预警会从「当前预警」中移除，其关联任务也会一并删除。`,
      '误报确认',
      { type: 'warning', confirmButtonText: '确认误报', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await misreport(id)
    ElMessage.success('已标记为误报')
    currentListRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败，请检查后端日志')
  }
}
</script>

<style scoped>
.function-head {
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
.function-tabs :deep(.el-tabs__content) {
  padding: 12px 0 0;
}
</style>

<template>
  <div class="task-page">
  <FunctionListPage
    ref="listRef"
    title="任务管理"
    description="养护处置任务：异常核查、设备更换、翻垛作业、虫害巡检与复核"
    :api="findTask"
    :initial-query="initialQuery"
    :columns="columns"
    :search-fields="searchFields"
    :stats="stats"
    :action-width="210"
  >
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建任务</el-button>
    </template>
    <template #row-actions="{ row }">
      <el-button link type="primary" @click="openDispose(row)">{{ row.status === '1' ? '评价' : '处置' }}</el-button>
      <el-button link type="danger" @click="removeTask(row)">删除</el-button>
    </template>
  </FunctionListPage>

  <CrudDialog
    v-model="visible"
    :title="mode === 'edit' ? (disposeMode === 'evaluate' ? '任务评价' : '处置任务') : '新建任务'"
    :fields="mode === 'edit' ? disposeFields : createFields"
    :model="current"
    :mode="mode"
    :defaults="mode === 'edit' ? disposeDefaults : createDefaults"
    :submit="submit"
    :transform="transformTask"
    :tip="tip"
    :success-message="mode === 'edit' ? (disposeMode === 'evaluate' ? '评价已提交' : '任务已处置') : '任务创建成功'"
    width="700px"
    label-width="120px"
    @success="onSuccess"
  >
    <template #extra>
      <div v-if="mode === 'edit'" class="task-pic-box">
        <div class="pic-title">现场照片</div>
        <div v-if="existingPics.length" class="pic-wall">
          <div v-for="p in existingPics" :key="p.id" class="pic-item">
            <el-image
              :src="withHttp(p.pictureUrl)"
              :preview-src-list="previewList"
              fit="cover"
              hide-on-click-modal
            />
            <el-button
              class="pic-del"
              type="danger"
              :icon="Delete"
              circle
              size="small"
              title="删除"
              @click="removeExisting(p)"
            />
          </div>
        </div>
        <div v-else class="pic-empty">暂无照片</div>
        <el-upload
          ref="uploadRef"
          list-type="picture-card"
          :auto-upload="false"
          accept="image/*"
          :on-change="onPick"
          :on-remove="onRemove"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="pic-hint">处置 / 评价时上传现场照片，保存后随任务归档（支持多张）</div>
      </div>
    </template>
  </CrudDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import FunctionListPage from '../components/FunctionListPage.vue'
import CrudDialog from '../components/CrudDialog.vue'
import { findTask, addTask, delTask, taskDispose, taskEvaluate, findTaskPics, uploadTaskPic, deleteTaskPic } from '@/api/function/task'
import { listUser } from '@/api/user'
import { warehouseTree, getAllWareArea } from '@/api/function/warehouse'
import { TASK_TYPE, TASK_STATUS, toOptions } from '@/utils/function'

const rowsOf = (d) => (Array.isArray(d) ? d : Array.isArray(d?.list) ? d.list : Array.isArray(d?.rows) ? d.rows : [])

/** 账号 → 姓名，登录后由 loadUsers 填充；接口只回账号时用它兜底显示 */
const userMap = ref({})

/**
 * 后端返回的负责人可能是「账号」「账号(姓名)」「null(账号)」三种形态（历史数据 + 缓存未命中），
 * 列表统一显示姓名：先拆多人，再取括号内/账号，最后按用户表换成姓名。
 */
const cleanName = (v) => {
  const names = String(v ?? '')
    .split(',')
    .map((s) => {
      const t = s.trim()
      const m = t.match(/^(?:null|[A-Za-z0-9_]+)\(([^()]+)\)$/)
      const key = m ? m[1] : t
      return userMap.value[key] || key
    })
    .filter(Boolean)
  return names.length ? names.join('、') : '-'
}

const columns = [
  { prop: 'taskNum', label: '任务编号', minWidth: 190 },
  { prop: 'type', label: '任务类型', width: 140, dict: TASK_TYPE },
  { prop: 'detail', label: '任务内容', minWidth: 220 },
  { prop: 'principal', label: '负责人', width: 120, formatter: cleanName },
  { prop: 'status', label: '状态', width: 100, dict: TASK_STATUS },
  { prop: 'taskAddress', label: '任务位置', minWidth: 220 },
  { prop: 'endTime', label: '截止时间', width: 170, type: 'time' },
  { prop: 'createdTime', label: '创建时间', width: 170, type: 'time' }
]

const stats = (rows, total) => [
  { label: '任务总数', value: total },
  { label: '当前页待处理', value: rows.filter((r) => String(r.status) === '0').length },
  { label: '当前页已完成', value: rows.filter((r) => String(r.status) === '1').length },
  { label: '当前页条数', value: rows.length }
]

const EXTRA_TYPES = [
  { label: '06 · 垛内氧气预警', value: '06' },
  { label: '07 · 仓间氧气预警', value: '07' },
  { label: '08 · 垛内温度预警', value: '08' },
  { label: '09 · 仓间温度预警', value: '09' },
  { label: '10 · 垛内湿度预警', value: '10' },
  { label: '11 · 仓间湿度预警', value: '11' },
  { label: '12 · 包芯温度预警', value: '12' },
  { label: '13 · 平均包芯高于环境预警', value: '13' },
  { label: '14 · 仓间粉螟预警', value: '14' },
  { label: '15 · 仓库检查', value: '15' },
  { label: '16 · 其他', value: '16' }
]
const typeOptions = () => [...toOptions(TASK_TYPE), ...EXTRA_TYPES]

const defaultEndTime = () => {
  const d = new Date()
  d.setDate(d.getDate() + 3)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} 18:00:00`
}

/* ===== 负责人下拉（用户列表） ===== */
const userOptions = ref([])
async function loadUsers() {
  try {
    const rows = rowsOf(await listUser({ page: 1, size: 200 }))
    userOptions.value = rows.map((u) => ({
      label: `${u.nickname || u.username}${u.username ? `（${u.username}）` : ''}`,
      value: u.username
    }))
    const m = {}
    rows.forEach((u) => { if (u?.username) m[u.username] = u.nickname || u.username })
    userMap.value = m
  } catch (e) {
    userOptions.value = []
  }
}

// tb_task.principal 存账号，筛选必须传账号；下拉展示姓名 + 账号，值仍是账号
const searchFields = computed(() => [
  { prop: 'taskNum', label: '任务编号', width: 190, placeholder: '如 TASK-20261104-001' },
  { prop: 'type', label: '任务类型', type: 'select', options: toOptions(TASK_TYPE), width: 150 },
  { prop: 'status', label: '状态', type: 'select', options: toOptions(TASK_STATUS), width: 120 },
  { prop: 'principal', label: '负责人', type: 'select', options: userOptions.value, width: 180 }
])

/* ===== 任务位置：仓库 → 楼栋 → 楼层 → 库区 树，选中后自动带出路径/ID ===== */
const locationTree = ref([])
const locationMap = ref({})
async function buildLocationTree() {
  try {
    const whRows = rowsOf(await warehouseTree({ warehouseNodeId: 0, warehouseNodeType: '01' }))
    const areas = rowsOf(await getAllWareArea())
    const tree = []
    const map = {}
    for (const w of whRows) {
      const wn = { id: `w${w.id}`, label: w.name || `仓库 ${w.id}`, type: '01', children: [] }
      const bRows = rowsOf(await warehouseTree({ warehouseNodeId: w.id, warehouseNodeType: '02' }))
      for (const b of bRows) {
        const bn = { id: `b${b.id}`, label: b.buildingName || b.buildingNo || `楼栋 ${b.id}`, type: '02', children: [] }
        const fRows = rowsOf(await warehouseTree({ warehouseNodeId: b.id, warehouseNodeType: '03' }))
        for (const f of fRows) {
          const fn = { id: `f${f.id}`, label: f.floorName || f.floorNo || `楼层 ${f.id}`, type: '03', children: [] }
          for (const a of areas.filter((x) => Number(x.floorId) === Number(f.id))) {
            const an = { id: String(a.id), label: a.areaName || a.areaNo || `分区 ${a.id}`, type: '04' }
            const path = [w.name, b.buildingName || b.buildingNo, f.floorName || f.floorNo, a.areaName || a.areaNo]
              .filter(Boolean).join('/')
            const route = `[00:0],[01:${w.id}],[02:${b.id}],[03:${f.id}],[04:${a.id}]`
            map[String(a.id)] = { path, route, siteId: a.id }
            fn.children.push(an)
          }
          bn.children.push(fn)
        }
        wn.children.push(bn)
      }
      tree.push(wn)
    }
    locationTree.value = tree
    locationMap.value = map
  } catch (e) {
    locationTree.value = []
    locationMap.value = {}
  }
}

/** 新建提交前：根据选中的位置自动写入 任务位置 / 位置路径 / 位置ID（三者对用户隐藏） */
async function transformTask(payload, mode) {
  if (mode === 'create' && payload.locationId) {
    const loc = locationMap.value[payload.locationId]
    if (loc) {
      payload.taskAddress = loc.path
      payload.siteRoute = loc.route
      payload.siteId = loc.siteId
    }
  }
  return payload
}

// addTask 的必填项；负责人从用户列表选择，任务位置从位置树选择，路径/ID 自动获取（对用户隐藏）
const createFields = [
  { prop: 'type', label: '任务类型', type: 'select', required: true, span: 12, options: typeOptions },
  { prop: 'principal', label: '负责人', type: 'select', required: true, span: 12, options: () => userOptions.value, filterable: true, placeholder: '从用户列表选择' },
  { prop: 'locationId', label: '任务位置', type: 'tree', required: true, span: 12, tree: () => locationTree.value, placeholder: '在仓库树中选择位置（库区）', clearable: true },
  { prop: 'endTime', label: '截止时间', type: 'datetime', required: true, span: 12, hint: '格式 yyyy-MM-dd HH:mm:ss，不能早于昨天' },
  { prop: 'pushWay', label: '推送方式', type: 'select', required: true, span: 12, options: () => [
    { label: 'M · 短信', value: 'M' },
    { label: 'A · App', value: 'A' },
    { label: 'Q · 全部', value: 'Q' }
  ] },
  { prop: 'detail', label: '任务内容', type: 'textarea', required: true, span: 24, rows: 2 }
]

const createDefaults = reactive({
  type: '01',
  principal: 'rpzh',
  pushWay: 'Q',
  detail: '前端新建测试任务',
  endTime: defaultEndTime()
})

const disposeFields = [
  { prop: 'dealMethod', label: '处置方式', span: 24, placeholder: '如 现场核查 / 更换设备', disabled: () => disposeMode.value === 'evaluate' },
  { prop: 'dealResult', label: '处置结果', type: 'textarea', span: 24, rows: 2, hint: '填写结果后任务置为「已完成」；留空则为「处理中」', disabled: () => disposeMode.value === 'evaluate' },
  { prop: 'evaluate', label: '任务评价', type: 'textarea', span: 24, rows: 2, disabled: () => disposeMode.value === 'dispose', hint: '处置中不可填写评价；任务完成后方可评价' }
]
const disposeDefaults = { dealMethod: '现场核查', dealResult: '已完成核查，无异常' }

/** 处置 / 评价 状态联动：已完成 => 仅评价可改；处置中 => 评价禁用 */
const disposeMode = ref('dispose')

const tip = computed(() => {
  if (mode.value !== 'edit') {
    return '任务编号、创建人、状态由后端生成（状态默认「待处理」），截止时间不能早于昨天。位置在仓库树中选择，路径与位置 ID 自动获取。'
  }
  return disposeMode.value === 'evaluate'
    ? '任务已「已完成」：处置信息只读，仅可填写任务评价。'
    : '处置会更新任务状态并联动预警记录；处置期间不可填写评价，完成后方可评价。'
})

const listRef = ref(null)
const route = useRoute()

/** 首次加载就带上的筛选（来自预警中心「查看关联任务」的 taskNum / siteRoute） */
const initialQuery = (() => {
  const q = {}
  if (route.query?.taskNum) q.taskNum = route.query.taskNum
  if (route.query?.siteRoute) q.siteRoute = route.query.siteRoute
  return q
})()

const visible = ref(false)
const mode = ref('create')
const current = ref(null)

/* ===== 任务现场照片（FastDFS） ===== */
const existingPics = ref([])   // 已有照片：{ id, pictureUrl, pictureGroup, pictureFilename }
const pendingFiles = ref([])   // 待上传的原始 File
const uploadRef = ref(null)
const withHttp = (u) => /^https?:\/\//.test(u || '') ? u : `http://${u}`
const previewList = computed(() => existingPics.value.map((p) => withHttp(p.pictureUrl)))

const openCreate = () => {
  createDefaults.endTime = defaultEndTime()
  mode.value = 'create'
  current.value = null
  visible.value = true
}
const openDispose = async (row) => {
  mode.value = 'edit'
  current.value = { ...row }
  disposeMode.value = String(row.status) === '1' ? 'evaluate' : 'dispose'
  pendingFiles.value = []
  existingPics.value = []
  visible.value = true
  try {
    const list = await findTaskPics(row.id)
    existingPics.value = Array.isArray(list) ? list : []
  } catch (e) {
    existingPics.value = []
  }
}
const onSuccess = () => listRef.value?.load()

async function submit(payload, m) {
  if (m === 'edit') {
    if (disposeMode.value === 'evaluate') {
      // 已完成任务：仅提交评价（评价是唯一可改字段，空值直接拦下，避免白发请求）
      // 注意：弹窗只在 submit 抛错时保留，所以这里必须 throw，不能 return
      if (!String(payload.evaluate || '').trim()) {
        throw new Error('请填写任务评价后再提交')
      }
      await taskEvaluate({ id: current.value?.id, evaluate: payload.evaluate })
    } else {
      await taskDispose({
        id: current.value?.id,
        dealMethod: payload.dealMethod,
        dealResult: payload.dealResult
      })
      if (payload.evaluate) {
        await taskEvaluate({ id: current.value?.id, evaluate: payload.evaluate })
      }
    }
    // 处置/评价保存成功后，上传现场照片（任务此刻已存在，可关联 id）
    await uploadPendingPics()
  } else {
    await addTask(payload)
  }
}

/** 处置/评价后上传现场照片：逐张上传，单张失败不影响整体，也不阻塞弹窗关闭 */
async function uploadPendingPics() {
  const id = current.value?.id
  if (!id || !pendingFiles.value.length) return
  let ok = 0
  for (const f of pendingFiles.value) {
    try {
      await uploadTaskPic(f, id)
      ok++
    } catch (e) {
      ElMessage.error(`照片「${f.name || '未命名'}」上传失败：${e?.message || '未知错误'}`)
    }
  }
  if (ok) {
    ElMessage.success(`已上传 ${ok} 张现场照片`)
    pendingFiles.value = []
    try {
      const list = await findTaskPics(id)
      existingPics.value = Array.isArray(list) ? list : existingPics.value
    } catch (e) { /* 保留现有列表 */ }
  }
}

/** 删除已有照片（FastDFS + DB） */
async function removeExisting(p) {
  try {
    await deleteTaskPic(p.pictureGroup, p.pictureFilename)
    existingPics.value = existingPics.value.filter((x) => x.id !== p.id)
    ElMessage.success('照片已删除')
  } catch (e) {
    ElMessage.error('删除失败：' + (e?.message || '未知错误'))
  }
}

function onPick(uploadFile) {
  if (uploadFile?.raw) pendingFiles.value.push(uploadFile.raw)
}
function onRemove(uploadFile) {
  pendingFiles.value = pendingFiles.value.filter((f) => f !== uploadFile.raw)
}

async function removeTask(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除任务 ${row.taskNum || row.id} 吗？`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await delTask([row.id])
    ElMessage.success('删除成功')
    listRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败，请检查后端日志')
  }
}

onMounted(() => {
  loadUsers()
  buildLocationTree()
})

/**
 * 从预警中心「查看关联任务」带参跳转：
 *  有 taskNum → 按任务编号精确筛选；否则有 siteRoute → 按预警位置筛选。
 * 首次进入由 :initial-query 直接带上（不再多发一次全量请求）；
 * 已在任务页时再次带参跳转，用 watch 重新筛选。
 */
function applyRouteQuery() {
  const patch = {}
  if (route.query?.taskNum) patch.taskNum = route.query.taskNum
  if (route.query?.siteRoute) patch.siteRoute = route.query.siteRoute
  if (Object.keys(patch).length) listRef.value?.setQuery(patch)
}
watch(() => route.query.taskNum, (v, old) => { if (v && v !== old) applyRouteQuery() })
watch(() => route.query.siteRoute, (v, old) => { if (v && v !== old) applyRouteQuery() })
</script>

<style scoped>
.task-pic-box {
  margin-top: 8px;
  padding-top: 14px;
  border-top: 1px dashed var(--el-border-color-lighter);
}
.pic-title {
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
}
.pic-wall {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}
.pic-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}
.pic-item .el-image {
  width: 100%;
  height: 100%;
}
.pic-del {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 4px;
}
.pic-empty {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-bottom: 10px;
}
.pic-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
}
</style>


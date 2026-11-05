<template>
  <div class="app-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>虫害监测</h2>
        <p>诱捕板台账与虫害采集历史，含甲虫／粉螨计数与预警等级</p>
      </div>
      <el-button type="primary" :icon="Edit" @click="openRule">修改巡查规则</el-button>
    </div>

    <el-tabs v-model="active" type="border-card" class="function-tabs">
      <el-tab-pane label="诱捕板" name="board">
        <FunctionListPage
          ref="boardListRef"
          embedded
          title="诱捕板"
          :api="findPestBoard"
          :columns="boardColumns"
          :search-fields="boardSearch"
          :stats="boardStats"
          :action-width="150"
        >
          <template #actions>
            <el-button type="primary" size="small" :icon="Plus" @click="openBoardCreate">新建诱捕板</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="danger" @click="removeBoard(row)">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>

      <el-tab-pane label="虫害历史" name="history">
        <FunctionListPage
          embedded
          title="虫害历史"
          :api="findPestDataHistory"
          :columns="historyColumns"
          :hint="historyHint"
        />
      </el-tab-pane>
    </el-tabs>

    <CrudDialog
      v-model="boardVisible"
      title="新建诱捕板"
      :fields="boardFields"
      :defaults="boardDefaults"
      :submit="submitBoard"
      :transform="transformBoard"
      tip="负责人从用户列表选择、所在区域从位置树选择，路径与位置 ID 自动获取；使用天数与启用时间字段请勿填写（当前 mapper 列数不匹配）。"
      width="700px"
      label-width="130px"
      @success="onBoardSuccess"
    />

    <CrudDialog
      v-model="ruleVisible"
      title="修改虫害巡查规则"
      mode="edit"
      :fields="ruleFields"
      :defaults="ruleDefaults"
      :submit="submitRule"
      tip="一周巡查日可多选；三级预警值必须严格递增：一级 < 二级 < 三级。"
      width="640px"
      label-width="130px"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit } from '@element-plus/icons-vue'
import FunctionListPage from '../components/FunctionListPage.vue'
import CrudDialog from '../components/CrudDialog.vue'
import {
  findPestBoard,
  findPestDataHistory,
  addPestBoard,
  deletePestBoard,
  updatePestRule
} from '@/api/function/pest'
import { listUser } from '@/api/user'
import { warehouseTree, getAllWareArea } from '@/api/function/warehouse'
import { BOARD_TYPE, ALERT_LEVEL, toOptions } from '@/utils/function'

const rowsOf = (d) => (Array.isArray(d) ? d : Array.isArray(d?.list) ? d.list : Array.isArray(d?.rows) ? d.rows : [])

const active = ref('board')
const cleanName = (v) => String(v ?? '-').replace(/^null\((.*)\)$/, '$1')

const boardColumns = [
  { prop: 'pestBoardId', label: '诱捕板编号', width: 140 },
  { prop: 'boardType', label: '板类型', width: 130, dict: BOARD_TYPE },
  { prop: 'siteName', label: '所在区域', minWidth: 130 },
  { prop: 'siteRoute', label: '位置路径', minWidth: 180 },
  { prop: 'principalName', label: '负责人', width: 120, formatter: cleanName },
  { prop: 'siteX', label: 'X', width: 60, align: 'center' },
  { prop: 'siteY', label: 'Y', width: 60, align: 'center' },
  { prop: 'siteZ', label: 'Z', width: 60, align: 'center' },
  { prop: 'remark', label: '备注', width: 120 },
  { prop: 'createdTime', label: '创建时间', width: 170, type: 'time' }
]

const boardSearch = [
  { prop: 'pestBoardId', label: '板编号', width: 150 },
  { prop: 'boardType', label: '板类型', type: 'select', options: toOptions(BOARD_TYPE), width: 140 },
  { prop: 'siteName', label: '所在区域', width: 140 }
]

const historyColumns = [
  { prop: 'pestBoardId', label: '诱捕板编号', width: 140 },
  { prop: 'gatherSite', label: '采集位置', width: 120 },
  { prop: 'gatherTime', label: '采集时间', width: 170, type: 'time' },
  { prop: 'beetleCount', label: '甲虫数', width: 100 },
  { prop: 'mealwormCount', label: '粉螨数', width: 100 },
  { prop: 'alertLevel', label: '预警等级', width: 120, dict: ALERT_LEVEL },
  { prop: 'siteRoute', label: '位置路径', minWidth: 180 }
]

const boardStats = (rows, total) => [
  { label: '诱捕板总数', value: total },
  { label: '常规板', value: rows.filter((r) => String(r.boardType) === '01').length },
  { label: '加强板', value: rows.filter((r) => String(r.boardType) === '02').length },
  { label: '当前页', value: rows.length }
]

const historyHint =
  '虫害历史的 rownum = 1 已改为 LIMIT 1，tb_pest_data_history 中的采集记录现可正常返回。' +
  '虫害规则（tb_pest_patrol）若为空配置表，后端会返回 Index: 0, Size: 0，属缺少基础配置数据；' +
  '可通过页面右上「修改巡查规则」写入配置。'

/* ===== 负责人下拉（用户列表） ===== */
const userOptions = ref([])
const userMap = {}
async function loadUsers() {
  try {
    const rows = rowsOf(await listUser({ page: 1, size: 200 }))
    rows.forEach((u) => { userMap[u.username] = u })
    userOptions.value = rows.map((u) => ({
      label: `${u.nickname || u.username}${u.username ? `（${u.username}）` : ''}`,
      value: u.username
    }))
  } catch (e) {
    userOptions.value = []
  }
}

/* ===== 所在区域：仓库 → 楼栋 → 楼层 → 库区 树，选中后自动带出路径/ID ===== */
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

/** 新建提交前：位置路径 / 位置ID / 所在区域 自动获取（对用户隐藏） */
async function transformBoard(payload) {
  // 负责人姓名以所选账号为准，避免「只改了账号但姓名还是旧值 / 默认账号没带出姓名」
  const u = payload.principalUser ? userMap.value[payload.principalUser] : null
  if (u) payload.principalName = u.nickname || u.username
  if (payload.locationId) {
    const loc = locationMap.value[payload.locationId]
    if (loc) {
      payload.siteName = loc.path
      payload.siteRoute = loc.route
      payload.siteId = loc.siteId
    }
  }
  return payload
}

/** addPestBoard 的必填字段；负责人从用户列表选择，所在区域从位置树选择，路径/ID 自动获取 */
const boardFields = [
  { prop: 'pestBoardId', label: '诱捕板编号', required: true, span: 12 },
  { prop: 'boardType', label: '板类型', type: 'select', span: 12, options: () => toOptions(BOARD_TYPE) },
  { prop: 'principalUser', label: '负责人', type: 'select', required: true, span: 12, options: () => userOptions.value, filterable: true, placeholder: '从用户列表选择',
    onChange: (v, form) => { const u = userMap[v]; if (u) form.principalName = u.nickname || u.username } },
  { prop: 'principalName', label: '负责人姓名', span: 12, hint: '选择负责人后自动带出', disabled: true },
  { prop: 'locationId', label: '所在区域', type: 'tree', required: true, span: 24, tree: () => locationTree.value, placeholder: '在仓库树中选择位置（库区）', clearable: true },
  { prop: 'rfid', label: 'RFID', required: true, span: 12, hint: '后端仅校验非空，不入库' },
  { prop: 'siteX', label: 'X 坐标', span: 8 },
  { prop: 'siteY', label: 'Y 坐标', span: 8 },
  { prop: 'siteZ', label: 'Z 坐标', span: 8 },
  { prop: 'remark', label: '备注', span: 24 }
]

const boardDefaults = reactive({
  boardType: '01',
  principalUser: 'rpzh',
  principalName: ''
})

const WEEK_OPTIONS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  .map((label, value) => ({ label, value }))

const ruleFields = [
  {
    prop: 'weekPatrol', label: '每周巡查日', type: 'multiselect', required: true, span: 24,
    options: () => WEEK_OPTIONS,
    hint: '选中的日子记为 1，其余为 0，后端按长度 7 写入周一到周日',
    transform: (v) => {
      const picked = Array.isArray(v) ? v.map(Number) : []
      return Array.from({ length: 7 }, (_, i) => (picked.includes(i) ? 1 : 0))
    }
  },
  { prop: 'valueOne', label: '一级预警值', type: 'number', required: true, span: 8 },
  { prop: 'valueTwo', label: '二级预警值', type: 'number', required: true, span: 8 },
  { prop: 'valueThree', label: '三级预警值', type: 'number', required: true, span: 8 },
  { prop: 'colorOne', label: '一级颜色', required: true, span: 8 },
  { prop: 'colorTwo', label: '二级颜色', required: true, span: 8 },
  { prop: 'colorThree', label: '三级颜色', required: true, span: 8 },
  { prop: 'replacePeriod', label: '更换周期(天)', type: 'number', required: true, span: 24 }
]

const ruleDefaults = {
  weekPatrol: [0, 2, 4],
  valueOne: 5,
  valueTwo: 10,
  valueThree: 20,
  colorOne: '#FFFF00',
  colorTwo: '#FFA500',
  colorThree: '#FF0000',
  replacePeriod: 30
}

const boardListRef = ref(null)
const boardVisible = ref(false)
const ruleVisible = ref(false)

const onBoardSuccess = () => boardListRef.value?.load()

onMounted(() => {
  loadUsers()
  buildLocationTree()
})

const openBoardCreate = () => {
  const stamp = Date.now()
  boardDefaults.pestBoardId = `BOARD-${stamp}`
  boardDefaults.rfid = `RFID-${stamp}`
  boardVisible.value = true
}
const openRule = () => { ruleVisible.value = true }

async function submitBoard(payload) {
  await addPestBoard(payload)
}

async function submitRule(payload) {
  const vals = [Number(payload.valueOne), Number(payload.valueTwo), Number(payload.valueThree)]
  if (!(vals[0] < vals[1] && vals[1] < vals[2])) {
    throw new Error('预警值需严格递增：一级 < 二级 < 三级')
  }
  await updatePestRule(payload)
}

async function removeBoard(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除诱捕板 ${row.pestBoardId || row.id} 吗？存在未处理虫害预警时后端会拒绝。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await deletePestBoard([row.id])
    ElMessage.success('删除成功')
    boardListRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败，请检查后端日志')
  }
}
</script>

<style scoped>
.function-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
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

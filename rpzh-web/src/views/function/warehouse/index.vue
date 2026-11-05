<template>
  <div class="app-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>仓库管理</h2>
        <p>仓库 → 楼 → 层 → 分区 → 垛位 的完整层级台账；每一级都支持新建、修改、删除</p>
      </div>
    </div>

    <el-tabs v-model="active" type="border-card" class="function-tabs">
      <!-- 仓库 -->
      <el-tab-pane label="仓库列表" name="warehouse">
        <FunctionListPage
          ref="whListRef"
          embedded
          title="仓库"
          :api="warehouseTree"
          :default-params="{ warehouseNodeId: 0, warehouseNodeType: '01' }"
          :paged="false"
          :columns="warehouseColumns"
          :stats="warehouseStats"
          :action-width="190"
        >
          <template #actions>
            <el-button type="primary" size="small" :icon="Plus" @click="openWhCreate">新建仓库</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openWhEdit(row)">修改</el-button>
            <el-button link type="danger" @click="removeNode(row, '01')">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>

      <!-- 楼栋 -->
      <el-tab-pane label="楼栋列表" name="building">
        <FunctionListPage
          v-if="bParent"
          ref="bListRef"
          embedded
          title="楼栋"
          :api="warehouseTree"
          :default-params="{ warehouseNodeId: bParent, warehouseNodeType: '02' }"
          :paged="false"
          :columns="buildingColumns"
          :action-width="190"
          @loaded="syncBuildingOptions"
        >
          <template #actions>
            <el-select v-model="bParent" size="small" style="width: 170px" @change="bListRef?.load()">
              <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
            <el-button type="primary" size="small" :icon="Plus" @click="openBuildingCreate">新建楼栋</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openBuildingEdit(row)">修改</el-button>
            <el-button link type="danger" @click="removeNode(row, '02')">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>

      <!-- 楼层 -->
      <el-tab-pane label="楼层列表" name="floor">
        <FunctionListPage
          v-if="fParent"
          ref="fListRef"
          embedded
          title="楼层"
          :api="warehouseTree"
          :default-params="{ warehouseNodeId: fParent, warehouseNodeType: '03' }"
          :paged="false"
          :columns="floorColumns"
          :action-width="190"
        >
          <template #actions>
            <el-select v-model="fParent" size="small" style="width: 210px" @change="fListRef?.load()">
              <el-option v-for="b in buildingOptions" :key="b.id" :label="b.label" :value="b.id" />
            </el-select>
            <el-button type="primary" size="small" :icon="Plus" @click="openFloorCreate">新建楼层</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openFloorEdit(row)">修改</el-button>
            <el-button link type="danger" @click="removeNode(row, '03')">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>

      <!-- 分区 -->
      <el-tab-pane label="分区列表" name="area">
        <FunctionListPage
          v-if="aParent"
          ref="aListRef"
          embedded
          title="分区"
          :api="warehouseTree"
          :default-params="{ warehouseNodeId: aParent, warehouseNodeType: '04' }"
          :paged="false"
          :columns="areaColumns"
          :action-width="190"
        >
          <template #actions>
            <el-select v-model="aParent" size="small" style="width: 240px" @change="aListRef?.load()">
              <el-option v-for="f in floorOptions" :key="f.id" :label="f.label" :value="f.id" />
            </el-select>
            <el-button type="primary" size="small" :icon="Plus" @click="openAreaCreate">新建分区</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openAreaEdit(row)">修改</el-button>
            <el-button link type="danger" @click="removeNode(row, '04')">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>

      <!-- 垛位 -->
      <el-tab-pane label="垛位列表" name="stack">
        <FunctionListPage
          ref="stackListRef"
          embedded
          title="垛位"
          :api="getStackPage"
          :default-params="{ wareAreaId: sParent }"
          :columns="stackColumns"
          :search-fields="stackSearch"
          :stats="stackStats"
          :action-width="190"
        >
          <template #actions>
            <el-select v-model="sParent" size="small" style="width: 240px" @change="stackListRef?.load()">
              <el-option v-for="a in areaOptions" :key="a.id" :label="a.label" :value="a.id" />
            </el-select>
            <el-button type="primary" size="small" :icon="Plus" @click="openStackCreate">新建垛位</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openStackEdit(row)">修改</el-button>
            <el-button link type="danger" @click="removeStack(row)">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>
    </el-tabs>

    <CrudDialog
      v-model="whVisible"
      :title="whMode === 'edit' ? '修改仓库' : '新建仓库'"
      :fields="whFields"
      :model="whCurrent"
      :mode="whMode"
      :defaults="whDefaults"
      :submit="submitWarehouse"
      tip="仓库编码不能重复；后端会自动生成层级路径与所属站点。"
      label-width="120px"
      @success="onWhSuccess"
    />

    <CrudDialog
      v-model="bVisible"
      :title="bMode === 'edit' ? '修改楼栋' : '新建楼栋'"
      :fields="buildingFields"
      :model="bCurrent"
      :mode="bMode"
      :defaults="buildingDefaults"
      :submit="submitBuilding"
      tip="楼号与楼栋名称在同一仓库下不可重复；楼栋类型由仓库类型自动带出。"
      label-width="120px"
      @success="onBuildingSuccess"
    />

    <CrudDialog
      v-model="fVisible"
      :title="fMode === 'edit' ? '修改楼层' : '新建楼层'"
      :fields="floorFields"
      :model="fCurrent"
      :mode="fMode"
      :defaults="floorDefaults"
      :submit="submitFloor"
      tip="楼层编号全局唯一；楼层类型由所属楼栋自动带出。"
      label-width="120px"
      @success="onFloorSuccess"
    />

    <CrudDialog
      v-model="aVisible"
      :title="aMode === 'edit' ? '修改分区' : '新建分区'"
      :fields="areaFields"
      :model="aCurrent"
      :mode="aMode"
      :defaults="areaDefaults"
      :submit="submitArea"
      tip="分区编号全局唯一；分区名称在同一楼层下不可重复。"
      label-width="120px"
      @success="onAreaSuccess"
    />

    <CrudDialog
      v-model="stackVisible"
      :title="stackMode === 'edit' ? '修改垛位' : '新建垛位'"
      :fields="stackFields"
      :model="stackCurrent"
      :mode="stackMode"
      :defaults="stackDefaults"
      :submit="submitStack"
      tip="位置路径 pidRoute 与所属站点 siteName 由后端按分区自动拼接，无需填写。新建后可在「养护计划」页选用空闲垛位。"
      label-width="120px"
      @success="onStackSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import FunctionListPage from '../components/FunctionListPage.vue'
import CrudDialog from '../components/CrudDialog.vue'
import {
  warehouseTree,
  getStackPage,
  getAllWareArea,
  addWarehouse,
  updateWarehouse,
  addBuilding,
  updateBuilding,
  addFloor,
  updateFloor,
  addWarearea,
  updateWarearea,
  addStack,
  updateStack,
  delWarehouseNode
} from '@/api/function/warehouse'
import { listUser } from '@/api/user'

const active = ref('warehouse')
const cleanName = (v) => String(v ?? '-').replace(/^null\((.*)\)$/, '$1')
const rowsOf = (res) => (Array.isArray(res) ? res : res?.list || res?.records || [])

/* ---------------- 负责人缓存（用户列表） ---------------- */
const userOptions = ref([])
const userMap = ref({})
async function loadUsers() {
  try {
    const rows = rowsOf(await listUser({ page: 1, size: 500 }))
    // 注意：tb_stack / tb_ware_area 等表的 principal_name 存的是「姓名」，
    // 所以下拉的 value 必须是姓名；账号只放在 label 里便于区分同名。
    userOptions.value = rows.map((u) => ({
      label: `${u.nickname || u.username}${u.username ? `（${u.username}）` : ''}`,
      value: u.nickname || u.username
    }))
    const m = {}
    rows.forEach((u) => { m[u.nickname || u.username] = { id: u.userId, username: u.username } })
    userMap.value = m
  } catch (e) {
    userOptions.value = []
  }
}

/* ---------------- 列表列定义 ---------------- */
const warehouseColumns = [
  { prop: 'name', label: '仓库名称', minWidth: 160 },
  { prop: 'code', label: '仓库编码', width: 120 },
  { prop: 'siteName', label: '所属站点', minWidth: 160 },
  { prop: 'principalName', label: '负责人', width: 120, formatter: cleanName },
  { prop: 'useVolume', label: '使用体积', width: 120, formatter: (v, r) => (v ? `${v} ${r?.useVolumeUnit || ''}` : '-') },
  { prop: 'useArea', label: '使用面积', width: 120, formatter: (v, r) => (v ? `${v} ${r?.useAreaUnit || ''}` : '-') },
  { prop: 'isAutomation', label: '自动化', width: 90, formatter: (v) => (String(v) === '1' ? '是' : '否') },
  { prop: 'buildTime', label: '建成时间', width: 170, type: 'time' }
]

const buildingColumns = [
  { prop: 'buildingNo', label: '楼号', width: 100 },
  { prop: 'buildingName', label: '楼栋名称', minWidth: 150 },
  { prop: 'siteName', label: '位置路径', minWidth: 200 },
  { prop: 'principalName', label: '负责人', width: 110, formatter: cleanName },
  { prop: 'buildingOrientation', label: '朝向', width: 110 },
  { prop: 'useVolume', label: '使用体积', width: 110 },
  { prop: 'remark', label: '备注', width: 110 },
  { prop: 'createdTime', label: '创建时间', width: 170, type: 'time' }
]

const floorColumns = [
  { prop: 'floorNo', label: '楼层编号', width: 110 },
  { prop: 'floorName', label: '楼层名称', minWidth: 150 },
  { prop: 'siteName', label: '位置路径', minWidth: 220 },
  { prop: 'isPartition', label: '是否分区', width: 100, formatter: (v) => (String(v) === '1' ? '是' : '否') },
  { prop: 'principalName', label: '负责人', width: 110, formatter: cleanName },
  { prop: 'remark', label: '备注', width: 110 },
  { prop: 'createdTime', label: '创建时间', width: 170, type: 'time' }
]

const areaColumns = [
  { prop: 'areaNo', label: '分区编号', width: 110 },
  { prop: 'areaName', label: '分区名称', minWidth: 150 },
  { prop: 'siteName', label: '位置路径', minWidth: 240 },
  { prop: 'principalName', label: '负责人', width: 110, formatter: cleanName },
  { prop: 'remark', label: '备注', width: 110 },
  { prop: 'createdTime', label: '创建时间', width: 170, type: 'time' }
]

const stackColumns = [
  { prop: 'stackNo', label: '垛位编号', width: 110 },
  { prop: 'nameCn', label: '垛位名称', minWidth: 150 },
  { prop: 'siteName', label: '位置路径', minWidth: 240 },
  { prop: 'principalName', label: '负责人', width: 110, formatter: cleanName },
  { prop: 'remark', label: '备注', width: 110 },
  { prop: 'createdTime', label: '创建时间', width: 170, type: 'time' }
]

const stackSearch = [
  { prop: 'stackNo', label: '垛位编号', width: 150 },
  { prop: 'nameCn', label: '垛位名称', width: 150 }
]

/* ---------------- 统计卡片 ---------------- */
const warehouseStats = (rows, total) => [
  { label: '仓库总数', value: total },
  { label: '自动化仓库', value: rows.filter((r) => String(r.isAutomation) === '1').length },
  { label: '使用中', value: rows.filter((r) => String(r.isUsing) === '1').length },
  { label: '统计中', value: rows.filter((r) => String(r.isStatistics) === '1').length }
]

const stackStats = (rows, total) => [
  { label: '垛位总数', value: total },
  { label: '当前页', value: rows.length },
  { label: '所属分区', value: sParent.value || '-' },
  { label: '层级', value: '5 级' }
]

/* ---------------- 上级下拉 ---------------- */
const warehouseOptions = ref([])
const buildingOptions = ref([])
const floorOptions = ref([])
const areaOptions = ref([])

const bParent = ref(null)
const fParent = ref(null)
const aParent = ref(null)
const sParent = ref(201)

const uniqBy = (list, key = 'id') => {
  const map = new Map()
  list.forEach((x) => { if (x && !map.has(x[key])) map.set(x[key], x) })
  return [...map.values()]
}

async function loadWarehouseOptions() {
  try {
    const rows = rowsOf(await warehouseTree({ warehouseNodeId: 0, warehouseNodeType: '01' }))
    warehouseOptions.value = rows.map((r) => ({ id: r.id, name: r.name || `仓库 ${r.id}` }))
    if (!bParent.value && warehouseOptions.value.length) bParent.value = warehouseOptions.value[0].id
  } catch (e) { /* 下拉失败不阻塞列表 */ }
}

async function loadBuildingOptions() {
  const all = []
  for (const w of warehouseOptions.value) {
    try {
      const rows = rowsOf(await warehouseTree({ warehouseNodeId: w.id, warehouseNodeType: '02' }))
      rows.forEach((r) => all.push({ id: r.id, label: `${w.name} / ${r.buildingName || r.buildingNo}`, warehouseId: w.id }))
    } catch (e) { /* ignore */ }
  }
  buildingOptions.value = uniqBy(all)
  if (!fParent.value && buildingOptions.value.length) fParent.value = buildingOptions.value[0].id
}

async function loadFloorOptions() {
  const all = []
  for (const b of buildingOptions.value) {
    try {
      const rows = rowsOf(await warehouseTree({ warehouseNodeId: b.id, warehouseNodeType: '03' }))
      rows.forEach((r) => all.push({ id: r.id, label: `${b.label} / ${r.floorName || r.floorNo}`, buildingId: b.id }))
    } catch (e) { /* ignore */ }
  }
  floorOptions.value = uniqBy(all)
  if (!aParent.value && floorOptions.value.length) aParent.value = floorOptions.value[0].id
}

async function loadAreaOptions() {
  const all = []
  // 优先用全量库区接口，一次拿全；不可用时按楼层逐个查
  try {
    const rows = rowsOf(await getAllWareArea())
    if (rows.length) {
      rows.forEach((r) => {
        const floor = floorOptions.value.find((f) => Number(f.id) === Number(r.floorId))
        all.push({
          id: r.id,
          label: `${floor?.label || `楼层 ${r.floorId}`} / ${r.areaName || r.areaNo}`,
          floorId: r.floorId
        })
      })
    }
  } catch (e) { /* fall through */ }
  if (!all.length) {
    for (const f of floorOptions.value) {
      try {
        const rows = rowsOf(await warehouseTree({ warehouseNodeId: f.id, warehouseNodeType: '04' }))
        rows.forEach((r) => all.push({ id: r.id, label: `${f.label} / ${r.areaName || r.areaNo}`, floorId: f.id }))
      } catch (e) { /* ignore */ }
    }
  }
  areaOptions.value = uniqBy(all)
  if (areaOptions.value.length && !areaOptions.value.some((a) => a.id === sParent.value)) {
    sParent.value = areaOptions.value[0].id
  }
}

/** 楼栋列表加载后，把当前仓库的楼栋并入下拉（避免新增后下拉缺项） */
function syncBuildingOptions(state) {
  const w = warehouseOptions.value.find((x) => x.id === bParent.value)
  if (!w) return
  const extra = state.rows.map((r) => ({ id: r.id, label: `${w.name} / ${r.buildingName || r.buildingNo}`, warehouseId: w.id }))
  buildingOptions.value = uniqBy([...buildingOptions.value, ...extra])
}

/* ---------------- 树状选择器数据（仓库→楼栋→楼层→分区） ---------------- */
const warehouseTreeData = ref([])
const LEVEL = { '01': 1, '02': 2, '03': 3, '04': 4 }

/**
 * 构建完整层级树：warehouse(01) → building(02) → floor(03) → area(04)。
 * 节点 id 统一为字符串，便于 el-tree-select 与表单值精确匹配（避免数字/字符串类型不一致导致回显成数字）。
 */
async function buildWarehouseTree() {
  try {
    const whRows = rowsOf(await warehouseTree({ warehouseNodeId: 0, warehouseNodeType: '01' }))
    const allAreas = rowsOf(await getAllWareArea()).map((a) => ({
      id: Number(a.id),
      label: a.areaName || a.areaNo || `分区 ${a.id}`,
      floorId: Number(a.floorId)
    }))
    const tree = []
    for (const w of whRows) {
      const wn = { id: String(w.id), label: w.name || `仓库 ${w.id}`, type: '01', children: [] }
      const bRows = rowsOf(await warehouseTree({ warehouseNodeId: w.id, warehouseNodeType: '02' }))
      for (const b of bRows) {
        const bn = { id: String(b.id), label: b.buildingName || b.buildingNo || `楼栋 ${b.id}`, type: '02', children: [] }
        const fRows = rowsOf(await warehouseTree({ warehouseNodeId: b.id, warehouseNodeType: '03' }))
        for (const f of fRows) {
          const fn = { id: String(f.id), label: f.floorName || f.floorNo || `楼层 ${f.id}`, type: '03', children: [] }
          for (const a of allAreas.filter((x) => x.floorId === Number(f.id))) {
            fn.children.push({ id: String(a.id), label: a.label, type: '04', children: [] })
          }
          bn.children.push(fn)
        }
        wn.children.push(bn)
      }
      tree.push(wn)
    }
    warehouseTreeData.value = tree
  } catch (e) {
    warehouseTreeData.value = []
  }
}

/**
 * 基于完整树裁剪出「只可选某个层级」的树：
 * - 目标层级节点可选（disabled=false），作为叶子；
 * - 上层节点可展开但不可选（disabled=true），帮助用户定位。
 */
function makeSelectTree(selectableType) {
  const sel = LEVEL[selectableType]
  const clone = (node) => {
    const lv = LEVEL[node.type]
    const copy = { ...node, disabled: lv !== sel }
    if (lv < sel && Array.isArray(node.children)) copy.children = node.children.map(clone)
    else copy.children = []
    return copy
  }
  return warehouseTreeData.value.map(clone)
}

onMounted(async () => {
  loadUsers()
  await loadWarehouseOptions()
  await loadBuildingOptions()
  await loadFloorOptions()
  await loadAreaOptions()
  await buildWarehouseTree()
})

/* ---------------- 表单字段 ---------------- */
const whFields = [
  { prop: 'name', label: '仓库名称', required: true, span: 12 },
  { prop: 'code', label: '仓库编码', required: true, span: 12, hint: '不能与已有仓库重复' },
  { prop: 'buildTime', label: '建成时间', type: 'date', required: true, span: 12 },
  { prop: 'principalName', label: '负责人', type: 'select', required: true, span: 12, options: () => userOptions.value, filterable: true, placeholder: '从用户列表选择' },
  { prop: 'siteName', label: '所属站点', span: 12 },
  { prop: 'isAutomation', label: '自动化', type: 'select', span: 12, options: () => [
    { label: '是', value: '1' },
    { label: '否', value: '0' }
  ] },
  { prop: 'useVolume', label: '使用体积', type: 'number', span: 12, precision: 2 },
  { prop: 'useArea', label: '使用面积', type: 'number', span: 12, precision: 2 },
  { prop: 'remark', label: '备注', type: 'textarea', span: 24, rows: 2 }
]
const whDefaults = { buildTime: '2020-01-01', principalName: '', isAutomation: '0' }

const buildingFields = [
  { prop: 'warehouseId', label: '所属仓库', type: 'tree', required: true, span: 12, tree: () => makeSelectTree('01'), placeholder: '在树中选择所属仓库' },
  { prop: 'buildingNo', label: '楼号', required: true, span: 12, hint: '如 03，不能与已有楼号重复' },
  { prop: 'buildingName', label: '楼栋名称', required: true, span: 12 },
  { prop: 'principalName', label: '负责人', type: 'select', span: 12, options: () => userOptions.value, filterable: true, placeholder: '从用户列表选择' },
  { prop: 'buildingOrientation', label: '朝向', span: 12 },
  { prop: 'useVolume', label: '使用体积', type: 'number', span: 12, precision: 2 },
  { prop: 'useArea', label: '使用面积', type: 'number', span: 12, precision: 2 },
  { prop: 'remark', label: '备注', type: 'textarea', span: 24, rows: 2 }
]
const buildingDefaults = { principalName: '', buildingOrientation: '坐北朝南' }

const floorFields = [
  { prop: 'buildingId', label: '所属楼栋', type: 'tree', required: true, span: 12, tree: () => makeSelectTree('02'), placeholder: '在树中选择所属楼栋' },
  { prop: 'floorNo', label: '楼层编号', required: true, span: 12, hint: '如 103，全局唯一' },
  { prop: 'floorName', label: '楼层名称', required: true, span: 12 },
  { prop: 'isPartition', label: '是否分区', type: 'select', required: true, span: 12, options: () => [
    { label: '是', value: '1' },
    { label: '否', value: '0' }
  ] },
  { prop: 'principalName', label: '负责人', type: 'select', span: 12, options: () => userOptions.value, filterable: true, placeholder: '从用户列表选择' },
  { prop: 'remark', label: '备注', type: 'textarea', span: 24, rows: 2 }
]
const floorDefaults = { isPartition: '1', principalName: '' }

const areaFields = [
  { prop: 'floorId', label: '所属楼层', type: 'tree', required: true, span: 12, tree: () => makeSelectTree('03'), placeholder: '在树中选择所属楼层' },
  { prop: 'areaNo', label: '分区编号', required: true, span: 12, hint: '如 C，全局唯一' },
  { prop: 'areaName', label: '分区名称', required: true, span: 12 },
  { prop: 'principalName', label: '负责人', type: 'select', span: 12, options: () => userOptions.value, filterable: true, placeholder: '从用户列表选择' },
  { prop: 'remark', label: '备注', type: 'textarea', span: 24, rows: 2 }
]
const areaDefaults = { principalName: '' }

const stackFields = [
  { prop: 'stackNo', label: '垛位编号', required: true, span: 12 },
  { prop: 'nameCn', label: '垛位名称', required: true, span: 12 },
  { prop: 'areaId', label: '所属分区', type: 'tree', required: true, span: 12, tree: () => makeSelectTree('04'), placeholder: '在树中选择所属分区' },
  { prop: 'principalName', label: '负责人', type: 'select', span: 12, options: () => userOptions.value, filterable: true, placeholder: '从用户列表选择', hint: '修改时必填；负责人 ID 自动带出，无需手填', onChange: (v, form) => { const u = userMap.value[v]; if (u && u.id != null) form.principalId = u.id } },
  { prop: 'remark', label: '备注', span: 12 }
]
const stackDefaults = { areaId: '201', principalName: '' }

/* ---------------- 弹窗状态 ---------------- */
const whListRef = ref(null)
const bListRef = ref(null)
const fListRef = ref(null)
const aListRef = ref(null)
const stackListRef = ref(null)

const whVisible = ref(false)
const whMode = ref('create')
const whCurrent = ref(null)
const bVisible = ref(false)
const bMode = ref('create')
const bCurrent = ref(null)
const fVisible = ref(false)
const fMode = ref('create')
const fCurrent = ref(null)
const aVisible = ref(false)
const aMode = ref('create')
const aCurrent = ref(null)
const stackVisible = ref(false)
const stackMode = ref('create')
const stackCurrent = ref(null)

const onWhSuccess = () => { whListRef.value?.load(); loadWarehouseOptions() }
const onBuildingSuccess = async () => {
  bListRef.value?.load()
  await loadBuildingOptions()
  await loadFloorOptions()
  await loadAreaOptions()
}
const onFloorSuccess = async () => {
  fListRef.value?.load()
  await loadFloorOptions()
  await loadAreaOptions()
}
const onAreaSuccess = async () => {
  aListRef.value?.load()
  await loadAreaOptions()
}
const onStackSuccess = () => stackListRef.value?.load()

const openWhCreate = () => { whMode.value = 'create'; whCurrent.value = null; whVisible.value = true }
const openWhEdit = (row) => { whMode.value = 'edit'; whCurrent.value = { ...row }; whVisible.value = true }

const openBuildingCreate = () => {
  bMode.value = 'create'
  bCurrent.value = { warehouseId: bParent.value != null ? String(bParent.value) : '' }
  bVisible.value = true
}
const openBuildingEdit = (row) => { bMode.value = 'edit'; bCurrent.value = { ...row, warehouseId: row.warehouseId != null ? String(row.warehouseId) : '' }; bVisible.value = true }

const openFloorCreate = () => {
  fMode.value = 'create'
  fCurrent.value = { buildingId: fParent.value != null ? String(fParent.value) : '' }
  fVisible.value = true
}
const openFloorEdit = (row) => { fMode.value = 'edit'; fCurrent.value = { ...row, buildingId: row.buildingId != null ? String(row.buildingId) : '' }; fVisible.value = true }

const openAreaCreate = () => {
  aMode.value = 'create'
  aCurrent.value = { floorId: aParent.value != null ? String(aParent.value) : '' }
  aVisible.value = true
}
const openAreaEdit = (row) => { aMode.value = 'edit'; aCurrent.value = { ...row, floorId: row.floorId != null ? String(row.floorId) : '' }; aVisible.value = true }

const openStackCreate = () => {
  stackMode.value = 'create'
  stackCurrent.value = { areaId: sParent.value != null ? String(sParent.value) : '' }
  stackVisible.value = true
}
const openStackEdit = (row) => { stackMode.value = 'edit'; stackCurrent.value = { ...row, areaId: row.areaId != null ? String(row.areaId) : '' }; stackVisible.value = true }

/* ---------------- 提交 ---------------- */
async function submitWarehouse(payload, m) {
  if (m === 'edit') await updateWarehouse({ ...payload, id: whCurrent.value?.id })
  else await addWarehouse(payload)
}

async function submitBuilding(payload, m) {
  if (m === 'edit') await updateBuilding({ ...payload, id: bCurrent.value?.id })
  else await addBuilding(payload)
}

async function submitFloor(payload, m) {
  if (m === 'edit') await updateFloor({ ...payload, id: fCurrent.value?.id })
  else await addFloor({ ...payload, buildingId: String(payload.buildingId) })
}

async function submitArea(payload, m) {
  if (m === 'edit') await updateWarearea({ ...payload, id: aCurrent.value?.id })
  else await addWarearea(payload)
}

async function submitStack(payload, m) {
  // updateStack 的 WarehouseServiceImpl 对 principalName 直接 contains("(")，空值会 NPE
  if (!payload.principalName) throw new Error('请选择负责人')
  // 负责人 ID 由所选用户自动带出（不再要求手填数字）
  const u = userMap.value[payload.principalName]
  if (u && u.id != null) payload.principalId = u.id
  else if (stackCurrent.value?.principalId != null) payload.principalId = stackCurrent.value.principalId
  if (m === 'edit') await updateStack({ ...payload, stackId: stackCurrent.value?.id })
  else await addStack(payload)
}

/* ---------------- 删除 ---------------- */
async function confirmRemove(message) {
  try {
    await ElMessageBox.confirm(message, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    return true
  } catch (e) {
    return false
  }
}

const NODE_LABEL = { '01': '仓库', '02': '楼栋', '03': '楼层', '04': '分区' }

async function removeNode(row, type) {
  const name = row.name || row.buildingName || row.floorName || row.areaName || row.id
  if (!(await confirmRemove(`确认删除${NODE_LABEL[type] || '节点'} ${name} 吗？存在下级节点时后端会拒绝。`))) return
  try {
    await delWarehouseNode({ warehouseNodeId: row.id, warehouseNodeType: type })
    ElMessage.success('删除成功')
    if (type === '01') { whListRef.value?.load(); loadWarehouseOptions() }
    if (type === '02') { bListRef.value?.load(); loadBuildingOptions() }
    if (type === '03') { fListRef.value?.load(); loadFloorOptions() }
    if (type === '04') { aListRef.value?.load(); loadAreaOptions() }
  } catch (e) {
    ElMessage.error(e?.message || '删除失败，请检查后端日志')
  }
}

async function removeStack(row) {
  if (!(await confirmRemove(`确认删除垛位 ${row.stackNo || row.id} 吗？已有养护计划的垛位请先处理计划。`))) return
  try {
    await delWarehouseNode({ warehouseNodeType: '05', stackIds: [row.id] })
    ElMessage.success('删除成功')
    stackListRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败，请检查后端日志')
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

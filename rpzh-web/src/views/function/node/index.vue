<template>
  <FunctionListPage
    ref="listRef"
    title="节点管理"
    description="传感器节点台账：「修改」用于修改入垛位置（垛位 / XYZ 坐标 / 箱号），「入垛 / 出垛」绑定或解绑垛位；状态含 解垛 / 已入垛 / 未使用 / 故障 / 运维中 / 异常"
    :api="findNode"
    :columns="columns"
    :search-fields="searchFields"
    :stats="stats"
    :action-width="330"
  >
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="openCreate">添加节点</el-button>
    </template>
    <template #row-actions="{ row }">
      <el-button link type="primary" @click="openEditPosition(row)">修改</el-button>
      <el-button v-if="canEnter(row)" link type="success" @click="openEnter(row)">入垛</el-button>
      <el-button v-if="canUnbind(row)" link type="warning" @click="openUnbind(row)">出垛</el-button>
      <el-button link type="danger" @click="removeNode(row)">删除</el-button>
    </template>
  </FunctionListPage>

  <!-- 第一步：添加 / 修改节点（仅基础信息，不含垛位绑定） -->
  <CrudDialog
    v-model="nodeVisible"
    :title="'添加节点（第一步：登记设备）'"
    :fields="nodeFields"
    :model="nodeCurrent"
    :mode="nodeMode"
    :defaults="nodeDefaults"
    :submit="submitNode"
    :tip="'第一步：登记设备基础信息。登记后状态为「未入垛」；设备箱号在「入垛」时填写，随后在列表对该节点执行「入垛」绑定垛位。'"
    label-width="110px"
    @success="onSuccess"
  />

  <!-- 入垛 / 修改入垛位置：共用同一套位置表单与 enterStack 链路 -->
  <CrudDialog
    v-model="enterVisible"
    :title="enterMode === 'position' ? '修改入垛位置' : '节点入垛（第二步：绑定垛位）'"
    :fields="enterFields"
    :model="enterCurrent"
    :mode="'create'"
    :submit="submitEnter"
    :tip="enterMode === 'position'
      ? `修改节点 ${enterNode?.nodeId || ''} 的入垛位置：重选目标垛位、调整 XYZ 坐标或更新设备箱号，保存后立即生效。`
      : `将节点 ${enterNode?.nodeId || ''} 绑定到目标垛位；可填写采集坐标。绑定后状态变为「已入垛」。`"
    :success-message="enterMode === 'position' ? '入垛位置已修改' : '入垛成功'"
    label-width="110px"
    @success="onEnterSuccess"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import FunctionListPage from '../components/FunctionListPage.vue'
import CrudDialog from '../components/CrudDialog.vue'
import {
  findNode,
  addNode,
  updateNode,
  deleteNode,
  enterStack,
  unbindStack
} from '@/api/function/node'
import { warehouseTree, getAllWareArea, getStackPage } from '@/api/function/warehouse'
import { NODE_TYPE, NODE_STATUS, toOptions } from '@/utils/function'

const columns = [
  { prop: 'nodeId', label: '节点编号', minWidth: 140 },
  { prop: 'nodeType', label: '节点类型', width: 130, dict: NODE_TYPE },
  { prop: 'gatherSite', label: '采集位置', width: 100 },
  { prop: 'stack', label: '所在垛位', minWidth: 150 },
  { prop: 'status', label: '状态', width: 100, dict: NODE_STATUS },
  { prop: 'caseNo', label: '设备箱号', width: 110 },
  { prop: 'siteRoute', label: '位置路径', minWidth: 200 },
  { prop: 'intoStackTime', label: '入垛时间', width: 170, type: 'time' }
]

const searchFields = [
  { prop: 'nodeId', label: '节点编号', width: 170, placeholder: '如 NODE-0001' },
  { prop: 'nodeType', label: '节点类型', type: 'select', options: toOptions(NODE_TYPE), width: 150 },
  { prop: 'status', label: '状态', type: 'select', options: toOptions(NODE_STATUS), width: 120 }
]

const stats = (rows, total) => [
  { label: '节点总数', value: total },
  { label: '当前页已入垛', value: rows.filter((r) => String(r.status) === '1').length },
  { label: '当前页解垛', value: rows.filter((r) => String(r.status) === '0').length },
  { label: '当前页异常', value: rows.filter((r) => String(r.status) === '5').length },
  { label: '当前页条数', value: rows.length }
]

/** 只有解垛 / 未使用（或状态为空）的设备可入垛；异常、故障、运维中设备仍占垛位 */
const canEnter = (row) => !['1', '5', '3', '4'].includes(String(row.status))
/** 已入垛或异常（仍在垛内）的设备可出垛 */
const canUnbind = (row) => ['1', '5'].includes(String(row.status))

/* ---------------- 第一步：节点基础信息字段（不含垛位 / 坐标） ---------------- */
const nodeFields = [
  { prop: 'nodeId', label: '节点编号', required: true, span: 12, hint: '设备唯一编号，已存在会被后端拒绝；设备箱号在「入垛」时填写' },
  { prop: 'nodeType', label: '节点类型', type: 'select', required: true, span: 12, options: () => toOptions(NODE_TYPE) }
]
const nodeDefaults = { nodeType: '01' }

const listRef = ref(null)
const nodeVisible = ref(false)
const nodeMode = ref('create')
const nodeCurrent = ref(null)

const openCreate = () => { nodeMode.value = 'create'; nodeCurrent.value = null; nodeVisible.value = true }
const onSuccess = () => listRef.value?.load()

async function submitNode(payload) {
  // 第一步：仅登记设备基础信息（不含设备箱号 / 垛位，箱号在「入垛」时填写）
  await addNode({ nodeId: payload.nodeId, nodeType: payload.nodeType })
}

/* ---------------- 第二步：入垛 / 出垛 ---------------- */
const enterVisible = ref(false)
/** enter = 新入垛；position = 修改已入垛设备的位置 */
const enterMode = ref('enter')
const enterNode = ref(null)
const enterCurrent = ref(null)
const stackTreeData = ref([])
const stackMap = ref({}) // stackId(字符串) -> { id, nameCn, pidRoute }

// 垛位选择字段（树状，只可选最末级「垛位」）
const enterFields = [
  { prop: 'stackId', label: '目标垛位', type: 'tree', required: true, span: 24, tree: () => stackSelectTree(), placeholder: '在树中选择目标垛位' },
  { prop: 'caseNo', label: '设备箱号', span: 12 },
  { prop: 'siteX', label: 'X 坐标', span: 8 },
  { prop: 'siteY', label: 'Y 坐标', span: 8 },
  { prop: 'siteZ', label: 'Z 坐标', span: 8 }
]

const rowsOf = (res) =>
  Array.isArray(res) ? res : (res?.list || res?.records || res?.data?.list || [])

/**
 * 构建「仓库 → 楼栋 → 楼层 → 分区 → 垛位」完整树，供入垛时选择目标垛位。
 * 垛位的 pid_route（父级路径）随节点一起缓存，用于构造 enterStack 的 pidRoute 入参。
 */
async function buildStackTree() {
  try {
    const whRows = rowsOf(await warehouseTree({ warehouseNodeId: 0, warehouseNodeType: '01' }))
    const areas = rowsOf(await getAllWareArea())
    const tree = []
    for (const w of whRows) {
      const wn = { id: String(w.id), label: w.name || `仓库 ${w.id}`, type: '01', children: [] }
      const bRows = rowsOf(await warehouseTree({ warehouseNodeId: w.id, warehouseNodeType: '02' }))
      for (const b of bRows) {
        const bn = { id: String(b.id), label: b.buildingName || b.buildingNo || `楼栋 ${b.id}`, type: '02', children: [] }
        const fRows = rowsOf(await warehouseTree({ warehouseNodeId: b.id, warehouseNodeType: '03' }))
        for (const f of fRows) {
          const fn = { id: String(f.id), label: f.floorName || f.floorNo || `楼层 ${f.id}`, type: '03', children: [] }
          for (const a of areas.filter((x) => Number(x.floorId) === Number(f.id))) {
            const an = { id: String(a.id), label: a.areaName || a.areaNo || `分区 ${a.id}`, type: '04', children: [] }
            try {
              const stRows = rowsOf(await getStackPage({ wareAreaId: a.id, page: 1, size: 1000 }))
              for (const s of stRows) {
                const sid = String(s.id)
                an.children.push({ id: sid, label: s.nameCn || s.stackNo || `垛位 ${s.id}`, type: '05' })
                stackMap.value[sid] = { id: s.id, nameCn: s.nameCn || s.stackNo || '', pidRoute: s.pidRoute || '' }
              }
            } catch (e) { /* 单个分区垛位加载失败不阻塞整棵树 */ }
            fn.children.push(an)
          }
          bn.children.push(fn)
        }
        wn.children.push(bn)
      }
      tree.push(wn)
    }
    stackTreeData.value = tree
  } catch (e) {
    stackTreeData.value = []
  }
}

/** 裁剪出只可选「垛位」(type=05) 的树：上层节点可展开定位但不可选 */
function stackSelectTree() {
  const clone = (node) => {
    const lv = Number(node.type)
    const copy = { ...node, disabled: lv !== 5 }
    if (lv < 5 && Array.isArray(node.children)) copy.children = node.children.map(clone)
    else copy.children = []
    return copy
  }
  return stackTreeData.value.map(clone)
}

const openEnter = (row) => {
  enterMode.value = 'enter'
  enterNode.value = row
  enterCurrent.value = { stackId: '', caseNo: row.caseNo || '' }
  enterVisible.value = true
}

/** 「修改」= 修改入垛位置：带出当前垛位 / 坐标 / 箱号，提交仍走已跑通的 enterStack 链路 */
const openEditPosition = (row) => {
  enterMode.value = 'position'
  enterNode.value = row
  enterCurrent.value = {
    stackId: row.stackId != null
      ? String(row.stackId)
      : (row.siteId != null ? String(row.siteId) : ''),
    caseNo: row.caseNo || '',
    siteX: row.siteX || '',
    siteY: row.siteY || '',
    siteZ: row.siteZ || ''
  }
  enterVisible.value = true
}

async function submitEnter(form) {
  const sid = String(form.stackId)
  const st = stackMap.value[sid]
  if (!st) throw new Error('请选择目标垛位')
  const node = enterNode.value

  if (enterMode.value === 'position') {
    // 「修改」只更新入垛位置与箱号，走 updateNode 动态 set：
    // 不会像 enterStack 那样把 status 重置为 1、把入垛时间刷新成 NOW()。
    await updateNode({
      id: node.id,
      nodeId: node.nodeId,
      nodeType: node.nodeType,
      caseNo: form.caseNo || '',
      stackId: st.id,
      siteId: st.id,
      siteType: '05',
      gatherSite: st.nameCn,
      stack: st.nameCn,
      siteX: form.siteX || '',
      siteY: form.siteY || '',
      siteZ: form.siteZ || '',
      siteRoute: `${st.pidRoute},[05:${st.id}]`
    })
    return
  }

  const payload = [{
    id: node.id,
    nodeId: node.nodeId,
    nodeType: node.nodeType,
    pidRoute: st.pidRoute, // 垛位父级路径，如 0,1,11,101,201
    stackId: st.id,
    stack: st.nameCn,
    siteType: '05',
    caseNo: form.caseNo || '',
    x: form.siteX || '',
    y: form.siteY || '',
    z: form.siteZ || ''
  }]
  await enterStack(payload)
}

const onEnterSuccess = () => listRef.value?.load()

async function openUnbind(row) {
  try {
    await ElMessageBox.confirm(
      `确认将节点 ${row.nodeId} 出垛（解除垛位绑定）吗？`,
      '出垛确认',
      { type: 'warning', confirmButtonText: '确认出垛', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await unbindStack({ ids: [row.id], nodeIds: [row.nodeId] })
    ElMessage.success('出垛成功')
    listRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '出垛失败，请检查后端日志')
  }
}

async function removeNode(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除节点 ${row.nodeId || row.id} 吗？`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch (e) {
    return
  }
  try {
    await deleteNode([row.nodeId])
    ElMessage.success('删除成功')
    listRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败，请检查后端日志')
  }
}

onMounted(() => { buildStackTree() })
</script>

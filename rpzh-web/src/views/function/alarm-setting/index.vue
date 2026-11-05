<template>
  <div class="app-page">
    <div class="app-card function-head">
      <div class="function-head__text">
        <h2>阈值设置</h2>
        <p>报警阈值模板、库区报警规则与预警颜色／升级天数；三类配置均支持新建或修改</p>
      </div>
    </div>

    <el-tabs v-model="active" type="border-card" class="function-tabs">
      <el-tab-pane label="阈值模板" name="template">
        <FunctionListPage
          ref="tplListRef"
          embedded
          title="报警阈值模板"
          :api="findAlarmSettingTemplateTable"
          :columns="templateColumns"
          :hint="templateHint"
          :action-width="190"
        >
          <template #actions>
            <el-button type="primary" size="small" :icon="Plus" @click="openTplCreate">新建模板</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openTplEdit(row)">修改</el-button>
            <el-button link type="danger" @click="removeTemplate(row)">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>

      <el-tab-pane label="库区报警设置" name="area">
        <FunctionListPage
          ref="areaListRef"
          embedded
          title="库区报警设置"
          :api="findWareAreaAlarmSettingInfo"
          :columns="areaColumns"
          :stats="areaStats"
          :action-width="190"
        >
          <template #actions>
            <el-button type="primary" size="small" :icon="Plus" @click="openAreaCreate">新建规则</el-button>
          </template>
          <template #row-actions="{ row }">
            <el-button link type="primary" @click="openAreaEdit(row)">修改</el-button>
            <el-button link type="danger" @click="removeArea(row)">删除</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>

      <el-tab-pane label="颜色与升级" name="color">
        <FunctionListPage
          ref="colorListRef"
          embedded
          title="预警颜色与升级天数"
          :api="findColorAndAlertUpgradeDay"
          :paged="false"
          :show-index="false"
          :columns="colorColumns"
          :hint="colorHint"
          :action-width="150"
        >
          <template #actions>
            <el-button type="primary" size="small" :icon="Edit" @click="openColorEdit">修改配置</el-button>
          </template>
        </FunctionListPage>
      </el-tab-pane>
    </el-tabs>

    <CrudDialog
      v-model="tplVisible"
      :title="tplMode === 'edit' ? '修改阈值模板' : '新建阈值模板'"
      :fields="tplFields"
      :model="tplCurrent"
      :mode="tplMode"
      :defaults="tplDefaults"
      :submit="submitTemplate"
      tip="20 个阈值全部必填，且每组严格递增：下限 < 一级 < 二级 < 三级；包芯高于环境 一级 < 二级 < 三级。"
      width="860px"
      label-width="140px"
      @success="onTplSuccess"
    />

    <CrudDialog
      v-model="areaVisible"
      :title="areaMode === 'edit' ? '修改库区报警规则' : '新建库区报警规则'"
      :fields="areaFields"
      :model="areaCurrent"
      :mode="areaMode"
      :defaults="areaDefaults"
      :submit="submitArea"
      tip="12 个仓间阈值全部必填且每组严格递增；位置路径与区域 ID 仅在新建时需要。"
      width="800px"
      label-width="140px"
      @success="onAreaSuccess"
    />

    <CrudDialog
      v-model="colorVisible"
      title="修改预警颜色与升级天数"
      mode="edit"
      :fields="colorFields"
      :model="colorCurrent"
      :defaults="colorDefaults"
      :submit="submitColor"
      tip="后端按 tb_general_setting 是否已有配置自动选择新增或更新，并刷新 Redis 缓存。"
      width="600px"
      label-width="150px"
      @success="onColorSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit } from '@element-plus/icons-vue'
import FunctionListPage from '../components/FunctionListPage.vue'
import CrudDialog from '../components/CrudDialog.vue'
import {
  findAlarmSettingTemplateTable,
  findColorAndAlertUpgradeDay,
  findWareAreaAlarmSettingInfo,
  findAlarmValueSetting,
  findWareAreaAlarmSettingById,
  insertAlarmSettingTemplate,
  updateAlarmSettingTemplate,
  deleteAlarmValueSetting,
  insertWareAreaAlarmSetting,
  updateWareAreaAlarmSetting,
  deleteWareAreaAlarmSetting,
  updateOrInsertColorAndAlertUpgradeDay
} from '@/api/function/alarmSetting'
import { findAllMaterialCodes } from '@/api/function/material'
import {
  TEMPLATE_THRESHOLDS,
  TEMPLATE_THRESHOLD_DEFAULTS,
  AREA_THRESHOLDS,
  AREA_THRESHOLD_DEFAULTS,
  TEMPLATE_GROUPS,
  AREA_GROUPS,
  checkThresholdGroups
} from '../components/thresholdFields'

/* ---------------- 缓存下拉：养护阶段 / 物料编码 ---------------- */
const PHASE_OPTIONS = [
  { label: '养护阶段', value: '养护阶段' },
  { label: '降氧养护阶段', value: '降氧养护阶段' },
  { label: '恒温养护阶段', value: '恒温养护阶段' },
  { label: '充氮杀虫阶段', value: '充氮杀虫阶段' },
  { label: '磷化铝杀虫阶段', value: '磷化铝杀虫阶段' },
  { label: '充氮保质阶段', value: '充氮保质阶段' },
  { label: '养护复核阶段', value: '养护复核阶段' }
]

const materialOptions = ref([])
async function loadMaterials() {
  try {
    const res = await findAllMaterialCodes()
    const list = Array.isArray(res) ? res : (res?.data || res?.list || res?.rows || [])
    materialOptions.value = list
      .map((m) => {
        const code = typeof m === 'string' ? m : (m.materialCode || m.code || m.value)
        return { label: code, value: code }
      })
      .filter((o) => o.value)
  } catch (e) {
    materialOptions.value = []
  }
}
onMounted(() => { loadMaterials() })

const active = ref('area')

/**
 * 列表字典与下拉选项必须定义在 columns 之前：
 * 下面的 templateColumns 在 setup 执行时就会读取 STYLE_DICT / YES_NO_DICT，
 * 放到后面会触发 "Cannot access 'STYLE_DICT' before initialization"（整页白屏）。
 */
const STYLE_OPTIONS = () => [
  { label: '01 · 常规养护', value: '01' },
  { label: '02 · 降氧养护', value: '02' },
  { label: '03 · 梗丝养护', value: '03' }
]
/** 列表字典：养护方式编码 → 中文，避免直接显示 01/02/03 */
const STYLE_DICT = {
  '01': { label: '常规养护', type: 'info' },
  '02': { label: '降氧养护', type: 'primary' },
  '03': { label: '梗丝养护', type: 'success' }
}
const YES_NO_DICT = {
  '1': { label: '是', type: 'success' },
  '0': { label: '否', type: 'info' }
}

const templateColumns = [
  { prop: 'earlyWarningRuleName', label: '规则名称', minWidth: 180 },
  { prop: 'materialCode', label: '物料编码', minWidth: 160 },
  { prop: 'maintenceStyle', label: '养护方式', width: 120, dict: STYLE_DICT },
  { prop: 'defaultFlag', label: '默认模板', width: 110, dict: YES_NO_DICT },
  { prop: 'createUser', label: '创建人', width: 120 },
  { prop: 'createdTime', label: '创建时间', width: 170, type: 'time' }
]

const areaColumns = [
  { prop: 'siteName', label: '库区位置', minWidth: 240 },
  { prop: 'stackId', label: '区域 ID', width: 110 },
  { prop: 'oxygenList', label: '氧气阈值', width: 150 },
  { prop: 'tempList', label: '温度阈值', width: 150 },
  { prop: 'moistureList', label: '湿度阈值', width: 150 },
  { prop: 'warehouseOxygenLower', label: '氧气下限', width: 110 },
  { prop: 'warehouseTempLower', label: '温度下限', width: 110 },
  { prop: 'warehouseMoistureLower', label: '湿度下限', width: 110 }
]

const colorColumns = [
  { prop: 'oneToTwoDay', label: '一级→二级天数', width: 150 },
  { prop: 'twoToThreeDay', label: '二级→三级天数', width: 150 },
  { prop: 'colorOne', label: '一级颜色', width: 130 },
  { prop: 'colorTwo', label: '二级颜色', width: 130 },
  { prop: 'colorThree', label: '三级颜色', width: 130 }
]

const areaStats = (rows, total) => [
  { label: '规则总数', value: total },
  { label: '当前页', value: rows.length },
  { label: '已配氧气下限', value: rows.filter((r) => r.warehouseOxygenLower).length },
  { label: '已配温度下限', value: rows.filter((r) => r.warehouseTempLower).length }
]

const templateHint =
  '阈值模板的 Oracle 序列 SEIINGTEMPLATE（NEXTVAL/CURRVAL）已改为 MySQL 的 MAX(id)+1 生成主键，' +
  'tb_alarm_setting_template 中的演示模板现可直接读取；已支持新建、修改与删除。'
const colorHint =
  '颜色与升级天数查询的 rownum = 1 已改为 LIMIT 1，tb_general_setting 中的配置行现可正常返回；' +
  '可通过工具栏「修改配置」更新。'

const tplFields = [
  { prop: 'earlyWarningRuleName', label: '规则名称', required: true, span: 12 },
  { prop: 'materialCode', label: '物料编码', type: 'select', required: true, span: 12, options: () => materialOptions.value, filterable: true, allowCreate: true, placeholder: '从物料列表选择或输入' },
  { prop: 'maintenceStyle', label: '养护方式', type: 'select', required: true, span: 12, options: STYLE_OPTIONS },
  { prop: 'phases', label: '养护阶段', type: 'select', required: true, span: 12, options: () => PHASE_OPTIONS, allowCreate: true, placeholder: '选择或输入养护阶段' },
  ...TEMPLATE_THRESHOLDS
]
const tplDefaults = {
  earlyWarningRuleName: '前端新建阈值模板',
  materialCode: 'MAT-2024-001',
  maintenceStyle: '01',
  phases: '养护阶段',
  ...TEMPLATE_THRESHOLD_DEFAULTS
}

const areaFields = [
  {
    prop: 'pidRoute', label: '位置路径', required: true, span: 24,
    hidden: (form, mode) => mode === 'edit',
    hint: '如 [00:0],[01:1],[02:11],[03:101]，后端按段数计算 siteType 并补齐末级'
  },
  {
    prop: 'stackId', label: '区域 ID', type: 'number', required: true, span: 12,
    hidden: (form, mode) => mode === 'edit'
  },
  ...AREA_THRESHOLDS
]
const areaDefaults = {
  pidRoute: '[00:0],[01:1],[02:11],[03:101]',
  stackId: 201,
  ...AREA_THRESHOLD_DEFAULTS
}

const colorFields = [
  { prop: 'colorOne', label: '一级颜色', required: true, span: 12 },
  { prop: 'colorTwo', label: '二级颜色', required: true, span: 12 },
  { prop: 'colorThree', label: '三级颜色', required: true, span: 12 },
  { prop: 'oneToTwoDay', label: '一级→二级天数', type: 'number', required: true, span: 12 },
  { prop: 'twoToThreeDay', label: '二级→三级天数', type: 'number', required: true, span: 12 }
]
const colorDefaults = {
  colorOne: '#FFFF00', colorTwo: '#FFA500', colorThree: '#FF0000',
  oneToTwoDay: 3, twoToThreeDay: 5
}

const tplListRef = ref(null)
const areaListRef = ref(null)
const colorListRef = ref(null)
const tplVisible = ref(false)
const tplMode = ref('create')
const tplCurrent = ref(null)
const areaVisible = ref(false)
const areaMode = ref('create')
const areaCurrent = ref(null)
const colorVisible = ref(false)
const colorCurrent = ref(null)

const onTplSuccess = () => tplListRef.value?.load()
const onAreaSuccess = () => areaListRef.value?.load()
const onColorSuccess = () => colorListRef.value?.load()

const openTplCreate = () => { tplMode.value = 'create'; tplCurrent.value = null; tplVisible.value = true }

async function openTplEdit(row) {
  tplMode.value = 'edit'
  tplCurrent.value = { ...row }
  // 列表接口只返回基础字段，阈值需按物料/规则/阶段单独查询后合并回显
  try {
    const d = await findAlarmValueSetting({
      materialCode: row.materialCode,
      earlyWarningRuleName: row.earlyWarningRuleName,
      phases: row.phases || ''
    })
    if (d && typeof d === 'object' && !Array.isArray(d)) {
      tplCurrent.value = { ...row, ...d }
    }
  } catch (e) { /* 查不到阈值时用默认值回显 */ }
  tplVisible.value = true
}

const openAreaCreate = () => { areaMode.value = 'create'; areaCurrent.value = null; areaVisible.value = true }

async function openAreaEdit(row) {
  areaMode.value = 'edit'
  areaCurrent.value = { ...row }
  try {
    const d = await findWareAreaAlarmSettingById({ id: row.id })
    if (d && typeof d === 'object' && !Array.isArray(d)) {
      areaCurrent.value = { ...row, ...d }
    }
  } catch (e) { /* 查不到时用默认值回显 */ }
  areaVisible.value = true
}

const openColorEdit = () => {
  const rows = colorListRef.value?.state?.rows
  colorCurrent.value = Array.isArray(rows) && rows.length ? { ...rows[0] } : null
  colorVisible.value = true
}

async function submitTemplate(payload, mode) {
  const err = checkThresholdGroups(payload, TEMPLATE_GROUPS)
  if (err) throw new Error(err)
  if (mode === 'edit') {
    await updateAlarmSettingTemplate({
      ...payload,
      alarmValueSettingId: tplCurrent.value?.alarmValueSettingId || tplCurrent.value?.id
    })
  } else {
    // 新建时绝不能传 alarmValueSettingId，否则后端会走「更新现有模板」分支
    await insertAlarmSettingTemplate(payload)
  }
}

async function submitArea(payload, mode) {
  const err = checkThresholdGroups(payload, AREA_GROUPS)
  if (err) throw new Error(err)
  if (mode === 'edit') await updateWareAreaAlarmSetting({ ...payload, id: areaCurrent.value?.id })
  else await insertWareAreaAlarmSetting(payload)
}

async function submitColor(payload) {
  await updateOrInsertColorAndAlertUpgradeDay(payload)
}

async function confirmRemove(message) {
  try {
    await ElMessageBox.confirm(message, '删除确认', {
      type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消'
    })
    return true
  } catch (e) {
    return false
  }
}

async function removeTemplate(row) {
  const id = row.alarmValueSettingId || row.id
  if (!(await confirmRemove(`确认删除阈值模板「${row.earlyWarningRuleName || id}」吗？`))) return
  try {
    await deleteAlarmValueSetting([id])
    ElMessage.success('删除成功')
    tplListRef.value?.load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败，请检查后端日志')
  }
}

async function removeArea(row) {
  if (!(await confirmRemove(`确认删除库区报警规则 ${row.siteName || row.id} 吗？`))) return
  try {
    await deleteWareAreaAlarmSetting([row.id])
    ElMessage.success('删除成功')
    areaListRef.value?.load()
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

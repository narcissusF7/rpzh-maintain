<template>
  <div :class="embedded ? 'function-embedded' : 'app-page'">
    <div v-if="!embedded" class="app-card function-head">
      <div class="function-head__text">
        <h2>{{ title }}</h2>
        <p>{{ description || '数据来自 rpzh-function 微服务（经网关 7001 转发）' }}</p>
      </div>
      <div class="function-head__actions">
        <!-- 页面自定义操作（如「新建」）；未提供时不影响其它列表页 -->
        <slot name="actions" />
        <el-tag :type="state.ok ? 'success' : 'warning'" size="small" effect="light">
          {{ state.ok ? '接口正常' : '降级展示' }}
        </el-tag>
        <el-button size="small" :icon="RefreshRight" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <el-alert
      v-if="!state.ok"
      class="function-alert"
      type="warning"
      show-icon
      :closable="false"
    >
      <template #title>该接口暂不可用：{{ reason }}</template>
      <template #default>
        <div class="function-alert__hint">{{ hint }}</div>
        <div v-if="state.message" class="function-alert__raw" :title="state.message">
          原始信息：{{ state.message.slice(0, 120) }}
        </div>
      </template>
    </el-alert>

    <div v-if="searchFields.length" class="search-bar">
      <el-form :inline="true" @submit.prevent>
        <el-form-item v-for="f in searchFields" :key="f.prop" :label="f.label">
          <el-select
            v-if="f.type === 'select'"
            v-model="query[f.prop]"
            clearable
            :placeholder="`全部${f.label}`"
            :style="{ width: (f.width || 150) + 'px' }"
          >
            <el-option v-for="o in f.options || []" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-input
            v-else
            v-model="query[f.prop]"
            clearable
            :placeholder="f.placeholder || `请输入${f.label}`"
            :style="{ width: (f.width || 180) + 'px' }"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row v-if="statItems.length" :gutter="12" class="function-stats">
      <el-col v-for="s in statItems" :key="s.label" :xs="12" :sm="6">
        <div class="app-card stat-card">
          <div class="stat-card__label">{{ s.label }}</div>
          <div class="stat-card__value">{{ s.value }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="app-card">
      <div class="table-toolbar">
        <span class="table-toolbar__title">{{ title }}列表</span>
        <span class="table-toolbar__count">共 {{ state.total }} 条</span>
        <!-- 管理员可配置本页显示哪些列，按「用户 + 页面」保存到 localStorage -->
        <el-popover v-if="canConfigColumns" placement="bottom-end" :width="230" trigger="click">
          <template #reference>
            <el-button class="column-setting-btn" size="small" :icon="Setting">列设置</el-button>
          </template>
          <div class="column-setting">
            <div class="column-setting__title">选择要显示的列（管理员）</div>
            <el-checkbox-group v-model="checkedProps" class="column-setting__list" @change="onCheckedChange">
              <el-checkbox v-for="c in columns" :key="c.prop" :value="c.prop">{{ c.label }}</el-checkbox>
            </el-checkbox-group>
            <div class="column-setting__footer">
              <span>已显示 {{ visibleColumns.length }} / {{ columns.length }} 列</span>
              <el-button link type="primary" size="small" @click="resetColumns">恢复默认</el-button>
            </div>
          </div>
        </el-popover>
        <div class="toolbar-right" v-if="embedded">
          <!-- 嵌在 tabs 里时没有页面级 header，顶部操作按钮放在工具栏右侧 -->
          <slot name="actions" />
          <el-tooltip content="刷新" placement="top">
            <el-button :icon="RefreshRight" circle size="small" :loading="loading" @click="load" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrap">
        <el-table
          v-loading="loading"
          :data="state.rows"
          border
          stripe
          :row-key="rowKey"
          style="width: 100%"
        >
          <el-table-column v-if="showIndex" type="index" label="#" width="55" align="center" />
          <el-table-column
            v-for="c in visibleColumns"
            :key="c.prop"
            :prop="c.prop"
            :label="c.label"
            :width="c.width"
            :min-width="c.minWidth"
            :align="c.align || 'left'"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <el-tag v-if="c.dict" :type="dictType(c.dict, row[c.prop])" size="small" effect="light">
                {{ dictLabel(c.dict, row[c.prop]) }}
              </el-tag>
              <span v-else-if="c.type === 'time'">{{ formatTime(row[c.prop]) }}</span>
              <span v-else>{{ cell(row, c) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            v-if="detail || hasRowActions"
            label="操作"
            :width="hasRowActions ? actionWidth : 90"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button v-if="detail" link type="primary" @click="openDetail(row)">详情</el-button>
              <slot name="row-actions" :row="row" />
            </template>
          </el-table-column>
          <template #empty>
            <el-empty :image-size="72" :description="state.ok ? '暂无数据' : '接口暂不可用，已降级展示'" />
          </template>
        </el-table>
      </div>

      <div v-if="paged" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page.page"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50]"
          :total="state.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="load"
          @current-change="load"
        />
      </div>
    </div>

    <el-drawer v-model="drawer" title="详情" size="46%" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item v-for="c in visibleColumns" :key="c.prop" :label="c.label">
          {{ cell(detailRow, c) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, useSlots, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, RefreshLeft, RefreshRight, Setting } from '@element-plus/icons-vue'
import { loadList, dictLabel, dictType, shortReason } from '@/utils/function'
import { formatTime } from '@/utils/form'
import { isAdmin, getCachedUser } from '@/utils/auth'

const props = defineProps({
  title: { type: String, default: '列表' },
  description: { type: String, default: '' },
  api: { type: Function, required: true },
  columns: { type: Array, default: () => [] },
  searchFields: { type: Array, default: () => [] },
  defaultParams: { type: Object, default: () => ({}) },
  /** 首次加载前预填的筛选条件（来自路由 query 等），会同步显示在搜索栏 */
  initialQuery: { type: Object, default: () => ({}) },
  /** 后端分页字段名：function 模块统一是 page / size */
  pageKey: { type: String, default: 'page' },
  sizeKey: { type: String, default: 'size' },
  pageSize: { type: Number, default: 10 },
  paged: { type: Boolean, default: true },
  rowKey: { type: String, default: 'id' },
  showIndex: { type: Boolean, default: true },
  detail: { type: Boolean, default: true },
  /** 提供 #row-actions 插槽时操作列的宽度 */
  actionWidth: { type: Number, default: 210 },
  /** 嵌在 el-tabs 里时去掉页面级标题与外边距 */
  embedded: { type: Boolean, default: false },
  /** 列显示配置的存储键；默认按路由路径区分不同页面 */
  storageKey: { type: String, default: '' },
  staticStats: { type: Array, default: () => [] },
  /** (rows, total) => [{ label, value }] */
  stats: { type: Function, default: null },
  /** 请求后对 rows 做二次加工 */
  transform: { type: Function, default: null },
  hint: {
    type: String,
    default: '页面框架、搜索与交互已就绪；后端 mapper 适配 MySQL 后无需改动前端即可展示真实数据。'
  }
})
const emit = defineEmits(['loaded'])
const slots = useSlots()
const hasRowActions = computed(() => !!slots['row-actions'])

const loading = ref(false)
const drawer = ref(false)
const detailRow = ref({})
const query = reactive({})
const page = reactive({ page: 1, size: props.pageSize })
const state = reactive({ ok: true, message: '', rows: [], total: 0 })

/* ---------------- 管理员列显示配置 ---------------- */
const route = useRoute()
const canConfigColumns = computed(() => isAdmin())
const scopeKey = computed(() => props.storageKey || route?.path || props.title)
const storageId = computed(
  () => `rpzh-columns:${getCachedUser()?.username || 'anon'}:${scopeKey.value}`
)
const hiddenProps = ref([])
const checkedProps = ref(props.columns.map((c) => c.prop))

function readHidden() {
  if (!canConfigColumns.value) return []
  try {
    const saved = JSON.parse(localStorage.getItem(storageId.value) || '{}')
    const valid = props.columns.map((c) => c.prop)
    return Array.isArray(saved.hidden) ? saved.hidden.filter((p) => valid.includes(p)) : []
  } catch (e) {
    return []
  }
}

function applyHidden(list) {
  hiddenProps.value = list
  checkedProps.value = props.columns.filter((c) => !list.includes(c.prop)).map((c) => c.prop)
  if (canConfigColumns.value) {
    try {
      localStorage.setItem(storageId.value, JSON.stringify({ hidden: list }))
    } catch (e) { /* 隐私模式下 localStorage 不可写，忽略 */ }
  }
}

function onCheckedChange(vals) {
  const valid = props.columns.map((c) => c.prop)
  const picked = (vals || []).filter((p) => valid.includes(p))
  if (!picked.length) {
    // 至少保留一列，避免表格空白
    applyHidden(valid.slice(1))
    return
  }
  applyHidden(valid.filter((p) => !picked.includes(p)))
}

function resetColumns() {
  applyHidden([])
}

const visibleColumns = computed(() => {
  const list = props.columns.filter((c) => !hiddenProps.value.includes(c.prop))
  return list.length ? list : props.columns.slice(0, 1)
})

onMounted(() => applyHidden(readHidden()))
watch(() => props.columns, () => applyHidden(readHidden()))

const reason = computed(() => shortReason(state.message))
const statItems = computed(() => {
  const list = typeof props.stats === 'function' ? props.stats(state.rows, state.total) : props.staticStats
  return Array.isArray(list) ? list : []
})

function cell(row, c) {
  const v = row?.[c.prop]
  if (c.formatter) return c.formatter(v, row)
  if (c.dict) return dictLabel(c.dict, v)
  if (v === null || v === undefined || v === '') return '-'
  return v
}

function buildParams() {
  const q = {}
  Object.entries(query).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q[k] = v
  })
  return {
    ...props.defaultParams,
    ...q,
    ...(props.paged ? { [props.pageKey]: page.page, [props.sizeKey]: page.size } : {})
  }
}

/** 请求序号：并发加载时只让「最后一次」的结果落地，避免先发后到覆盖筛选结果 */
let reqSeq = 0

async function load() {
  const seq = ++reqSeq
  loading.value = true
  const res = await loadList(props.api, buildParams())
  if (seq !== reqSeq) return
  if (props.transform && res.ok) {
    const patched = props.transform(res.rows, res)
    if (Array.isArray(patched)) res.rows = patched
    else if (patched && typeof patched === 'object') Object.assign(res, patched)
  }
  Object.assign(state, res)
  loading.value = false
  emit('loaded', state)
}

function handleSearch() {
  page.page = 1
  load()
}

function handleReset() {
  Object.keys(query).forEach((k) => delete query[k])
  page.page = 1
  load()
}

/** 供父组件在业务动作后切换筛选条件（例如养护完成后切到「已完成」列表）并重新加载 */
function setQuery(patch = {}) {
  Object.entries(patch).forEach(([k, v]) => {
    if (v === undefined || v === null || v === '') delete query[k]
    else query[k] = v
  })
  page.page = 1
  load()
}

function openDetail(row) {
  detailRow.value = row
  drawer.value = true
}

defineExpose({ load, state, setQuery })
onMounted(() => {
  // 从路由带参进入（如预警详情 → 关联任务）：首次加载就带筛选，避免先拉全量再筛一遍
  Object.entries(props.initialQuery || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') query[k] = v
  })
  load()
})
</script>

<style scoped>
.function-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  padding: 14px 18px;
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
.function-head__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.function-alert {
  margin-bottom: 12px;
}
.function-alert__hint {
  font-size: 12px;
  line-height: 1.7;
}
.function-alert__raw {
  margin-top: 4px;
  font-size: 11px;
  opacity: 0.6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
.table-toolbar__title {
  font-weight: 600;
}
.table-toolbar__count {
  margin-left: 12px;
  font-size: 12px;
  opacity: 0.6;
}
.column-setting-btn {
  margin-left: auto;
}
.column-setting__title {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
}
.column-setting__list {
  display: flex;
  flex-direction: column;
  max-height: 260px;
  overflow: auto;
}
.column-setting__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
  font-size: 11px;
  opacity: 0.75;
}
.function-embedded {
  padding: 0;
}
.function-embedded .function-alert {
  margin-bottom: 10px;
}
</style>

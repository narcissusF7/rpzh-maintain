<template>
  <div class="app-page">
    <div class="search-bar">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="菜单名称">
          <el-input v-model="keyword" placeholder="输入关键字过滤" clearable style="width: 200px" @keyup.enter="applyFilter" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部" clearable style="width: 120px" @change="applyFilter">
            <el-option v-for="item in MENU_STATUS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="applyFilter">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="app-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate()">新增菜单</el-button>
        <el-button :icon="Sort" @click="toggleExpand">{{ expandAll ? '折叠全部' : '展开全部' }}</el-button>
        <div class="toolbar-right">
          <span class="toolbar-hint">菜单要出现在侧边栏，状态必须是「正常」且类型为目录/菜单</span>
          <el-tooltip content="刷新" placement="top">
            <el-button :icon="RefreshRight" circle @click="loadTree" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrap">
        <el-alert
          v-if="!loading && !tableData.length"
          type="info"
          :closable="false"
          show-icon
          style="margin: 0 0 10px"
          title="接口返回空数组"
          description="MenuServiceImpl.SelectMenuList() 把用户名写死成 rpzh，非管理员只返回本人被授权的菜单。执行 sql/menu-seed.sql 并创建 rpzh 账号后这里才有数据。"
        />
        <el-table
          :key="tableKey"
          v-loading="loading"
          :data="tableData"
          border
          row-key="menuId"
          :tree-props="{ children: 'children' }"
          :default-expand-all="expandAll"
        >
          <el-table-column prop="menuName" label="菜单名称" min-width="180" show-overflow-tooltip />
          <el-table-column label="图标" width="70" align="center">
            <template #default="{ row }">
              <el-icon v-if="resolveIcon(row.icon)"><component :is="resolveIcon(row.icon)" /></el-icon>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="typeTag(row.menuType)" size="small">{{ dictLabel(MENU_TYPES, row.menuType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderNum" label="排序" width="80" align="center" />
          <el-table-column prop="path" label="路由地址" min-width="120" show-overflow-tooltip />
          <el-table-column prop="component" label="组件路径" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.component || '-' }}</template>
          </el-table-column>
          <el-table-column prop="perms" label="权限字符" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.perms || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="1"
                inactive-value="0"
                :loading="row.__statusLoading"
                @change="(val) => handleStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="210" fixed="right" align="center">
            <template #default="{ row }">
              <el-button v-if="row.menuType !== 'F'" link type="primary" @click="openCreate(row)">新增</el-button>
              <el-button link type="primary" @click="openEdit(row)">修改</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改菜单' : '新增菜单'"
      width="720px"
      append-to-body
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="12">
          <el-col :span="24">
            <el-form-item label="上级菜单" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="parentOptions"
                :props="{ label: 'menuName', children: 'children', value: 'menuId' }"
                node-key="menuId"
                check-strictly
                default-expand-all
                placeholder="选择上级菜单"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="菜单类型" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio v-for="item in MENU_TYPES" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" :max="999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="目录如 system，菜单如 user" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径" prop="component">
              <el-input
                v-model="form.component"
                :disabled="form.menuType !== 'C'"
                placeholder="如 system/user/index"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="权限字符" prop="perms">
              <el-input v-model="form.perms" :disabled="form.menuType === 'M'" placeholder="如 system:user:list" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单图标">
              <el-input v-model="form.icon" placeholder="如 system / user / Setting" clearable>
                <template #prefix>
                  <el-icon v-if="resolveIcon(form.icon)"><component :is="resolveIcon(form.icon)" /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="显示状态">
              <el-radio-group v-model="form.visible">
                <el-radio v-for="item in VISIBLE_OPTIONS" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="item in MENU_STATUS" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="后端 insertMenu 只写入菜单名称/上级/排序/路由/组件/类型/状态/权限字符/图标；显示状态、是否外链、是否缓存不会落库。"
        />
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, RefreshRight, Search, Sort } from '@element-plus/icons-vue'
import { listMenu, addMenu, updateMenu, deleteMenu, changeMenuStatus } from '@/api/menu'
import { MENU_TYPES, MENU_STATUS, VISIBLE_OPTIONS, dictLabel } from '@/utils/dict'
import { resolveIcon } from '@/utils/icon'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const expandAll = ref(true)
const tableKey = ref(0)

const keyword = ref('')
const statusFilter = ref('')
const rawTree = ref([])

const formRef = ref()
const form = reactive({
  menuId: undefined,
  parentId: 0,
  menuType: 'C',
  menuName: '',
  orderNum: 1,
  path: '',
  component: '',
  perms: '',
  icon: '',
  visible: '0',
  status: '1',
  remark: ''
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由地址', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  orderNum: [{ required: true, message: '请输入显示排序', trigger: 'blur' }]
}

/** 上级菜单只能是目录或菜单，且不能选自己 */
const parentOptions = computed(() => {
  const filter = (list) =>
    list
      .filter((item) => item.menuType !== 'F' && item.menuId !== form.menuId)
      .map((item) => ({ ...item, children: item.children ? filter(item.children) : [] }))

  return [{ menuId: 0, menuName: '主类目', children: filter(rawTree.value) }]
})

/** 客户端过滤：命中关键字的节点保留其父链 */
const tableData = computed(() => {
  const kw = keyword.value.trim()
  const status = statusFilter.value

  const match = (node) => {
    const kwOk = !kw || String(node.menuName || '').includes(kw)
    const statusOk = !status || String(node.status) === String(status)
    const children = (node.children || [])
      .map((child) => ({ ...child, children: filterNode(child) }))
      .filter(Boolean)
    if (kwOk && statusOk) return { ...node, children }
    return children.length ? { ...node, children } : null
  }

  const filterNode = (node) => match(node)

  if (!kw && !status) return rawTree.value
  return rawTree.value.map((node) => match(node)).filter(Boolean)
})

function typeTag(type) {
  if (type === 'M') return 'warning'
  if (type === 'C') return 'success'
  return 'info'
}

function toggleExpand() {
  expandAll.value = !expandAll.value
  tableKey.value += 1
}

function applyFilter() {
  // tableData 是计算属性，这里只负责滚动一下给用户反馈
  tableKey.value += 1
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = ''
  tableKey.value += 1
}

async function loadTree() {
  loading.value = true
  try {
    const data = await listMenu()
    rawTree.value = Array.isArray(data) ? data : []
  } catch (e) {
    rawTree.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  formRef.value?.clearValidate()
  Object.assign(form, {
    menuId: undefined,
    parentId: 0,
    menuType: 'C',
    menuName: '',
    orderNum: 1,
    path: '',
    component: '',
    perms: '',
    icon: '',
    visible: '0',
    status: '1',
    remark: ''
  })
}

function openCreate(parent) {
  isEdit.value = false
  resetForm()
  if (parent) {
    form.parentId = parent.menuId
    form.menuType = parent.menuType === 'M' ? 'C' : 'F'
  }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    menuId: row.menuId,
    parentId: row.parentId ?? 0,
    menuType: row.menuType || 'C',
    menuName: row.menuName,
    orderNum: Number(row.orderNum) || 0,
    path: row.path || '',
    component: row.component || '',
    perms: row.perms || '',
    icon: row.icon || '',
    visible: row.visible != null ? String(row.visible) : '0',
    status: row.status != null ? String(row.status) : '1'
  })
  dialogVisible.value = true
}

/** 后端 editMenu 校验 parentName 非空，需要按 parentId 反查名称 */
function findMenuName(list, id) {
  for (const item of list) {
    if (String(item.menuId) === String(id)) return item.menuName
    if (item.children?.length) {
      const found = findMenuName(item.children, id)
      if (found) return found
    }
  }
  return ''
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.menuType === 'C' && !form.component) {
    ElMessage.warning('菜单类型的组件路径不能为空，否则前端会落到占位页')
  }

  const parentName = form.parentId === 0 ? '主类目' : findMenuName(rawTree.value, form.parentId)

  submitting.value = true
  try {
    const payload = {
      menuId: form.menuId,
      parentId: form.parentId,
      menuType: form.menuType,
      menuName: form.menuName,
      orderNum: String(form.orderNum),
      path: form.path,
      component: form.component,
      perms: form.perms,
      icon: form.icon,
      visible: form.visible,
      status: form.status,
      parentName,
      remark: form.remark
    }

    if (isEdit.value) {
      await updateMenu(payload)
      ElMessage.success('修改菜单成功')
    } else {
      await addMenu(payload)
      ElMessage.success('新增菜单成功')
    }

    dialogVisible.value = false
    loadTree()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row, value) {
  const previous = value === '1' ? '0' : '1'
  row.__statusLoading = true
  try {
    await changeMenuStatus(row.menuId, value)
    ElMessage.success(value === '1' ? '已启用' : '已停用')
  } catch (e) {
    row.status = previous
  } finally {
    row.__statusLoading = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }

  try {
    await deleteMenu(row.menuId)
    ElMessage.success('删除成功')
    loadTree()
  } catch (e) {
    // 存在子菜单或被角色引用时后端会拒绝，提示已在拦截器
  }
}

onMounted(loadTree)
</script>

<style scoped>
.toolbar-hint {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-right: 4px;
}
</style>

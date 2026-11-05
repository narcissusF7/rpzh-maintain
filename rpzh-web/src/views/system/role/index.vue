<template>
  <div class="app-page">
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="角色名称">
          <el-input v-model="query.roleName" placeholder="请输入角色名称" clearable style="width: 170px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="query.roleKey" placeholder="如 admin" clearable style="width: 170px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in NORMAL_DISABLED" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="app-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增角色</el-button>
        <div class="toolbar-right">
          <el-tooltip content="刷新列表" placement="top">
            <el-button :icon="RefreshRight" circle @click="loadList" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="rows" border row-key="roleId">
          <el-table-column prop="roleName" label="角色名称" min-width="130" show-overflow-tooltip />
          <el-table-column prop="roleKey" label="权限字符" min-width="130" show-overflow-tooltip />
          <el-table-column prop="roleSort" label="显示顺序" width="100" align="center" />
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="0"
                inactive-value="1"
                :loading="row.__statusLoading"
                :disabled="row.roleId === 1"
                @change="(val) => handleStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createtime" label="创建时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createtime) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="290" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="openMenuGrant(row)">菜单权限</el-button>
              <el-button link type="warning" @click="openDataScope(row)">数据权限</el-button>
              <el-button link type="danger" :disabled="row.roleId === 1" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="loadList"
          />
        </div>
      </div>
    </div>

    <!-- 新增 / 编辑角色 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="640px" append-to-body>
      <el-alert
        v-if="isEdit && form.roleId === 1"
        type="warning"
        :closable="false"
        show-icon
        title="超级管理员角色后端只允许修改菜单权限，名称/权限字符/排序不会生效"
        style="margin-bottom: 12px"
      />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="form.roleName" placeholder="请输入角色名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限字符" prop="roleKey">
              <el-input v-model="form.roleKey" placeholder="如 maintain_admin" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="roleSort">
              <el-input-number v-model="form.roleSort" :min="0" :max="999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="item in NORMAL_DISABLED" :key="item.value" :value="item.value">
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
          <el-col :span="24">
            <el-form-item label="菜单权限" prop="menuIds">
              <div class="menu-tree-box">
                <div class="menu-tree-actions">
                  <el-checkbox v-model="expandAll" @change="toggleExpandAll">展开/折叠</el-checkbox>
                  <el-checkbox v-model="checkAll" @change="toggleCheckAll">全选/全不选</el-checkbox>
                </div>
                <el-tree
                  ref="menuTreeRef"
                  :data="menuTree"
                  :props="{ label: 'menuName', children: 'children' }"
                  node-key="menuId"
                  show-checkbox
                  default-expand-all
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 数据权限 -->
    <el-dialog v-model="scopeVisible" title="分配数据权限" width="420px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="角色名称">
          <el-input :model-value="scopeRow.roleName" disabled />
        </el-form-item>
        <el-form-item label="权限范围">
          <el-select v-model="scopeValue" style="width: 100%">
            <el-option v-for="item in DATA_SCOPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scopeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDataScope">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import {
  listRole,
  addRole,
  updateRole,
  deleteRoles,
  changeRoleStatus,
  updateDataScope,
  getRoleMenuIds
} from '@/api/role'
import { listMenu } from '@/api/menu'
import { formatTime } from '@/utils/form'
import { NORMAL_DISABLED, DATA_SCOPE_OPTIONS } from '@/utils/dict'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const scopeVisible = ref(false)
const isEdit = ref(false)

const rows = ref([])
const total = ref(0)
const menuTree = ref([])
const expandAll = ref(true)
const checkAll = ref(false)

const query = reactive({ page: 1, size: 10, roleName: '', roleKey: '', status: '' })

const formRef = ref()
const menuTreeRef = ref()
const form = reactive({
  roleId: undefined,
  roleName: '',
  roleKey: '',
  roleSort: 1,
  status: '0',
  remark: '',
  menuIds: []
})

const scopeRow = reactive({ roleId: undefined, roleName: '' })
const scopeValue = ref('1')

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }],
  roleSort: [{ required: true, message: '请输入显示顺序', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const data = await listRole(query)
    rows.value = (data?.list || []).map((item) => ({ ...item, __statusLoading: false }))
    total.value = data?.total || 0
  } catch (e) {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  try {
    const data = await listMenu()
    menuTree.value = Array.isArray(data) ? data : []
  } catch (e) {
    menuTree.value = []
  }
}

function handleSearch() {
  query.page = 1
  loadList()
}

function handleReset() {
  query.roleName = ''
  query.roleKey = ''
  query.status = ''
  query.page = 1
  loadList()
}

function handleSizeChange() {
  query.page = 1
  loadList()
}

function resetForm() {
  formRef.value?.clearValidate()
  Object.assign(form, {
    roleId: undefined,
    roleName: '',
    roleKey: '',
    roleSort: 1,
    status: '0',
    remark: '',
    menuIds: []
  })
  checkAll.value = false
  menuTreeRef.value?.setCheckedKeys([])
}

function toggleExpandAll(value) {
  const nodes = menuTreeRef.value?.store?.nodesMap || {}
  Object.values(nodes).forEach((node) => {
    node.expanded = Boolean(value)
  })
}

function toggleCheckAll(value) {
  if (!menuTreeRef.value) return
  if (value) {
    menuTreeRef.value.setCheckedKeys(collectMenuIds(menuTree.value))
  } else {
    menuTreeRef.value.setCheckedKeys([])
  }
}

function collectMenuIds(list = []) {
  return list.reduce((ids, item) => {
    ids.push(item.menuId)
    if (item.children?.length) ids.push(...collectMenuIds(item.children))
    return ids
  }, [])
}

function openCreate() {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

async function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    roleId: row.roleId,
    roleName: row.roleName,
    roleKey: row.roleKey,
    roleSort: Number(row.roleSort) || 1,
    status: row.status != null ? String(row.status) : '0',
    remark: row.remark || ''
  })
  dialogVisible.value = true

  await loadMenuTree()
  await fillRoleMenus(row.roleId)
}

async function openMenuGrant(row) {
  await openEdit(row)
}

async function fillRoleMenus(roleId) {
  try {
    const ids = await getRoleMenuIds(roleId)
    const keys = (Array.isArray(ids) ? ids : []).map((id) => Number(id))
    // 树是级联勾选：只回填叶子节点，父节点由 el-tree 自动算成半选，
    // 否则 setCheckedKeys 会把父节点下未授权的子菜单一起勾上。
    const leafIds = new Set(collectLeafIds(menuTree.value))
    const leafKeys = keys.filter((id) => leafIds.has(id))
    menuTreeRef.value?.setCheckedKeys(leafKeys)
    // 纯父节点（目录）也要保留，提交时统一合并
    form.menuIds = keys
  } catch (e) {
    form.menuIds = []
  }
}

function collectLeafIds(list = []) {
  return list.reduce((ids, item) => {
    if (item.children?.length) {
      ids.push(...collectLeafIds(item.children))
    } else {
      ids.push(item.menuId)
    }
    return ids
  }, [])
}

function getCheckedMenuIds() {
  const tree = menuTreeRef.value
  if (!tree) return []
  const checked = tree.getCheckedKeys(false)
  const half = tree.getHalfCheckedKeys()
  return [...new Set([...checked, ...half])].map((id) => Number(id))
}

function openDataScope(row) {
  scopeRow.roleId = row.roleId
  scopeRow.roleName = row.roleName
  scopeValue.value = row.dataScope ? String(row.dataScope) : '1'
  scopeVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const menuIds = getCheckedMenuIds()
  if (!menuIds.length) {
    ElMessage.warning('请至少选择一个菜单权限')
    return
  }

  submitting.value = true
  try {
    const payload = {
      roleId: form.roleId,
      roleName: form.roleName,
      roleKey: form.roleKey,
      roleSort: form.roleSort,
      status: form.status,
      remark: form.remark,
      menuIds
    }

    if (isEdit.value) {
      await updateRole(payload)
      ElMessage.success('修改角色成功')
    } else {
      await addRole(payload)
      ElMessage.success('新增角色成功')
    }

    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function submitDataScope() {
  submitting.value = true
  try {
    await updateDataScope(scopeRow.roleId, scopeValue.value)
    ElMessage.success('数据权限修改成功')
    scopeVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row, value) {
  const previous = value === '0' ? '1' : '0'
  row.__statusLoading = true
  try {
    await changeRoleStatus(row.roleId, value)
    ElMessage.success(value === '0' ? '已启用' : '已停用')
  } catch (e) {
    row.status = previous
  } finally {
    row.__statusLoading = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }

  try {
    await deleteRoles([row.roleId])
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(() => {
  loadList()
  loadMenuTree()
})
</script>

<style scoped>
.menu-tree-box {
  width: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 8px 10px;
  max-height: 260px;
  overflow: auto;
}

.menu-tree-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 6px;
  padding-bottom: 6px;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}
</style>

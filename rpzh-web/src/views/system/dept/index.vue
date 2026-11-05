<template>
  <div class="app-page">
    <div class="search-bar">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="部门名称">
          <el-input v-model="keyword" placeholder="输入关键字过滤" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in NORMAL_DISABLED" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadTree">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="app-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate()">新增部门</el-button>
        <el-button :icon="Sort" @click="toggleExpand">{{ expandAll ? '折叠全部' : '展开全部' }}</el-button>
        <div class="toolbar-right">
          <el-tooltip content="刷新" placement="top">
            <el-button :icon="RefreshRight" circle @click="loadTree" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrap">
        <el-table
          :key="tableKey"
          v-loading="loading"
          :data="tableData"
          border
          row-key="deptId"
          :tree-props="{ children: 'children' }"
          :default-expand-all="expandAll"
        >
          <el-table-column prop="deptName" label="部门名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="orderNum" label="排序" width="80" align="center" />
          <el-table-column prop="leader" label="负责人" width="110">
            <template #default="{ row }">{{ row.leader || '-' }}</template>
          </el-table-column>
          <el-table-column prop="phone" label="联系电话" width="140">
            <template #default="{ row }">{{ row.phone || '-' }}</template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.email || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="0"
                inactive-value="1"
                :loading="row.__statusLoading"
                @change="(val) => handleStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openCreate(row)">新增</el-button>
              <el-button link type="primary" @click="openEdit(row)">修改</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改部门' : '新增部门'"
      width="600px"
      append-to-body
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'deptName', children: 'children', value: 'deptId' }"
            node-key="deptId"
            check-strictly
            default-expand-all
            placeholder="选择上级部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" placeholder="请输入部门名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" :max="999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.leader" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="部门状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="item in NORMAL_DISABLED" :key="item.value" :value="item.value">
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { listDept, addDept, updateDept, deleteDept, changeDeptStatus } from '@/api/dept'
import { formatTime } from '@/utils/form'
import { NORMAL_DISABLED } from '@/utils/dict'

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
  deptId: undefined,
  parentId: 0,
  deptName: '',
  orderNum: 1,
  leader: '',
  phone: '',
  email: '',
  status: '0'
})

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  orderNum: [{ required: true, message: '请输入显示排序', trigger: 'blur' }],
  phone: [{ pattern: /^[\d-]{6,20}$/, message: '联系电话格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const parentOptions = computed(() => {
  const filter = (list, selfId) =>
    list
      .filter((item) => item.deptId !== selfId)
      .map((item) => ({ ...item, children: item.children ? filter(item.children, selfId) : [] }))

  return [{ deptId: 0, deptName: '主类目', children: filter(rawTree.value, form.deptId) }]
})

const tableData = computed(() => {
  const kw = keyword.value.trim()
  const status = statusFilter.value
  if (!kw && !status) return rawTree.value

  const filterNode = (node) => {
    const children = (node.children || []).map(filterNode).filter(Boolean)
    const kwOk = !kw || String(node.deptName || '').includes(kw)
    const statusOk = !status || String(node.status) === String(status)
    if ((kwOk && statusOk) || children.length) return { ...node, children }
    return null
  }

  return rawTree.value.map(filterNode).filter(Boolean)
})

function toggleExpand() {
  expandAll.value = !expandAll.value
  tableKey.value += 1
}

async function loadTree() {
  loading.value = true
  try {
    const data = await listDept()
    rawTree.value = Array.isArray(data) ? data : []
  } catch (e) {
    rawTree.value = []
  } finally {
    loading.value = false
  }
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = ''
  loadTree()
}

function resetForm() {
  formRef.value?.clearValidate()
  Object.assign(form, {
    deptId: undefined,
    parentId: 0,
    deptName: '',
    orderNum: 1,
    leader: '',
    phone: '',
    email: '',
    status: '0'
  })
}

function openCreate(parent) {
  isEdit.value = false
  resetForm()
  if (parent) form.parentId = parent.deptId
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    deptId: row.deptId,
    parentId: row.parentId ?? 0,
    deptName: row.deptName,
    orderNum: Number(row.orderNum) || 0,
    leader: row.leader || '',
    phone: row.phone || '',
    email: row.email || '',
    status: row.status != null ? String(row.status) : '0'
  })
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isEdit.value && String(form.deptId) === String(form.parentId)) {
    ElMessage.warning('上级部门不能是自己')
    return
  }

  submitting.value = true
  try {
    const payload = {
      deptId: form.deptId,
      parentId: form.parentId,
      deptName: form.deptName,
      orderNum: form.orderNum,
      leader: form.leader,
      phone: form.phone,
      email: form.email,
      status: form.status
    }

    if (isEdit.value) {
      await updateDept(payload)
      ElMessage.success('修改部门成功')
    } else {
      await addDept(payload)
      ElMessage.success('新增部门成功')
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
  const previous = value === '0' ? '1' : '0'
  row.__statusLoading = true
  try {
    await changeDeptStatus(row.deptId, value)
    ElMessage.success(value === '0' ? '已启用' : '已停用')
  } catch (e) {
    row.status = previous
  } finally {
    row.__statusLoading = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除部门「${row.deptName}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }

  try {
    await deleteDept(row.deptId)
    ElMessage.success('删除成功')
    loadTree()
  } catch (e) {
    // 存在下级部门或部门下有用户时后端会拒绝
  }
}

onMounted(loadTree)
</script>

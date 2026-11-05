<template>
  <div class="app-page">
    <!-- 查询条件 -->
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="用户昵称">
          <el-input v-model="query.nickName" placeholder="请输入昵称" clearable style="width: 170px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phonenumber" placeholder="请输入手机号" clearable style="width: 170px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="query.deptId"
            :data="deptTree"
            :props="deptProps"
            node-key="deptId"
            check-strictly
            clearable
            placeholder="全部部门"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="app-card">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
        <el-button type="danger" plain :icon="Delete" :disabled="!selection.length" @click="handleBatchDelete">
          批量删除{{ selection.length ? `(${selection.length})` : '' }}
        </el-button>
        <div class="toolbar-right">
          <el-tooltip content="刷新列表" placement="top">
            <el-button :icon="RefreshRight" circle @click="loadList" />
          </el-tooltip>
        </div>
      </div>

      <div class="table-wrap">
        <el-table
          v-loading="loading"
          :data="rows"
          border
          row-key="userId"
          @selection-change="(val) => (selection = val)"
        >
          <el-table-column type="selection" width="46" align="center" />
          <el-table-column prop="username" label="账号" min-width="110" show-overflow-tooltip />
          <el-table-column prop="nickname" label="昵称" min-width="110" show-overflow-tooltip />
          <el-table-column prop="deptname" label="部门" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ row.deptname || '-' }}</template>
          </el-table-column>
          <el-table-column prop="phonenumber" label="手机号" width="130">
            <template #default="{ row }">{{ row.phonenumber || '-' }}</template>
          </el-table-column>
          <el-table-column label="性别" width="80" align="center">
            <template #default="{ row }">{{ sexLabel(row.sex) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110" align="center">
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
          <el-table-column prop="createtime" label="创建时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createtime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="210" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="loadList"
          />
        </div>
      </div>
    </div>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="640px"
      append-to-body
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用户账号" prop="userName">
              <el-input v-model="form.userName" :disabled="isEdit" placeholder="仅字母和数字" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入昵称" />
            </el-form-item>
          </el-col>
          <el-col v-if="!isEdit" :span="12">
            <el-form-item label="用户密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="6-20 位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="deptProps"
                node-key="deptId"
                check-strictly
                clearable
                placeholder="请选择部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="form.sex">
                <el-radio value="1">男</el-radio>
                <el-radio value="0">女</el-radio>
                <el-radio value="2">未知</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账号状态">
              <el-radio-group v-model="form.status">
                <el-radio value="1">正常</el-radio>
                <el-radio value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色" prop="roleIds">
              <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
                <el-option
                  v-for="role in roleOptions"
                  :key="role.roleId"
                  :label="role.roleName"
                  :value="role.roleId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Refresh, RefreshRight, Search } from '@element-plus/icons-vue'
import {
  listUser,
  addUser,
  updateUser,
  deleteUser,
  changeUserStatus,
  resetUserPassword,
  getUserRoleIds
} from '@/api/user'
import { listRole } from '@/api/role'
import { listDept } from '@/api/dept'
import { formatTime } from '@/utils/form'

const deptProps = { label: 'deptName', children: 'children', value: 'deptId' }

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const rows = ref([])
const total = ref(0)
const selection = ref([])
const deptTree = ref([])
const roleOptions = ref([])

const query = reactive({
  page: 1,
  size: 10,
  nickName: '',
  phonenumber: '',
  status: '',
  deptId: undefined
})

const formRef = ref()
const form = reactive({
  userId: undefined,
  userName: '',
  nickName: '',
  password: '',
  phonenumber: '',
  email: '',
  deptId: undefined,
  sex: '2',
  status: '1',
  roleIds: [],
  remark: ''
})

const rules = {
  userName: [
    { required: true, message: '请输入用户账号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '账号只能包含字母和数字', trigger: 'blur' }
  ],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^[^\u4e00-\u9fa5]+$/, message: '密码不能包含中文', trigger: 'blur' }
  ],
  phonenumber: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个角色', trigger: 'change' }]
}

function sexLabel(sex) {
  if (sex === '0' || sex === 0) return '女'
  if (sex === '1' || sex === 1) return '男'
  return '未知'
}

async function loadList() {
  loading.value = true
  try {
    const data = await listUser(query)
    rows.value = (data?.list || []).map((item) => ({ ...item, __statusLoading: false }))
    total.value = data?.total || 0
  } catch (e) {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadDeptTree() {
  try {
    const data = await listDept()
    deptTree.value = Array.isArray(data) ? data : []
  } catch (e) {
    deptTree.value = []
  }
}

async function loadRoles() {
  try {
    const data = await listRole({ page: 1, size: 200 })
    roleOptions.value = data?.list || []
  } catch (e) {
    roleOptions.value = []
  }
}

function handleSearch() {
  query.page = 1
  loadList()
}

function handleReset() {
  query.nickName = ''
  query.phonenumber = ''
  query.status = ''
  query.deptId = undefined
  query.page = 1
  loadList()
}

function handleSizeChange() {
  query.page = 1
  loadList()
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    userId: undefined,
    userName: '',
    nickName: '',
    password: '',
    phonenumber: '',
    email: '',
    deptId: undefined,
    sex: '2',
    status: '0',
    roleIds: [],
    remark: ''
  })
}

function openCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    userId: row.userId,
    userName: row.username,
    nickName: row.nickname,
    phonenumber: row.phonenumber || '',
    email: row.email || '',
    deptId: row.deptId,
    sex: row.sex != null ? String(row.sex) : '2',
    status: row.status != null ? String(row.status) : '1',
    remark: row.remark || '',
    roleIds: []
  })
  dialogVisible.value = true

  // 已授权角色单独查
  try {
    const roleIds = await getUserRoleIds(row.userId)
    form.roleIds = (Array.isArray(roleIds) ? roleIds : []).map((id) => Number(id))
  } catch (e) {
    form.roleIds = []
  }
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isEdit.value && form.roleIds.length === 0) {
    ElMessage.warning('请选择用户角色')
    return
  }

  submitting.value = true
  try {
    const payload = {
      userId: form.userId,
      userName: form.userName,
      nickName: form.nickName,
      password: form.password,
      phonenumber: form.phonenumber,
      email: form.email,
      deptId: form.deptId,
      sex: form.sex,
      status: form.status,
      remark: form.remark,
      roleIds: form.roleIds
    }

    if (isEdit.value) {
      await updateUser(payload)
      ElMessage.success('修改用户成功')
    } else {
      await addUser(payload)
      ElMessage.success('新增用户成功')
    }
    dialogVisible.value = false
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
    await changeUserStatus(row.userId, value)
    ElMessage.success(value === '0' ? '已启用' : '已停用')
  } catch (e) {
    row.status = previous
  } finally {
    row.__statusLoading = false
  }
}

async function handleResetPassword(row) {
  let password = ''
  try {
    const result = await ElMessageBox.prompt(`请输入 ${row.username} 的新密码`, '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /^.{6,20}$/,
      inputErrorMessage: '密码长度 6-20 位'
    })
    password = result.value
  } catch (e) {
    return
  }

  try {
    await resetUserPassword(row.username, password)
    ElMessage.success('密码重置成功')
  } catch (e) {
    // 拦截器已提示
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.nickname || row.username}」吗？`, '提示', {
      type: 'warning'
    })
  } catch (e) {
    return
  }

  try {
    await deleteUser([row.userId])
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 拦截器已提示
  }
}

async function handleBatchDelete() {
  const ids = selection.value.map((item) => item.userId)
  if (!ids.length) return

  try {
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 个用户吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }

  try {
    await deleteUser(ids)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(() => {
  loadList()
  loadDeptTree()
  loadRoles()
})
</script>

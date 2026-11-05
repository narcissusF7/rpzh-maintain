<template>
  <div class="app-page">
    <el-row :gutter="12">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">系统模块</div>
          <div class="stat-value">{{ stats.modules }}</div>
          <div class="stat-desc">用户 / 角色 / 菜单 / 部门</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">用户总数</div>
          <div class="stat-value">{{ stats.user }}</div>
          <div class="stat-desc">tb_user（del_flag = 1）</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">角色总数</div>
          <div class="stat-value">{{ stats.role }}</div>
          <div class="stat-desc">tb_role（del_flag = 0）</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">部门总数</div>
          <div class="stat-value">{{ stats.dept }}</div>
          <div class="stat-desc">当前部门树节点数</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :xs="24" :md="14">
        <div class="app-card panel">
          <div class="panel-title">
            接口自检
            <el-button size="small" text type="primary" :loading="checking" @click="runCheck">
              重新检测
            </el-button>
          </div>
          <el-table :data="checks" size="small" border>
            <el-table-column prop="name" label="接口" width="200" />
            <el-table-column prop="path" label="地址" show-overflow-tooltip />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.ok ? 'success' : 'danger'" size="small">
                  {{ row.ok ? '正常' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="说明" show-overflow-tooltip />
          </el-table>
        </div>
      </el-col>

      <el-col :xs="24" :md="10">
        <div class="app-card panel">
          <div class="panel-title">环境信息</div>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="当前账号">
              {{ userStore.displayName }}
              <el-tag v-if="userStore.demoMode" size="small" type="warning" style="margin-left: 6px">
                免登录
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="请求前缀">{{ apiBase }}</el-descriptions-item>
            <el-descriptions-item label="菜单来源">
              {{ permissionStore.fallback ? '本地兜底（后端返回为空）' : '后端 /menu/getRouters' }}
            </el-descriptions-item>
            <el-descriptions-item label="前端版本">Vue 3 + Element Plus + Vite</el-descriptions-item>
          </el-descriptions>

          <div class="quick-links">
            <el-button v-for="link in quickLinks" :key="link.path" size="small" @click="router.push(link.path)">
              {{ link.title }}
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listUser } from '@/api/user'
import { listRole } from '@/api/role'
import { listDept } from '@/api/dept'
import { listMenu } from '@/api/menu'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const apiBase = import.meta.env.VITE_API_BASE || '/api'
const checking = ref(false)

const stats = reactive({ modules: 4, user: '-', role: '-', dept: '-' })

const checks = ref([
  { name: '用户分页', path: 'POST /username/findAllUserList', ok: false, message: '未检测' },
  { name: '角色分页', path: 'POST /role/findAllRole', ok: false, message: '未检测' },
  { name: '菜单树', path: 'GET /menu/list', ok: false, message: '未检测' },
  { name: '部门树', path: 'POST /dept/findAllDept', ok: false, message: '未检测' }
])

const quickLinks = [
  { path: '/system/user', title: '用户管理' },
  { path: '/system/role', title: '角色管理' },
  { path: '/system/menu', title: '菜单管理' },
  { path: '/system/dept', title: '部门管理' }
]

function countTree(list = []) {
  return list.reduce((sum, item) => sum + 1 + countTree(item.children || []), 0)
}

async function runCheck() {
  checking.value = true
  const results = await Promise.allSettled([
    listUser({ page: 1, size: 1 }),
    listRole({ page: 1, size: 1 }),
    listMenu(),
    listDept()
  ])

  const executor = [
    (data) => {
      stats.user = data?.total ?? 0
      return `共 ${stats.user} 条`
    },
    (data) => {
      stats.role = data?.total ?? 0
      return `共 ${stats.role} 条`
    },
    (data) => {
      stats.menu = Array.isArray(data) ? data.length : 0
      return `顶层 ${Array.isArray(data) ? data.length : 0} 个`
    },
    (data) => {
      stats.dept = countTree(Array.isArray(data) ? data : [])
      return `共 ${stats.dept} 个节点`
    }
  ]

  checks.value = results.map((result, index) => {
    const base = checks.value[index]
    if (result.status === 'fulfilled') {
      return { ...base, ok: true, message: executor[index](result.value) }
    }
    return { ...base, ok: false, message: result.reason?.message || '请求失败' }
  })

  checking.value = false
}

onMounted(runCheck)
</script>

<style scoped>
.stat-card {
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 12px;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  margin: 6px 0 2px;
  color: var(--el-color-primary);
}

.stat-desc {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.panel {
  padding: 14px 16px 16px;
  margin-bottom: 12px;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}
</style>

<template>
  <div class="app-wrapper">
    <aside class="app-sidebar" :class="{ 'is-collapsed': appStore.sidebarCollapsed }">
      <div class="logo">
        <span class="logo-dot"></span>
        <span v-show="!appStore.sidebarCollapsed">养护管理</span>
      </div>

      <div class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          unique-opened
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title><span>首页</span></template>
          </el-menu-item>

          <sidebar-item v-for="menu in permissionStore.visibleMenus" :key="menu.path" :menu="menu" />
        </el-menu>

        <div v-if="!permissionStore.visibleMenus.length" class="menu-empty">暂无菜单</div>
      </div>
    </aside>

    <div class="app-main-container">
      <header class="app-navbar">
        <div class="nav-left">
          <span class="collapse-btn" @click="appStore.toggleSidebar()">
            <el-icon>
              <Expand v-if="appStore.sidebarCollapsed" />
              <Fold v-else />
            </el-icon>
          </span>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="nav-right">
          <a class="screen-entry" href="/screen" target="_blank" rel="noopener" title="在新窗口打开养护大屏">
            养护大屏
          </a>

          <el-tooltip v-if="permissionStore.fallback" placement="bottom" :content="fallbackTip">
            <el-tag size="small" type="warning" effect="plain">本地菜单</el-tag>
          </el-tooltip>

          <el-tooltip :content="appStore.dark ? '切换到亮色' : '切换到暗色'" placement="bottom">
            <span class="collapse-btn" @click="appStore.toggleTheme()">
              <el-icon>
                <Sunny v-if="appStore.dark" />
                <Moon v-else />
              </el-icon>
            </span>
          </el-tooltip>

          <el-tooltip content="刷新当前页" placement="bottom">
            <span class="collapse-btn" @click="reload">
              <el-icon><Refresh /></el-icon>
            </span>
          </el-tooltip>

          <el-dropdown trigger="click" @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="26" :src="userStore.userInfo.avatar">{{ avatarText }}</el-avatar>
              <span>{{ userStore.displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="app-views">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>

    <password-dialog v-model="passwordVisible" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'
import SidebarItem from './components/SidebarItem.vue'
import PasswordDialog from './components/PasswordDialog.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const passwordVisible = ref(false)

const activeMenu = computed(() => route.path)

const avatarText = computed(() => (userStore.displayName || '游').slice(0, 1).toUpperCase())

/** 面包屑：在权限菜单里回溯当前路径的祖先链 */
const breadcrumbs = computed(() => {
  const trail = []
  const walk = (menus, target) => {
    for (const menu of menus) {
      if (target === menu.path || target.startsWith(`${menu.path}/`)) {
        trail.push({ path: menu.path, title: menu.title })
        if (menu.children?.length) walk(menu.children, target)
        return
      }
    }
  }
  walk(permissionStore.visibleMenus, route.path)
  return trail
})

const fallbackTip = computed(() => {
  const reason = permissionStore.error ? `（${permissionStore.error}）` : ''
  return `后端 /menu/getRouters 返回为空，当前使用本地路由兜底${reason}`
})

function reload() {
  window.location.reload()
}

async function onCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }

  if (command === 'password') {
    passwordVisible.value = true
    return
  }

  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '退出',
        cancelButtonText: '取消'
      })
    } catch (e) {
      return
    }
    userStore.logout()
    permissionStore.reset()
    router.replace('/login')
  }
}
</script>

<style scoped>
.menu-empty {
  padding: 16px;
  font-size: 12px;
  color: #8a99a8;
  text-align: center;
}
.screen-entry {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  margin-right: 4px;
  border-radius: 6px;
  border: 1px solid #2f8f76;
  background: #153a36;
  color: #6ff0bd;
  font-size: 12px;
  text-decoration: none;
  white-space: nowrap;
}
.screen-entry:hover { background: #1c4c46; color: #9df5cf; }
</style>

<template>
  <el-sub-menu v-if="hasChildren" :index="menu.path">
    <template #title>
      <el-icon v-if="iconName"><component :is="iconName" /></el-icon>
      <span>{{ menu.title }}</span>
    </template>
    <sidebar-item v-for="child in menu.children" :key="child.path" :menu="child" />
  </el-sub-menu>

  <el-menu-item v-else :index="menu.path">
    <el-icon v-if="iconName"><component :is="iconName" /></el-icon>
    <template #title>
      <span>{{ menu.title }}</span>
    </template>
  </el-menu-item>
</template>

<script setup>
import { computed } from 'vue'
import { resolveIcon } from '@/utils/icon'

defineOptions({ name: 'SidebarItem' })

const props = defineProps({
  menu: {
    type: Object,
    required: true
  }
})

const hasChildren = computed(() => Array.isArray(props.menu.children) && props.menu.children.length > 0)

const iconName = computed(() => resolveIcon(props.menu.icon))
</script>

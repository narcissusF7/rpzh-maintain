<template>
  <div class="app-page">
    <div class="app-card placeholder">
      <el-icon class="icon"><Tools /></el-icon>
      <div class="title">{{ title }}</div>
      <div class="desc">
        后端菜单里配置了该页面的组件路径
        <code>{{ component || '（空）' }}</code>
        ，前端还没有对应的 <code>.vue</code> 文件。
      </div>
      <div class="desc">
        在 <code>src/views/{{ component || 'xxx' }}.vue</code> 新建文件即可自动生效，无需改路由。
      </div>
      <el-button size="small" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta?.title || '页面未实现')
const component = computed(() => route.meta?.componentPath || '')
</script>

<style scoped>
.placeholder {
  padding: 60px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;
}

.placeholder .icon {
  font-size: 42px;
  color: var(--el-color-warning);
}

.placeholder .title {
  font-size: 18px;
  font-weight: 600;
}

.placeholder .desc {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  max-width: 560px;
  line-height: 1.8;
}

code {
  background: var(--el-fill-color-light);
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 12px;
}
</style>

<template>
  <div class="error-page">
    <div class="code">404</div>
    <div class="desc">抱歉，你访问的页面不存在或已被移除</div>
    <el-button type="primary" @click="$router.replace('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.error-page {
  height: 100%;
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

.code {
  font-size: 72px;
  font-weight: 700;
  color: var(--el-color-primary);
  line-height: 1;
}

.desc {
  color: var(--el-text-color-secondary);
}
</style>
